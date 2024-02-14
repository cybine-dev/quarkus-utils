package de.cybine.quarkus.util.api.response;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.api.response.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.RestResponse.*;

import java.net.*;
import java.util.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T>
{
    @JsonIgnore
    @Builder.Default
    private final Status status = Status.OK;

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

    @JsonIgnore
    public ResponseBuilder<ApiResponse<T>> toResponseBuilder( )
    {
        ResponseBuilder<ApiResponse<T>> builder = ResponseBuilder.create(this.status, this);

        this.getSelf().map(ApiResourceInfo::getHref).map(URI::create).ifPresent(builder::location);

        return builder;
    }

    @JsonIgnore
    public RestResponse<ApiResponse<T>> toResponse( )
    {
        return this.toResponseBuilder().build();
    }
}
