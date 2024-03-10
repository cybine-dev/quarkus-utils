package de.cybine.quarkus.util.converter;

import lombok.*;

import java.util.function.*;

@Getter
@RequiredArgsConstructor
public class GenericConverter<I, O> implements Converter<I, O>
{
    private final Class<I> inputType;
    private final Class<O> outputType;

    @Getter(AccessLevel.NONE)
    private final BiFunction<I, ConversionHelper, O> conversionFunction;

    @Getter(AccessLevel.NONE)
    private final Function<ConverterMetadataBuilder, ConverterMetadataBuilder> metadataFunction;

    public GenericConverter(Class<I> inputType, Class<O> outputType,
            BiFunction<I, ConversionHelper, O> conversionFunction)
    {
        this(inputType, outputType, conversionFunction, metadata -> metadata);
    }

    @Override
    public ConverterMetadataBuilder getMetadata(ConverterMetadataBuilder metadata)
    {
        return this.metadataFunction.apply(metadata);
    }

    @Override
    public O convert(I input, ConversionHelper helper)
    {
        if (helper == null)
            throw new IllegalArgumentException("No helper provided");

        return this.conversionFunction.apply(input, helper);
    }
}
