package de.cybine.quarkus.util.converter;

/**
 * <p>Two way {@link Converter}</p>
 *
 * @param <E>
 *         type of the entity
 * @param <D>
 *         type of the data-object
 *
 * @see Converter
 */
public interface EntityMapper<E, D>
{
    /**
     * @return type of the entity
     */
    Class<E> getEntityType( );

    /**
     * @return type of the data-object
     */
    Class<D> getDataType( );

    /**
     * <p>Method to perform object mapping</p>
     *
     * @param data
     *         object to be mappepd
     * @param helper
     *         helper for mapping of related objects
     *
     * @return mapped object
     */
    E toEntity(D data, ConversionHelper helper);

    /**
     * <p>Method to perform object mapping</p>
     *
     * @param entity
     *         object to be mappepd
     * @param helper
     *         helper for mapping of related objects
     *
     * @return mapped object
     */
    D toData(E entity, ConversionHelper helper);

    /**
     * @return converter for one-way conversion from entity {@link E} to data {@link D}
     */
    default Converter<E, D> toDataConverter( )
    {
        Class<E> entityType = this.getEntityType();
        assert entityType != null : "No entity type provided";

        Class<D> dataType = this.getDataType();
        assert dataType != null : "No data type provided";

        return new GenericConverter<>(entityType, dataType, this::toData);
    }

    /**
     * @return converter for one-way conversion from data {@link D} to entity {@link E}
     */
    default Converter<D, E> toEntityConverter( )
    {
        Class<D> dataType = this.getDataType();
        assert dataType != null : "No data type provided";

        Class<E> entityType = this.getEntityType();
        assert entityType != null : "No entity type provided";

        return new GenericConverter<>(dataType, entityType, this::toEntity);
    }
}
