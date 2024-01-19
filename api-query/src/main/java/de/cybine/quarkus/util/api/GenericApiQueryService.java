package de.cybine.quarkus.util.api;

import de.cybine.quarkus.util.api.converter.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;
import io.quarkus.arc.*;
import lombok.extern.slf4j.*;

import java.util.*;

@Slf4j
public class GenericApiQueryService<E, D> extends GenericDatasourceService<E, D>
{
    private final ApiFieldResolver  fieldResolver;
    private final ApiPaginationInfo pagination;

    private GenericApiQueryService(Class<E> entityType, Class<D> dataType, ConverterRegistry registry,
            GenericDatasourceRepository<E> repository, ApiFieldResolver fieldResolver, ApiPaginationInfo pagination)
    {
        super(entityType, dataType, registry, repository);
        this.fieldResolver = fieldResolver;
        this.pagination = pagination;
    }

    public List<D> fetch(ApiQuery query)
    {
        DatasourceQuery datasourceQuery = this.getDatasourceQuery(query);
        List<D> items = this.fetch(datasourceQuery);

        datasourceQuery.getPagination().ifPresent(this::applyPagination);

        return items;
    }

    public Optional<D> fetchSingle(ApiQuery query)
    {
        return this.fetchSingle(this.getDatasourceQuery(query));
    }

    public <O> List<O> fetchOptions(ApiOptionQuery query)
    {
        DatasourceQuery datasourceQuery = this.getDatasourceQuery(query);
        List<O> options = this.fetchOptions(datasourceQuery);

        datasourceQuery.getPagination().ifPresent(this::applyPagination);

        return options;
    }

    public List<List<Object>> fetchMultiOptions(ApiOptionQuery query)
    {
        DatasourceQuery datasourceQuery = this.getDatasourceQuery(query);
        List<List<Object>> options = this.fetchMultiOptions(datasourceQuery);

        datasourceQuery.getPagination().ifPresent(this::applyPagination);

        return options;
    }

    public List<ApiCountInfo> fetchTotal(ApiCountQuery query)
    {
        return this.registry.getProcessor(DatasourceCountInfo.class, ApiCountInfo.class)
                            .toList(this.fetchTotal(this.getDatasourceQuery(query)))
                            .result();
    }

    private DatasourceQuery getDatasourceQuery(ApiQuery query)
    {
        return this.getDatasourceQuery(ApiQuery.class, query);
    }

    private DatasourceQuery getDatasourceQuery(ApiOptionQuery query)
    {
        return this.getDatasourceQuery(ApiOptionQuery.class, query);
    }

    private DatasourceQuery getDatasourceQuery(ApiCountQuery query)
    {
        return this.getDatasourceQuery(ApiCountQuery.class, query);
    }

    private <T> DatasourceQuery getDatasourceQuery(Class<T> type, T query)
    {
        return this.getDatasourceQuery(type, query, this.fieldResolver.getUserContext().getContextName());
    }

    private <T> DatasourceQuery getDatasourceQuery(Class<T> type, T query, String context)
    {
        ConverterConstraint constraint = ConverterConstraint.builder().allowEmptyCollection(true).maxDepth(20).build();
        ConverterTree tree = ConverterTree.builder().constraint(constraint).build();

        log.debug("Generating datasource-query from api-query with context '{}'", context);
        return this.registry.getProcessor(type, DatasourceQuery.class, tree)
                            .withContext(ApiQueryConverter.CONTEXT_PROPERTY, context)
                            .withContext(ApiQueryConverter.DATA_TYPE_PROPERTY, this.dataType)
                            .toItem(query)
                            .result();
    }

    private void applyPagination(DatasourcePaginationInfo pagination)
    {
        pagination.getSize().ifPresent(this.pagination::setSize);
        pagination.getOffset().ifPresent(this.pagination::setOffset);
        pagination.getTotal().ifPresent(this.pagination::setTotal);
    }

    public static <E, D> GenericApiQueryService<E, D> forType(Class<E> entityType, Class<D> dataType)
    {
        ConverterRegistry converterRegistry = Arc.container().select(ConverterRegistry.class).get();
        ApiFieldResolver resolver = Arc.container().select(ApiFieldResolver.class).get();
        ApiPaginationInfo pagination = Arc.container().select(ApiPaginationInfo.class).get();

        GenericDatasourceRepository<E> repository = GenericDatasourceRepository.forType(entityType);

        return new GenericApiQueryService<>(entityType, dataType, converterRegistry, repository, resolver, pagination);
    }
}
