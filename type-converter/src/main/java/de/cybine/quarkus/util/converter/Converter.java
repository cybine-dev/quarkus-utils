package de.cybine.quarkus.util.converter;

/**
 * <p>Interface to define object mappings</p>
 *
 * @param <I>
 *         type of the object to be mapped
 * @param <O>
 *         type of the object that is generated in the mapping process
 */
public interface Converter<I, O>
{
    /**
     * @return type of the object to be mapped
     */
    Class<I> getInputType( );

    /**
     * @return type of the object that is generated in the mapping process
     */
    Class<O> getOutputType( );

    /**
     * @return metadata of the converter for property introspection and runtime optimizations
     */
    default ConverterMetadataBuilder getMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata;
    }

    /**
     * @return compound type of the converter
     */
    default ConverterType<I, O> getType( )
    {
        Class<I> inputType = this.getInputType();
        Class<O> outputType = this.getOutputType();

        assert inputType != null : "No input type present";
        assert outputType != null : "No output type present";

        return new ConverterType<>(inputType, outputType);
    }

    /**
     * <p>Method to perform object mapping</p>
     *
     * @param input
     *         object to be mapped
     * @param helper
     *         helper for mapping of related objects
     *
     * @return mapped object
     */
    O convert(I input, ConversionHelper helper);
}
