package de.cybine.quarkus.util.converter;

import de.cybine.quarkus.util.*;
import lombok.*;

import java.util.*;
import java.util.stream.*;

/**
 * Helper class to initiate item conversion
 *
 * @param <I>
 *         input data-type
 * @param <O>
 *         output data-type
 *
 * @see ConversionHelper
 */
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ConversionProcessor<I, O>
{
    private static final String TYPES_NOT_EMPTY = "Converter types is empty";
    private static final String TYPE_NOT_PRESENT = "No converter type present";

    private final ConverterTree     metadata;
    private final ConverterResolver converterResolver;

    private final List<ConverterType<?, ?>> types;

    private final List<BiTuple<String, Object>> context = new ArrayList<>();

    /**
     * Add copntext information
     *
     * @param property
     *         name of the context information
     * @param value
     *         value of the context information
     *
     * @return current processor instance
     */
    public ConversionProcessor<I, O> withContext(String property, Object value)
    {
        this.context.add(new BiTuple<>(property, value));
        return this;
    }

    /**
     * Convert a single item
     *
     * @param input
     *         item to convert
     *
     * @return {@link ConversionResult}
     *
     * @see ConversionHelper#toItem(Class, Class)
     */
    @SuppressWarnings("unchecked")
    public ConversionResult<O> toItem(I input)
    {
        assert !this.types.isEmpty() : TYPES_NOT_EMPTY;

        Object result = null;
        for (ConverterType<?, ?> type : this.types)
            result = this.toItem(type, result != null ? result : input);

        return new ConversionResult<>(this.metadata, (O) result);
    }

    @SuppressWarnings("unchecked")
    private <T> Object toItem(ConverterType<T, ?> type, Object item)
    {
        assert type != null : TYPE_NOT_PRESENT;

        ConversionHelper helper = this.createConversionHelper();
        return helper.toItem(type.inputType(), type.outputType()).apply((T) item);
    }

    /**
     * Convert a collection of items
     *
     * @param input
     *         collection of items to convert
     *
     * @return {@link ConversionResult}
     *
     * @see ConversionProcessor#toCollection(Collection, Collection, Collector)
     * @see ConversionHelper#toList(Class, Class)
     */
    @SuppressWarnings("unchecked")
    public ConversionResult<List<O>> toList(Collection<I> input)
    {
        assert !this.types.isEmpty() : TYPES_NOT_EMPTY;

        List<?> result = null;
        for (ConverterType<?, ?> type : this.types)
            result = this.toList(type, result != null ? result : input);

        return new ConversionResult<>(this.metadata, (List<O>) result);
    }

    @SuppressWarnings("unchecked")
    private <T> List<?> toList(ConverterType<T, ?> type, Collection<?> input)
    {
        assert type != null : TYPE_NOT_PRESENT;

        ConversionHelper helper = this.createConversionHelper();
        return helper.toList(type.inputType(), type.outputType()).apply((Collection<T>) input);
    }

    /**
     * Convert a collection of items
     *
     * @param input
     *         collection of items to convert
     *
     * @return {@link ConversionResult}
     *
     * @see ConversionProcessor#toCollection(Collection, Collection, Collector)
     * @see ConversionHelper#toSet(Class, Class)
     */
    @SuppressWarnings("unchecked")
    public ConversionResult<Set<O>> toSet(Collection<I> input)
    {
        assert !this.types.isEmpty() : TYPES_NOT_EMPTY;

        Set<?> result = null;
        for (ConverterType<?, ?> type : this.types)
            result = this.toSet(type, result != null ? result : input);

        return new ConversionResult<>(this.metadata, (Set<O>) result);
    }

    @SuppressWarnings("unchecked")
    private <T> Set<?> toSet(ConverterType<T, ?> type, Collection<?> input)
    {
        assert type != null : TYPE_NOT_PRESENT;

        ConversionHelper helper = this.createConversionHelper();
        return helper.toSet(type.inputType(), type.outputType()).apply((Collection<T>) input);
    }

    /**
     * Convert a collection of items
     *
     * @param input
     *         collection of items to convert
     * @param defaultValue
     *         default value to return if constraints are not met
     * @param collector
     *         collector for the stream of converted items
     * @param <C>
     *         output collection-type
     *
     * @return {@link ConversionResult}
     */
    @SuppressWarnings("unchecked")
    public <C extends Collection<O>> ConversionResult<C> toCollection(Collection<I> input, C defaultValue,
            Collector<O, ?, C> collector)
    {
        assert !this.types.isEmpty() : TYPES_NOT_EMPTY;

        Collection<?> result = null;
        for (ConverterType<?, ?> type : this.types)
            result = this.toCollection(type, result != null ? result : input, defaultValue, collector);

        return new ConversionResult<>(this.metadata, (C) result);
    }

    @SuppressWarnings("unchecked")
    private <T, S, C extends Collection<S>> C toCollection(ConverterType<T, S> type, Collection<?> input,
            Object defaultValue, Collector<?, ?, ?> collector)
    {
        assert type != null : TYPE_NOT_PRESENT;
        assert collector != null : "No collector provided";

        ConversionHelper helper = this.createConversionHelper();
        return helper.toCollection(type.inputType(), type.outputType(), (C) defaultValue,
                (Collector<S, ?, C>) collector).apply((Collection<T>) input);
    }

    private ConversionHelper createConversionHelper( )
    {
        ConversionHelper helper = new ConversionHelper(this.metadata.getRootNode(), this.converterResolver);
        this.context.forEach(item -> helper.withContext(item.first(), item.second()));

        return helper;
    }
}
