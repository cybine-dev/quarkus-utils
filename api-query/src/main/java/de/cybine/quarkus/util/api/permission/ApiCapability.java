package de.cybine.quarkus.util.api.permission;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiCapability
{
    @NotNull
    @NotBlank
    @JsonProperty("name")
    private final String name;

    @JsonProperty("scopes")
    private final List<ApiCapabilityScope> scopes;
}
