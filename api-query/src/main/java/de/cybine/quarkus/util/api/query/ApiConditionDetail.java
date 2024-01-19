package de.cybine.quarkus.util.api.query;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.util.datasource.DatasourceConditionDetail.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiConditionDetail
{
    @With
    @NotNull
    @JsonProperty("property")
    private final String property;

    @NotNull
    @JsonProperty("type")
    private final Type type;

    @JsonProperty("value")
    private final Object value;

    public Optional<Object> getValue( )
    {
        return Optional.ofNullable(this.value);
    }
}
