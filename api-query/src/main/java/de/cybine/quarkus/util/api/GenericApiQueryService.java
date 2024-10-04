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

import static de.cybine.quarkus.util.api.ApiQueryTask.*;

@Slf4j
public class GenericApiQueryService<E, D> extends GenericDatasourceService<E, D>
{
    private final ApiFieldResolverContext context;

    private final ApiQueryContext apiContext;

    private final ObjectMapper objectMapper;

    private GenericApiQueryService(Class<E> entityType, Class<D> dataType, ConverterRegistry registry,
            GenericDatasourceRepository<E> repository, ApiFieldResolverContext context, ApiQueryContext apiContext,
            ObjectMapper objectMapper)
    {
        super(entityType, dataType, registry, repository);
        this.context = context;
        this.apiContext = apiContext;
        this.objectMapper = objectMapper;
    }

    public List<D> fetch(ApiQuery query)
    {
        DatasourceQuery datasourceQuery = this.getDatasourceQuery(FETCH, query);
        List<D> items = this.fetch(datasourceQuery);

        datasourceQuery.getPagination().ifPresent(this::applyPagination);

        return items;
    }

    public Optional<D> fetchSingle(ApiQuery query)
    {
        return this.fetchSingle(this.getDatasourceQuery(FETCH_SINGLE, query));
    }

    public List<Map<String, Object>> fetchOptions(ApiQuery query)
    {
        DatasourceQuery datasourceQuery = this.getDatasourceQuery(OPTIONS, query);
        List<Map<String, Object>> options = new ArrayList<>();
        for (Map<String, Object> option : this.fetchOptions(datasourceQuery))
        {
            Map<String, Object> translatedOption = new HashMap<>();
            for (Map.Entry<String, Object> entry : option.entrySet())
            {
                String key = this.apiContext.getTranslations()
                                            .stream()
                                            .filter(item -> item.getTranslationOrDefault().equals(entry.getKey()))
                                            .findAny()
                                            .map(ApiFieldNameTranslation::getFieldName)
                                            .orElseThrow(( ) -> new NoSuchElementException(
                                                    "Unknown field translation: " + entry.getKey()));

                translatedOption.put(key, entry.getValue());
            }

            options.add(translatedOption);
        }

        datasourceQuery.getPagination().ifPresent(this::applyPagination);

        return options;
    }

    public List<ApiCountInfo> fetchTotal(ApiQuery query)
    {
        return this.registry.getProcessor(DatasourceCountInfo.class, ApiCountInfo.class)
                            .withContext(ApiQueryConverter.API_CONTEXT_PROPERTY, this.apiContext)
                            .toList(this.fetchTotal(this.getDatasourceQuery(COUNT, query)))
                            .result();
    }

    private DatasourceQuery getDatasourceQuery(ApiQueryTask task, ApiQuery query)
    {
        if (!this.context.canExecuteAction(this.dataType, task.getAction()))
            throw new UnauthorizedException();

        ConverterConstraint constraint = ConverterConstraint.builder().allowEmptyCollection(true).maxDepth(20).build();
        ConverterTree tree = ConverterTree.builder().constraint(constraint).build();

        log.debug("Generating datasource-query from api-query with context '{}'", context);
        return this.registry.getProcessor(ApiQuery.class, DatasourceQuery.class, tree)
                            .withContext(ApiQueryConverter.TASK_PROPERTY, task)
                            .withContext(ApiQueryConverter.CONTEXT_PROPERTY, this.context)
                            .withContext(ApiQueryConverter.ROOT_TYPE_PROPERTY, this.dataType)
                            .withContext(ApiQueryConverter.FIELD_PATH_PROPERTY, "")
                            .withContext(ApiQueryConverter.OBJECT_MAPPER_PROPERTY, this.objectMapper)
                            .withContext(ApiQueryConverter.API_CONTEXT_PROPERTY, this.apiContext)
                            .toItem(query)
                            .result();
    }

    private void applyPagination(DatasourcePaginationInfo pagination)
    {
        pagination.getSize().ifPresent(this.apiContext.getPaginationInfo()::setSize);
        pagination.getOffset().ifPresent(this.apiContext.getPaginationInfo()::setOffset);
        pagination.getTotal().ifPresent(this.apiContext.getPaginationInfo()::setTotal);
    }

    public static <E, D> GenericApiQueryService<E, D> forType(Class<E> entityType, Class<D> dataType)
    {
        ConverterRegistry converterRegistry = Arc.container().select(ConverterRegistry.class).get();
        ApiFieldResolverContext context = Arc.container().select(ApiFieldResolverContext.class).get();
        ApiQueryContext apiContext = Arc.container().select(ApiQueryContext.class).get();
        ObjectMapper objectMapper = Arc.container().select(ObjectMapper.class).get();

        GenericDatasourceRepository<E> repository = GenericDatasourceRepository.forType(entityType);

        return new GenericApiQueryService<>(entityType, dataType, converterRegistry, repository, context, apiContext,
                objectMapper);
    }
}
