package de.cybine.quarkus.util.api;

import com.fasterxml.jackson.databind.*;
import de.cybine.quarkus.util.api.converter.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;
import io.quarkus.arc.*;
import io.quarkus.security.*;
import lombok.extern.slf4j.*;

import java.util.*;

@Slf4j
public class GenericApiQueryService<E, D> extends GenericDatasourceService<E, D>
{
    private final ApiFieldResolverContext context;

    private final ApiPaginationInfo pagination;

    private final ObjectMapper objectMapper;

    private GenericApiQueryService(Class<E> entityType, Class<D> dataType, ConverterRegistry registry,
            GenericDatasourceRepository<E> repository, ApiFieldResolverContext context, ApiPaginationInfo pagination,
            ObjectMapper objectMapper)
    {
        super(entityType, dataType, registry, repository);
        this.context = context;
        this.pagination = pagination;
        this.objectMapper = objectMapper;
    }

    public List<D> fetch(ApiQuery query)
    {
        if (!this.context.canExecuteAction(this.dataType, "fetch"))
            throw new UnauthorizedException();

        DatasourceQuery datasourceQuery = this.getDatasourceQuery(query);
        List<D> items = this.fetch(datasourceQuery);

        datasourceQuery.getPagination().ifPresent(this::applyPagination);

        return items;
    }

    public Optional<D> fetchSingle(ApiQuery query)
    {
        if (!this.context.canExecuteAction(this.dataType, "fetch_single"))
            throw new UnauthorizedException();

        return this.fetchSingle(this.getDatasourceQuery(query));
    }

    public <O> List<O> fetchOptions(ApiOptionQuery query)
    {
        if (!this.context.canExecuteAction(this.dataType, "options"))
            throw new UnauthorizedException();

        DatasourceQuery datasourceQuery = this.getDatasourceQuery(query);
        List<O> options = this.fetchOptions(datasourceQuery);

        datasourceQuery.getPagination().ifPresent(this::applyPagination);

        return options;
    }

    public List<List<Object>> fetchMultiOptions(ApiOptionQuery query)
    {
        if (!this.context.canExecuteAction(this.dataType, "options"))
            throw new UnauthorizedException();

        DatasourceQuery datasourceQuery = this.getDatasourceQuery(query);
        List<List<Object>> options = this.fetchMultiOptions(datasourceQuery);

        datasourceQuery.getPagination().ifPresent(this::applyPagination);

        return options;
    }

    public List<ApiCountInfo> fetchTotal(ApiCountQuery query)
    {
        if (!this.context.canExecuteAction(this.dataType, "count"))
            throw new UnauthorizedException();

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
        ConverterConstraint constraint = ConverterConstraint.builder().allowEmptyCollection(true).maxDepth(20).build();
        ConverterTree tree = ConverterTree.builder().constraint(constraint).build();

        log.debug("Generating datasource-query from api-query with context '{}'", context);
        return this.registry.getProcessor(type, DatasourceQuery.class, tree)
                            .withContext(ApiQueryConverter.CONTEXT_PROPERTY, this.context)
                            .withContext(ApiQueryConverter.ROOT_TYPE_PROPERTY, this.dataType)
                            .withContext(ApiQueryConverter.FIELD_PATH_PROPERTY, "")
                            .withContext(ApiQueryConverter.OBJECT_MAPPER_PROPERTY, this.objectMapper)
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
        ApiFieldResolverContext context = Arc.container().select(ApiFieldResolverContext.class).get();
        ApiPaginationInfo pagination = Arc.container().select(ApiPaginationInfo.class).get();
        ObjectMapper objectMapper = Arc.container().select(ObjectMapper.class).get();

        GenericDatasourceRepository<E> repository = GenericDatasourceRepository.forType(entityType);

        return new GenericApiQueryService<>(entityType, dataType, converterRegistry, repository, context, pagination,
                objectMapper);
    }
}
