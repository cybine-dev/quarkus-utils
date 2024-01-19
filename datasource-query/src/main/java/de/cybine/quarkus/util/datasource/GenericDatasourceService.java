package de.cybine.quarkus.util.datasource;

import de.cybine.quarkus.util.converter.*;
import io.quarkus.arc.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.util.*;

@Slf4j
@SuppressWarnings("unused")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class GenericDatasourceService<E, D>
{
    protected final Class<E> entityType;
    protected final Class<D> dataType;

    protected final ConverterRegistry registry;

    protected final GenericDatasourceRepository<E> repository;

    public List<D> fetch(DatasourceQuery query)
    {
        List<E> entities = this.fetchEntities(query);
        return this.getToDataProcessor().toList(entities).result();
    }

    public List<E> fetchEntities(DatasourceQuery query)
    {
        return this.repository.fetch(query);
    }

    public Optional<D> fetchSingle(DatasourceQuery query)
    {
        return this.fetchSingleEntity(query).map(this.getToDataProcessor()::toItem).map(ConversionResult::result);
    }

    public Optional<E> fetchSingleEntity(DatasourceQuery query)
    {
        return this.repository.fetchSingle(query);
    }

    public <O> List<O> fetchOptions(DatasourceQuery query)
    {
        return this.repository.fetchOptions(query);
    }

    public List<List<Object>> fetchMultiOptions(DatasourceQuery query)
    {
        return this.repository.fetchMultiOptions(query);
    }

    public List<DatasourceCountInfo> fetchTotal(DatasourceQuery query)
    {
        return this.repository.fetchTotal(query);
    }

    protected ConversionProcessor<E, D> getToDataProcessor( )
    {
        return this.registry.getProcessor(this.entityType, this.dataType);
    }

    public static <E, D> GenericDatasourceService<E, D> forType(Class<E> entityType, Class<D> dataType)
    {
        ConverterRegistry registry = Arc.container().select(ConverterRegistry.class).get();
        GenericDatasourceRepository<E> repository = GenericDatasourceRepository.forType(entityType);

        return new GenericDatasourceService<>(entityType, dataType, registry, repository);
    }
}
