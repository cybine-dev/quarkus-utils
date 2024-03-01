package de.cybine.quarkus.util.converter;

/**
 * Helper class to define method to retrieve {@link Converter}
 */
public interface ConverterResolver
{
    <I, O> Converter<I, O> getConverter(ConverterType<I, O> type);

    default <I, O> Converter<I, O> getConverter(Class<I> inputType, Class<O> outputType)
    {
        if(inputType == null)
            throw new IllegalArgumentException("Input type must not be null");

        if(outputType == null)
            throw new IllegalArgumentException("Output type must not be null");

        return this.getConverter(new ConverterType<>(inputType, outputType));
    }
}
