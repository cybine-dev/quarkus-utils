package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.exception.*;
import de.cybine.quarkus.exception.api.*;
import de.cybine.quarkus.exception.datasource.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;

import java.lang.reflect.*;
import java.util.*;

import static de.cybine.quarkus.util.api.ApiQueryTask.*;

public class ApiQueryConverter implements Converter<ApiQuery, DatasourceQuery>
{
    public static final String TASK_PROPERTY          = "task";
    public static final String CONTEXT_PROPERTY       = "context";
    public static final String ROOT_TYPE_PROPERTY     = "root-type";
    public static final String FIELD_PATH_PROPERTY    = "field-path";
    public static final String API_CONTEXT_PROPERTY   = "api-context";
    public static final String OBJECT_MAPPER_PROPERTY = "object-mapper";

    @Override
    public Class<ApiQuery> getInputType( )
    {
        return ApiQuery.class;
    }

    @Override
    public Class<DatasourceQuery> getOutputType( )
    {
        return DatasourceQuery.class;
    }

    @Override
    public ConverterMetadataBuilder getMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(ApiQueryPagination.class, DatasourcePaginationInfo.class)
                       .withRelation(ApiConditionInfo.class, DatasourceConditionInfo.class)
                       .withRelation(ApiOrderInfo.class, DatasourceOrderInfo.class)
                       .withRelation(ApiRelationInfo.class, DatasourceRelationInfo.class);
    }

    @Override
    public DatasourceQuery convert(ApiQuery input, ConversionHelper helper)
    {
        return DatasourceQuery.builder()
                              .fields(resolveDatasourceFields(input.getFields(), helper))
                              .pagination(helper.toItem(ApiQueryPagination.class, DatasourcePaginationInfo.class)
                                                .map(input::getPagination))
                              .condition(helper.toItem(ApiConditionInfo.class, DatasourceConditionInfo.class)
                                               .map(input::getCondition))
                              .order(helper.toList(ApiOrderInfo.class, DatasourceOrderInfo.class)
                                           .apply(input::getOrder))
                              .relations(helper.toList(ApiRelationInfo.class, DatasourceRelationInfo.class)
                                               .apply(input::getRelations))
                              .build();
    }

    static List<String> resolveDatasourceFields(List<String> fields, ConversionHelper helper)
    {
        List<String> resolvedFields = new ArrayList<>();
        ApiQueryTask task = helper.getContextOrThrow(TASK_PROPERTY);
        ApiQueryContext apiContext = helper.getContextOrThrow(API_CONTEXT_PROPERTY);
        ApiFieldResolverContext context = helper.getContextOrThrow(ApiQueryConverter.CONTEXT_PROPERTY);
        for (String field : fields)
        {
            int steps = field.split("\\.").length;
            if (steps > 1)
                throw new UnknownRelationException(
                        String.format("Cannot traverse multiple elements while fetching fields (%s)", field));

            // TODO: Update to use Scopes
            ApiFieldPath path = ApiQueryConverter.getFieldPathOrThrow(helper, field);
            ApiField apiField = path.getLast();
            if (!context.isAvailable(apiField.getObjectType(), apiField.getName()))
                throw new PropertyUnavailableException(
                        String.format("Field '%s' is not available", path.asString())).addData("path", path.asString());

            List<ApiQueryTask> groupingTasks = List.of(OPTIONS, COUNT);
            if (groupingTasks.contains(task) && !context.hasAnyCapability(apiField.getObjectType(),
                    ApiQuery.GROUP_CAPABILITY, apiField.getName()))
                throw new MissingCapabilityException(String.format("Cannot group by '%s'", path.asString())).addData(
                        "path", path.asString());

            if (path.getLast().getDatasourceField().isRelation())
                throw new UnknownRelationException(String.format("Cannot fetch relation as field (%s)", field));

            ApiFieldNameTranslation translation = path.getTranslation();
            apiContext.addTranslation(translation);
            resolvedFields.add(translation.getTranslation().orElseThrow());
        }

        return resolvedFields;
    }

    static ApiFieldPath getFieldPathOrThrow(ConversionHelper helper)
    {
        return getFieldPathOrThrow(helper, null);
    }

    static ApiFieldPath getFieldPathOrThrow(ConversionHelper helper, String fieldName)
    {
        Type rootType = helper.getContextOrThrow(ApiQueryConverter.ROOT_TYPE_PROPERTY);
        ApiFieldResolverContext context = helper.getContextOrThrow(ApiQueryConverter.CONTEXT_PROPERTY);

        String fieldPath = helper.getContextOrThrow(ApiQueryConverter.FIELD_PATH_PROPERTY);
        if (fieldName != null && !fieldName.isBlank())
            fieldPath = String.format("%s.%s", fieldPath, fieldName);

        ServiceException unknownFieldError = new UnknownApiContextException("Unable to find field");
        unknownFieldError.addData("name", fieldPath)
                         .addData("type", rootType.getTypeName())
                         .addData(ApiQueryConverter.CONTEXT_PROPERTY, context.getContextName());

        return context.findField(rootType, fieldPath).orElseThrow(( ) -> unknownFieldError);
    }
}
