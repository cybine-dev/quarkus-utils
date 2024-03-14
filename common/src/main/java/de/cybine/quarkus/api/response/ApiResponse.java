package de.cybine.quarkus.api.response;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.util.*;
import java.util.function.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T>
{
    @JsonIgnore
    @Builder.Default
    private final int statusCode = 200;

    @JsonProperty("value")
    private final T value;

    @With
    @JsonProperty("self")
    private final ApiResourceInfo self;

    @JsonProperty("resources")
    private final List<ApiResourceDefinition> resources;

    @Singular
    @JsonProperty("errors")
    private final List<ApiError> errors;

    public Optional<ApiResourceInfo> getSelf( )
    {
        return Optional.ofNullable(this.self);
    }

    public Optional<List<ApiError>> getErrors( )
    {
        if(this.errors == null || this.errors.isEmpty())
            return Optional.empty();

        return Optional.of(this.errors);
    }

    public <O> O transform(Function<ApiResponse<T>, O> mapper)
    {
        return mapper.apply(this);
    }
}
