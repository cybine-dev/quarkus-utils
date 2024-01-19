package de.cybine.quarkus.util.converter;

/**
 * Wrapper class for result of conversion operation
 *
 * @param metadata
 *         settings and action-log of the executed conversion
 * @param result
 *         converted item
 * @param <T>
 *         result data-type
 */
public record ConversionResult<T>(ConverterTree metadata, T result)
{ }
