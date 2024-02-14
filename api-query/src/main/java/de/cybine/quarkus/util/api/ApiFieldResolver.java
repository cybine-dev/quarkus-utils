package de.cybine.quarkus.util.api;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import de.cybine.quarkus.config.*;
import de.cybine.quarkus.util.*;
import de.cybine.quarkus.util.api.permission.*;
import io.quarkus.arc.*;
import io.quarkus.security.identity.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.inject.*;
import jakarta.inject.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

@Slf4j
@Singleton
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ApiFieldResolver
{
    public static final String DEFAULT_CONTEXT = "default";

    private final ObjectMapper     objectMapper;
    private final SecurityIdentity securityIdentity;
    private final ApiQueryConfig   apiQueryConfig;

    private final List<ApiField>                fields   = new ArrayList<>();
    private final List<ApiFieldResolverContext> contexts = new ArrayList<>();

    private final Map<Type, Type> representationTypes = new HashMap<>();

    private ApiPermissionConfig permissionConfig;

    @PostConstruct
    void setup( ) throws URISyntaxException, JsonProcessingException
    {
        String permissionJson = FilePathHelper.resolvePath(this.apiQueryConfig.paths().apiPermissionsPath())
                                              .flatMap(FilePathHelper::tryRead)
                                              .orElse(null);

        this.permissionConfig = ApiPermissionConfig.builder().build();
        if (permissionJson != null)
            this.permissionConfig = this.objectMapper.readValue(permissionJson, ApiPermissionConfig.class);

        List<ApiTypeMapping> typeMappings = this.permissionConfig.getTypeMappings().orElse(Collections.emptyList());

        this.contexts.clear();
        this.contexts.add(new ApiFieldResolverContext(DEFAULT_CONTEXT, ( ) -> Collections.unmodifiableList(this.fields),
                this.permissionConfig.getAvailableActions().orElse(Collections.emptyList()),
                this.permissionConfig.getCapabilities().orElse(Collections.emptyList()),
                this.permissionConfig.getTypes().orElse(Collections.emptyList()), typeMappings));

        for (ApiContextConfig config : this.permissionConfig.getContexts().orElse(Collections.emptyList()))
        {
            log.debug("Registering api-context {}...", config.getName());
            this.contexts.add(
                    new ApiFieldResolverContext(config.getName(), ( ) -> Collections.unmodifiableList(this.fields),
                            config.getAvailableActions(), config.getCapabilities(), config.getTypes(), typeMappings));
        }
    }

    @Produces
    @DefaultBean
    @Unremovable
    @RequestScoped
    @SneakyThrows
    public ApiFieldResolverContext getUserContext( )
    {
        if (this.securityIdentity == null)
            return this.getDefaultContext();

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
        return this.findContext(ApiFieldResolver.DEFAULT_CONTEXT).orElseThrow();
    }

    public Optional<ApiFieldResolverContext> findContext(String context)
    {
        return this.contexts.stream().filter(item -> item.getContextName().equals(context)).findAny();
    }

    public ApiFieldResolverHelper registerType(Type representationType)
    {
        return new ApiFieldResolverHelper(this, representationType, this.representationTypes::put, this.fields::add);
    }

    public Optional<Type> findRepresentationType(Type type)
    {
        return Optional.ofNullable(this.representationTypes.get(type));
    }
}
