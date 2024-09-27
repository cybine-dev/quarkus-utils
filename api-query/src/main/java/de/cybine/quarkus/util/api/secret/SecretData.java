package de.cybine.quarkus.util.api.secret;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
public class SecretData
{
    @JsonProperty("user_id")
    private final String userId;

    @Singular
    @JsonProperty("properties")
    private final Map<String, Object> properties;

    @Jacksonized
    @Builder(builderClassName = "Generator")
    private SecretData(String userId, Map<String, Object> properties)
    {
        this.userId = userId;
        this.properties = properties == null ? new HashMap<>() : properties;
    }

    public Optional<String> getUserId( )
    {
        return Optional.ofNullable(this.userId);
    }
}
