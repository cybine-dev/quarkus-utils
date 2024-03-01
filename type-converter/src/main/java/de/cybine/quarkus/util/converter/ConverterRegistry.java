package de.cybine.quarkus.util.converter;

import de.cybine.quarkus.config.*;
import jakarta.inject.*;
import lombok.*;

import java.util.*;

@Singleton
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class ConverterRegistry
{
    private static final String INPUT_NOT_NULL = "Input type must not be null";
    private static final String OUTPUT_NOT_NULL = "Output type must not be null";

    private final TypeConverterConfig config;

    private final Map<ConverterType<?, ?>, Converter<?, ?>> converters = new HashMap<>();

    /**
     * Register a converter
     *
     * @param converter
     *         converter to register
     */
    public void addConverter(Converter<?, ?> converter)
    {
        this.converters.put(converter.getType(), converter);
    }

    /**
     * Register both converters of an {@link EntityMapper}
     *
     * @param mapper
     *         mapper to register
     */
    public void addEntityMapper(EntityMapper<?, ?> mapper)
    {
        this.addConverter(mapper.toDataConverter());
        this.addConverter(mapper.toEntityConverter());
    }

    /**
     * Creates a helper object for converting item(s) of the given input type {@link I} to the given output type
     * {@link O} with default conversion settings
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
     * @return helper object for converting item(s)
     */
    public <I, O> ConversionProcessor<I, O> getProcessor(Class<I> inputType, Class<O> outputType)
    {
        return this.getProcessor(inputType, outputType, ConverterTree.create(this.getDefaultConstraint()));
    }

    /**
     * Creates a helper object for converting item(s) of the given input type {@link I} to the given output type
     * {@link O} with custom conversion settings
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
     * @return helper object for converting item(s)
     */
    public <I, O> ConversionProcessor<I, O> getProcessor(Class<I> inputType, Class<O> outputType, ConverterTree tree)
    {
        if(inputType == null)
            throw new IllegalArgumentException(INPUT_NOT_NULL);

        if(outputType == null)
            throw new IllegalArgumentException(OUTPUT_NOT_NULL);

        if(tree == null)
            throw new IllegalArgumentException("Converter tree must not be null");

        return this.getProcessor(inputType).withTree(tree).withOutput(outputType);
    }

    public <I> ConverterChain<I> getProcessor(Class<I> inputType)
    {
        if(inputType == null)
            throw new IllegalArgumentException(INPUT_NOT_NULL);

        return ConverterChain.withInput(inputType)
                             .withResolver(this::getConverter)
                             .withTree(ConverterTree.create(this.getDefaultConstraint()));
    }

    @SuppressWarnings("unchecked")
    private <I, O> Converter<I, O> getConverter(ConverterType<I, O> type)
    {
        return (Converter<I, O>) this.converters.get(type);
    }

    public ConverterConstraint getDefaultConstraint( )
    {
        return ConverterConstraint.builder()
                                  .maxDepth(this.config.maxDepth())
                                  .filterNullValues(this.config.filterNullValues())
                                  .allowEmptyCollection(this.config.allowEmptyCollections())
                                  .duplicatePolicy(this.config.duplicatePolicy())
                                  .build();
    }
}
