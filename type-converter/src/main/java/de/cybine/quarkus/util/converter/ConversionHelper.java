package de.cybine.quarkus.util.converter;

import de.cybine.quarkus.exception.converter.*;
import lombok.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * <p>Helper class that is provided in converters to handle hibernate-managed fields and sub-element mapping.</p>
 * <p>Also provides context to allow mapping based on outside-conditions.</p>
 *
 * @see ConverterTree
 * @see ConverterTreeNode
 * @see ConverterResolver
 */
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ConversionHelper
{
    private static final String INPUT_TYPE_NOT_NULL  = "Input type must not be null";
    private static final String OUTPUT_TYPE_NOT_NULL = "Output type must not be null";
    private static final String CONVERTER_NOT_NULL   = "Converter must not be null";
    private static final String SUPPLIER_NOT_NULL    = "Value supplier must not be null";
    private static final String PROPERTY_NOT_NULL    = "Property name must not be null";

    private final ConverterTreeNode parentNode;

    private final ConverterResolver converterResolver;

    private final Map<String, Object> context = new HashMap<>();

    /**
     * Add context information
     *
     * @param property
     *         name of the context information
     * @param value
     *         value of the context information
     *
     * @return current helper instance
     */
    public ConversionHelper withContext(String property, Object value)
    {
        if (property == null)
            throw new IllegalArgumentException(PROPERTY_NOT_NULL);

        this.context.put(property, value);
        return this;
    }

    /**
     * Updates context information
     *
     * @param property
     *         name of the context information
     * @param update
     *         update function
     * @param <T>
     *         type of the context information
     *
     * @return current helper instance
     */
    @SuppressWarnings("unchecked")
    public <T> ConversionHelper updateContext(String property, UnaryOperator<T> update)
    {
        if (property == null)
            throw new IllegalArgumentException(PROPERTY_NOT_NULL);

        if (update == null)
            throw new IllegalArgumentException("Update operator must not be null");

        this.context.put(property, update.apply((T) this.context.get(property)));
        return this;
    }

    /**
     * Search for context information
     *
     * @param property
     *         name of the context information
     * @param <T>
     *         type of the context information
     *
     * @return value of the context information
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> findContext(String property)
    {
        if (property == null)
            throw new IllegalArgumentException(PROPERTY_NOT_NULL);

        return Optional.ofNullable((T) this.context.get(property));
    }

    /**
     * Search for context information and throw exception if not found
     *
     * @param property
     *         name of the context information
     * @param <T>
     *         type of the context information
     *
     * @return value of the context information
     */
    public <T> T getContextOrThrow(String property)
    {
        if (property == null)
            throw new IllegalArgumentException(PROPERTY_NOT_NULL);

        return this.<T>findContext(property).orElseThrow();
    }

    /**
     * @see ConversionHelper#proxyRelation(T)
     */
    public <T> T proxyRelation(Supplier<T> input)
    {
        if (input == null)
            throw new IllegalArgumentException(SUPPLIER_NOT_NULL);

        return this.proxyRelation(input.get());
    }

    /**
     * Check if item is in persistence-context and filter based on lazy-loading state
     *
     * @param input
     *         item to be filtered
     * @param <T>
     *         type of the item
     *
     * @return item if loaded or no persistence-context otherwise default value
     */
    public <T> T proxyRelation(T input)
    {
        if (!this.isInitialized(input))
            return null;

        return input;
    }

    /**
     * Wrap field in an {@link Optional<T>}
     *
     * @param input
     *         data to wrap in an {@link Optional<T>}
     */
    public <T> Optional<T> optional(Supplier<T> input)
    {
        if (input == null)
            throw new IllegalArgumentException(SUPPLIER_NOT_NULL);

        return this.optional(input.get());
    }

    /**
     * Wrap field in an {@link Optional<T>}
     *
     * @param input
     *         data to wrap in an {@link Optional<T>}
     */
    public <T> Optional<T> optional(T input)
    {
        return Optional.ofNullable(input);
    }

    /**
     * Search for a converter and provide a {@link ConverterFunction} for a single item
     *
     * @param inputType
     *         class of input data-type
     * @param outputType
     *         class of output data-type
     * @param <I>
     *         input data-type
     * @param <O>
     *         output data-type
     *
     * @return {@link ConverterFunction}
     *
     * @see ConversionHelper#toItem(Converter)
     */
    public <I, O> ConverterFunction<I, O> toItem(Class<I> inputType, Class<O> outputType)
    {
        if (inputType == null)
            throw new IllegalArgumentException(INPUT_TYPE_NOT_NULL);

        if (outputType == null)
            throw new IllegalArgumentException(OUTPUT_TYPE_NOT_NULL);

        Converter<I, O> converter = this.getConverter(inputType, outputType);
        return this.toItem(converter);
    }

    /**
     * <p>Generate a {@link ConverterFunction} for a single item based on the given converter</p>
     * <p>Filters data according to lazy-loading state when associated to persistence-context</p>
     * <p>Filters data according to mapping-constraints defined in the associated {@link ConverterTree}</p>
     *
     * @param converter
     *         converter used to perform conversion
     * @param <I>
     *         input data-type
     * @param <O>
     *         output data-type
     *
     * @return {@link ConverterFunction}
     */
    public <I, O> ConverterFunction<I, O> toItem(Converter<I, O> converter)
    {
        if (converter == null)
            throw new IllegalArgumentException(CONVERTER_NOT_NULL);

        return input ->
        {
            if (!this.isInitialized(input))
                return null;

            ConverterTreeNode node = this.parentNode.process(input).orElse(null);
            if (node == null)
                return null;

            return converter.convert(input, this.createChildHelper(node));
        };
    }

    /**
     * <p>Search for a converter and provide {@link ConverterFunction} for a collection of items</p>
     * <p>Shortcut for creating a {@link ConverterFunction} for converting a {@link Collection<I>} of items to a
     * {@link List<O>}</p>
     *
     * @param inputType
     *         class of input data-type
     * @param outputType
     *         class of output data-type
     * @param <I>
     *         input data-type
     * @param <O>
     *         output data-type
     *
     * @return {@link ConverterFunction}
     *
     * @see ConversionHelper#toItem(Class, Class)
     * @see ConversionHelper#toCollection(Class, Class, Collection, Collector)
     */
    public <I, O> ConverterFunction<Collection<I>, List<O>> toList(Class<I> inputType, Class<O> outputType)
    {
        if (inputType == null)
            throw new IllegalArgumentException(INPUT_TYPE_NOT_NULL);

        if (outputType == null)
            throw new IllegalArgumentException(OUTPUT_TYPE_NOT_NULL);

        Converter<I, O> converter = this.getConverter(inputType, outputType);
        return this.toList(converter);
    }

    /**
     * <p>Generate a {@link ConverterFunction} for a collection of items based on the given converter</p>
     * <p>Shortcut for creating a {@link ConverterFunction} for converting a {@link Collection<I>} of items to a
     * {@link List<O>}</p>
     *
     * @param converter
     *         converter used to perform conversion
     * @param <I>
     *         input data-type
     * @param <O>
     *         output data-type
     *
     * @return {@link ConverterFunction}
     *
     * @see ConversionHelper#toItem(Converter)
     * @see ConversionHelper#toCollection(Converter, Collection, Collector)
     */
    public <I, O> ConverterFunction<Collection<I>, List<O>> toList(Converter<I, O> converter)
    {
        if (converter == null)
            throw new IllegalArgumentException(CONVERTER_NOT_NULL);

        return this.toCollection(converter, Collections.emptyList(), Collectors.toList());
    }

    /**
     * <p>Search for a converter and provide {@link ConverterFunction} for a collection of items</p>
     * <p>Shortcut for creating a {@link ConverterFunction} for converting a {@link Collection<I>} of items to a
     * {@link Set<O>}</p>
     *
     * @param inputType
     *         class of input data-type
     * @param outputType
     *         class of output data-type
     * @param <I>
     *         input data-type
     * @param <O>
     *         output data-type
     *
     * @return {@link ConverterFunction}
     *
     * @see ConversionHelper#toItem(Class, Class)
     * @see ConversionHelper#toCollection(Class, Class, Collection, Collector)
     */
    public <I, O> ConverterFunction<Collection<I>, Set<O>> toSet(Class<I> inputType, Class<O> outputType)
    {
        if (inputType == null)
            throw new IllegalArgumentException(INPUT_TYPE_NOT_NULL);

        if (outputType == null)
            throw new IllegalArgumentException(OUTPUT_TYPE_NOT_NULL);

        Converter<I, O> converter = this.getConverter(inputType, outputType);
        return this.toSet(converter);
    }

    /**
     * <p>Generate a {@link ConverterFunction} for a collection of items based on the given converter</p>
     * <p>Shortcut for creating a {@link ConverterFunction} for converting a {@link Collection<I>} of items to a
     * {@link Set<O>}</p>
     *
     * @param converter
     *         converter used to perform conversion
     * @param <I>
     *         input data-type
     * @param <O>
     *         output data-type
     *
     * @return {@link ConverterFunction}
     *
     * @see ConversionHelper#toItem(Converter)
     * @see ConversionHelper#toCollection(Converter, Collection, Collector)
     */
    public <I, O> ConverterFunction<Collection<I>, Set<O>> toSet(Converter<I, O> converter)
    {
        if (converter == null)
            throw new IllegalArgumentException(CONVERTER_NOT_NULL);

        return this.toCollection(converter, Collections.emptySet(), Collectors.toSet());
    }

    /**
     * Search for a converter and provide a {@link ConverterFunction} for a collection of items
     *
     * @param inputType
     *         class of input-type
     * @param outputType
     *         class of output-type
     * @param defaultValue
     *         default value to return if constraints are not met
     * @param collector
     *         collector for the stream of converted items
     * @param <I>
     *         input data-type
     * @param <O>
     *         output data-type
     * @param <C>
     *         output collection-type
     *
     * @return {@link ConverterFunction}
     */
    public <I, O, C extends Collection<O>> ConverterFunction<Collection<I>, C> toCollection(Class<I> inputType,
            Class<O> outputType, C defaultValue, Collector<O, ?, C> collector)
    {
        if (inputType == null)
            throw new IllegalArgumentException(INPUT_TYPE_NOT_NULL);

        if (outputType == null)
            throw new IllegalArgumentException(OUTPUT_TYPE_NOT_NULL);

        Converter<I, O> converter = this.getConverter(inputType, outputType);
        return this.toCollection(converter, defaultValue, collector);
    }

    /**
     * Generate a {@link ConverterFunction} for a collection of items based on the given converter
     *
     * @param converter
     *         converter used to perform conversion
     * @param defaultValue
     *         default value to return if constraints are not met
     * @param collector
     *         collector for the stream of converted items
     * @param <I>
     *         input data-type
     * @param <O>
     *         output data-type
     * @param <C>
     *         output collection-type
     *
     * @return {@link ConverterFunction}
     */
    public <I, O, C extends Collection<O>> ConverterFunction<Collection<I>, C> toCollection(Converter<I, O> converter,
            C defaultValue, Collector<O, ?, C> collector)
    {
        if (converter == null)
            throw new IllegalArgumentException(CONVERTER_NOT_NULL);

        if (collector == null)
            throw new IllegalArgumentException("Collector must not be null");

        ConverterFunction<I, O> converterFunction = this.toItem(converter);
        return input ->
        {
            if (!this.isInitialized(input))
                return null;

            ConverterConstraint generalConstraint = this.parentNode.getConstraint();
            ConverterConstraint typeSpecificConstraint = this.parentNode.getConstraint(this.parentNode.getItemType());
            boolean allowEmptyCollection = typeSpecificConstraint.getAllowEmptyCollection()
                                                                 .or(generalConstraint::getAllowEmptyCollection)
                                                                 .orElse(false);

            boolean shouldFilterNullValues = typeSpecificConstraint.getFilterNullValues()
                                                                   .or(generalConstraint::getFilterNullValues)
                                                                   .orElse(true);

            if (!this.parentNode.shouldBeProcessed(input))
                return this.processEmptyCollection(defaultValue, allowEmptyCollection);

            return this.processEmptyCollection(input.stream()
                                                    .map(converterFunction)
                                                    .filter(item -> !shouldFilterNullValues || item != null)
                                                    .collect(collector), allowEmptyCollection);
        };
    }

    private <I, O> Converter<I, O> getConverter(Class<I> inputType, Class<O> outputType)
    {
        Converter<I, O> converter = this.converterResolver.getConverter(inputType, outputType);
        if (converter == null)
            throw new UnknownConverterException(
                    String.format("No converter found for input '%s' and output '%s'", inputType.getSimpleName(),
                            outputType.getSimpleName())).addData("input_type", inputType.getName())
                                                        .addData("output_type", outputType.getName());

        return this.converterResolver.getConverter(inputType, outputType);
    }

    private <T extends Collection<?>> T processEmptyCollection(T collection, boolean allowEmptyCollection)
    {
        assert collection != null : "No collection provided";

        return allowEmptyCollection || !collection.isEmpty() ? collection : null;
    }

    private ConversionHelper createChildHelper(ConverterTreeNode node)
    {
        assert node != null : "No tree-node provided";

        ConversionHelper helper = new ConversionHelper(node, this.converterResolver);
        this.context.forEach(helper::withContext);

        return helper;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isInitialized(Object input)
    {
        try
        {
            Method getPersistenceUtil = Class.forName("jakarta.persistence.Persistence")
                                             .getMethod("getPersistenceUtil");

            Method isLoaded = Class.forName("jakarta.persistence.PersistenceUtil").getMethod("isLoaded", Object.class);

            Object persistenceUtil = getPersistenceUtil.invoke(null);

            return (boolean) isLoaded.invoke(persistenceUtil, input);
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                InvocationTargetException ignored)
        {
            return true;
        }
    }
}
