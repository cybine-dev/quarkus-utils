package de.cybine.quarkus.util.api;

import de.cybine.quarkus.util.api.permission.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ApiFieldResolverContext
{
    @Getter
    private final String contextName;

    private final Supplier<List<ApiField>> fieldSupplier;

    private final List<String>         availableActions;
    private final List<ApiCapability>  capabilities;
    private final List<ApiTypeConfig>  types;
    private final List<ApiTypeMapping> typeMappings;

    public boolean canExecuteAction(Type responseType, String action)
    {
        return this.findType(responseType)
                   .map(item -> item.getAvailableActions().contains(action))
                   .orElseGet(( ) -> this.availableActions.contains(action));
    }

    public boolean hasCapability(Type responseType, String capability, ApiCapabilityScope scope)
    {
        return this.findCapability(responseType, capability).map(item -> this.hasCapability(item, scope)).orElse(false);
    }

    public boolean hasCapability(Type responseType, String capability, ApiCapabilityScope scope, String property)
    {
        ApiFieldConfig config = this.findFieldConfig(responseType, property).orElse(null);
        if (config == null || config.isUnavailable())
            return this.hasCapability(responseType, capability, scope);

        return this.findCapability(config.getCapabilities(), capability)
                   .map(item -> this.hasCapability(item, scope))
                   .orElseGet(( ) -> this.hasCapability(responseType, capability, scope));
    }

    private boolean hasCapability(ApiCapability apiCapability, ApiCapabilityScope scope)
    {
        if (apiCapability == null)
            return false;

        if (apiCapability.getScopes().contains(ApiCapabilityScope.ALL))
            return true;

        return apiCapability.getScopes().contains(scope);
    }

    public boolean hasAnyCapability(Type responseType, String capability)
    {
        return this.findCapability(responseType, capability).isPresent();
    }

    public boolean hasAnyCapability(Type responseType, String capability, String property)
    {
        ApiFieldConfig config = this.findFieldConfig(responseType, property).orElse(null);
        if (config == null)
            return this.hasAnyCapability(responseType, capability);

        return config.isAvailable() && this.findCapability(config.getCapabilities(), capability).isPresent();
    }

    public boolean isAvailable(Type responseType, String property)
    {
        return this.findFieldConfig(responseType, property).map(ApiFieldConfig::isAvailable).orElse(true);
    }

    public Optional<ApiFieldPath> findField(Type responseType, String fieldName)
    {
        if (fieldName == null || fieldName.isBlank())
            return Optional.empty();

        ApiField field = null;
        String[] names = fieldName.split("\\.");
        ApiFieldPath.Generator path = ApiFieldPath.builder();
        for (String name : names)
        {
            if(name.isBlank())
                continue;

            Type fieldType = responseType;
            if(field != null)
                fieldType = field.getFieldType().orElse(null);

            if(fieldType == null)
                return Optional.empty();

            field = this.getField(fieldType, name).orElse(null);
            if (field == null)
                return Optional.empty();

            path.field(field);
        }

        return Optional.of(path.build());
    }

    private Optional<ApiField> getField(Type objectType, String property)
    {
        return this.fieldSupplier.get()
                                 .stream()
                                 .filter(item -> item.getObjectType() == objectType && item.getName().equals(property))
                                 .findAny();
    }

    private Optional<ApiTypeConfig> findType(Type type)
    {
        List<String> aliases = this.getTypeAliases(type);
        Type entityType = this.typeMappings.stream()
                                           .filter(item -> item.getType().equals(type))
                                           .findAny()
                                           .map(ApiTypeMapping::getType)
                                           .map(Type.class::cast)
                                           .orElse(type);

        String[] typeNameParts = entityType.getTypeName().split(" ");
        String typeName = typeNameParts[ typeNameParts.length - 1 ];
        for (ApiTypeConfig config : this.types)
        {
            if (config.getName().equals(typeName))
                return Optional.of(config);

            if (aliases.contains(config.getName()))
                return Optional.of(config);
        }

        return Optional.empty();
    }

    private List<String> getTypeAliases(Type type)
    {
        return this.typeMappings.stream().filter(item -> item.getType() == type).map(ApiTypeMapping::getName).toList();
    }

    private Optional<ApiCapability> findCapability(Type type, String capability)
    {
        return this.findType(type)
                   .map(ApiTypeConfig::getCapabilities)
                   .flatMap(item -> this.findCapability(item, capability))
                   .or(( ) -> this.findCapability(this.capabilities, capability));
    }

    private Optional<ApiCapability> findCapability(List<ApiCapability> capabilities, String capability)
    {
        return capabilities.stream().filter(item -> item.getName().equals(capability)).findAny();
    }

    private Optional<ApiFieldConfig> findFieldConfig(Type type, String name)
    {
        ApiTypeConfig typeConfig = this.findType(type).orElse(null);
        if (typeConfig == null)
            return Optional.empty();

        return typeConfig.getFields().stream().filter(item -> item.getName().equals(name)).findAny();
    }
}
