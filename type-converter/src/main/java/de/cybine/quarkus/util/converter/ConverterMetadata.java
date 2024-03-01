package de.cybine.quarkus.util.converter;

import lombok.*;

import java.util.*;

/**
 * Metadata of a converter used for property introspection and runtime optimizations
 */
@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConverterMetadata
{
    private final List<ConverterType<?, ?>> relationConverterTypes;
}
