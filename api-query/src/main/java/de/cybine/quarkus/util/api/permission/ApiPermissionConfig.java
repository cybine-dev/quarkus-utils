package de.cybine.quarkus.util.api.permission;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiPermissionConfig
{
    @Valid
    @Singular
    @JsonProperty("type_mappings")
    private final List<ApiTypeMapping> typeMappings;

    @Valid
    @Singular
    @JsonProperty("context_mappings")
    private final List<ApiContextMapping> contextMappings;

    @Singular
    @JsonProperty("available_actions")
    private final List<String> availableActions;

    @Valid
    @Singular
    @JsonProperty("capabilities")
    private final List<ApiCapability> capabilities;

    @Valid
    @Singular
    @JsonProperty("types")
    private final List<ApiTypeConfig> types;

    @Valid
    @Singular
    @JsonProperty("contexts")
    private final List<ApiContextConfig> contexts;

    public Optional<List<ApiTypeMapping>> getTypeMappings( )
    {
        return Optional.ofNullable(this.typeMappings);
    }

    public Optional<List<ApiContextMapping>> getContextMappings( )
    {
        return Optional.ofNullable(this.contextMappings);
    }

    public Optional<List<String>> getAvailableActions( )
    {
        return Optional.ofNullable(this.availableActions);
    }

    public Optional<List<ApiCapability>> getCapabilities( )
    {
        return Optional.ofNullable(this.capabilities);
    }

    public Optional<List<ApiTypeConfig>> getTypes( )
    {
        return Optional.ofNullable(this.types);
    }

    public Optional<List<ApiContextConfig>> getContexts( )
    {
        return Optional.ofNullable(this.contexts);
    }
}
