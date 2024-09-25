package de.cybine.quarkus.util.api.secret;

import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;
import java.util.concurrent.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SecretData
{
    private final String userId;

    @Singular
    private final Map<String, Object> properties = new ConcurrentHashMap<>();

    public Optional<String> getUserId( )
    {
        return Optional.ofNullable(this.userId);
    }
}
