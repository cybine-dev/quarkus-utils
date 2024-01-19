package de.cybine.quarkus.util.api;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import de.cybine.quarkus.config.*;
import de.cybine.quarkus.util.*;
import de.cybine.quarkus.util.api.permission.*;
import de.cybine.quarkus.util.datasource.*;
import io.quarkus.security.identity.*;
import jakarta.annotation.*;
import jakarta.inject.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class ApiFieldResolver
{
    public static final String DEFAULT_CONTEXT = "default";

    private final ObjectMapper     objectMapper;
    private final SecurityIdentity securityIdentity;
    private final ApiQueryConfig   apiQueryConfig;

    private final Map<String, ApiFieldResolverContext> contexts = new HashMap<>();

    private final Map<BiTuple<Type, String>, DatasourceField> fields = new HashMap<>();

    private final Map<Type, Type> representationTypes = new HashMap<>();

    private ApiPermissionConfig permissionConfig;

    @PostConstruct
    void setup( ) throws URISyntaxException, JsonProcessingException
    {
        String permissionJson = FilePathHelper.resolvePath(this.apiQueryConfig.paths().apiPermissionsPath())
                                              .flatMap(FilePathHelper::tryRead)
                                              .orElse(null);

        this.permissionConfig = ApiPermissionConfig.builder().build();
        if(permissionJson != null)
            this.permissionConfig = this.objectMapper.readValue(permissionJson, ApiPermissionConfig.class);

        this.contexts.clear();
        for (ApiContextConfig context : this.permissionConfig.getContexts().orElse(Collections.emptyList()))
            log.debug("Registering api-context {}", this.getContext(context.getName()).getContextName());
    }

    public ApiFieldResolverContext getUserContext( )
    {
        return this.permissionConfig.getContextMappings()
                                    .orElse(Collections.emptyList())
                                    .stream()
                                    .filter(item -> this.securityIdentity.checkPermissionBlocking(
                                            ApiPermission.of(item.getPermission())))
                                    .sorted()
                                    .findFirst()
                                    .map(ApiContextMapping::getName)
                                    .flatMap(this::findContext)
                                    .orElseGet(this::getDefaultContext);
    }

    public ApiFieldResolverContext getDefaultContext( )
    {
        return this.getContext(ApiFieldResolver.DEFAULT_CONTEXT);
    }

    public ApiFieldResolverContext getContext(String name)
    {
        return this.contexts.computeIfAbsent(name,
                context -> new ApiFieldResolverContext(context, this.fields::get, new ArrayList<>(), new ArrayList<>(),
                        new ArrayList<>(), new HashMap<>()));
    }

    public Optional<ApiFieldResolverContext> findContext(String context)
    {
        return Optional.ofNullable(this.contexts.get(context));
    }

    public ApiFieldResolver registerField(Type dataType, String alias, DatasourceField field)
    {
        log.debug("Registering api-field: {}({})", dataType.getTypeName(), alias);

        this.fields.put(new BiTuple<>(this.findRepresentationType(dataType).orElse(dataType), alias), field);
        return this;
    }

    public ApiFieldResolverHelper registerTypeRepresentation(Type representationType, Type datasourceType)
    {
        this.representationTypes.put(representationType, datasourceType);
        return this.getTypeRepresentationHelper(representationType);
    }

    public ApiFieldResolverHelper getTypeRepresentationHelper(Type representationType)
    {
        return new ApiFieldResolverHelper(this, representationType);
    }

    public Optional<Type> findRepresentationType(Type type)
    {
        return Optional.ofNullable(this.representationTypes.get(type));
    }
}
