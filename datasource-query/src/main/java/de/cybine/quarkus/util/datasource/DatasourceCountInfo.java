package de.cybine.quarkus.util.datasource;

import lombok.*;

import java.util.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasourceCountInfo
{
    @Singular("key")
    private final Map<String, Object> groupKey;

    private final long count;
}
