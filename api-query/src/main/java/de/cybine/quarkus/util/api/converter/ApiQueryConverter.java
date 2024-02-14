package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.exception.*;
import de.cybine.quarkus.exception.api.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;

import java.lang.reflect.*;

public class ApiQueryConverter implements Converter<ApiQuery, DatasourceQuery>
{
    public static final String CONTEXT_PROPERTY       = "context";
    public static final String ROOT_TYPE_PROPERTY     = "root-type";
    public static final String FIELD_PATH_PROPERTY    = "field-path";
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
    public DatasourceQuery convert(ApiQuery input, ConversionHelper helper)
    {
        return DatasourceQuery.builder()
                              .pagination(helper.toItem(ApiPaginationInfo.class, DatasourcePaginationInfo.class)
                                                .map(input::getPagination))
                              .condition(helper.toItem(ApiConditionInfo.class, DatasourceConditionInfo.class)
                                               .map(input::getCondition))
                              .order(helper.toList(ApiOrderInfo.class, DatasourceOrderInfo.class)
                                           .apply(input::getOrder))
                              .relations(helper.toList(ApiRelationInfo.class, DatasourceRelationInfo.class)
                                               .apply(input::getRelations))
                              .build();
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
