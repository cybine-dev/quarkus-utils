package de.cybine.quarkus.util.converter;

import lombok.*;

import java.util.*;

@NoArgsConstructor(staticName = "create")
public class ConverterMetadataBuilder
{
    private final List<ConverterType<?, ?>> relationConverterTypes = new ArrayList<>();

    public ConverterMetadataBuilder withRelation(Class<?> inputType, Class<?> outputType)
    {
        if(inputType == null)
            throw new IllegalArgumentException("Input type must not be null");

        if(outputType == null)
            throw new IllegalArgumentException("Output type must not be null");

        this.relationConverterTypes.add(new ConverterType<>(inputType, outputType));
        return this;
    }

    public ConverterMetadata build( )
    {
        return ConverterMetadata.builder()
                                .relationConverterTypes(Collections.unmodifiableList(this.relationConverterTypes))
                                .build();
    }
}
