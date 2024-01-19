package de.cybine.quarkus.api.response;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.util.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiError
{
    @JsonProperty("message")
    private final String message;

    @JsonProperty("code")
    private final String code;

    @Singular("data")
    @JsonProperty("data")
    private final Map<String, Object> data;
}
