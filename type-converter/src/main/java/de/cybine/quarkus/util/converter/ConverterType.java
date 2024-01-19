package de.cybine.quarkus.util.converter;

/**
 * <p>Class to define input and output type of a converter</p>
 */
public record ConverterType<I, O>(Class<I> inputType, Class<O> outputType)
{ }
