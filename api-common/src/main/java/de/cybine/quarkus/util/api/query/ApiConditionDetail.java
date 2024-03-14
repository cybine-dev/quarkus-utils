package de.cybine.quarkus.util.api.query;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.*;
import org.eclipse.microprofile.openapi.annotations.media.*;

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

    @Schema(name = "ConditionType")
    public enum Type
    {
        IS_NULL,
        IS_NOT_NULL,
        IS_EQUAL,
        IS_NOT_EQUAL,
        IS_LIKE,
        IS_NOT_LIKE,
        IS_IN,
        IS_NOT_IN,
        IS_PRESENT,
        IS_NOT_PRESENT,
        IS_GREATER,
        IS_GREATER_OR_EQUAL,
        IS_LESS,
        IS_LESS_OR_EQUAL;
    }
}
