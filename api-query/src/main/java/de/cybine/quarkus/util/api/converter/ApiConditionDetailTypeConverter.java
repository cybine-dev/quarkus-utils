package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;

public class ApiConditionDetailTypeConverter
        implements Converter<ApiConditionDetail.Type, DatasourceConditionDetail.Type>
{
    @Override
    public Class<ApiConditionDetail.Type> getInputType( )
    {
        return ApiConditionDetail.Type.class;
    }

    @Override
    public Class<DatasourceConditionDetail.Type> getOutputType( )
    {
        return DatasourceConditionDetail.Type.class;
    }

    @Override
    public DatasourceConditionDetail.Type convert(ApiConditionDetail.Type input, ConversionHelper helper)
    {
        return switch (input)
        {
            case IS_NULL -> DatasourceConditionDetail.Type.IS_NULL;
            case IS_NOT_NULL -> DatasourceConditionDetail.Type.IS_NOT_NULL;
            case IS_EQUAL -> DatasourceConditionDetail.Type.IS_EQUAL;
            case IS_NOT_EQUAL -> DatasourceConditionDetail.Type.IS_NOT_EQUAL;
            case IS_LIKE -> DatasourceConditionDetail.Type.IS_LIKE;
            case IS_NOT_LIKE -> DatasourceConditionDetail.Type.IS_NOT_LIKE;
            case IS_IN -> DatasourceConditionDetail.Type.IS_IN;
            case IS_NOT_IN -> DatasourceConditionDetail.Type.IS_NOT_IN;
            case IS_PRESENT -> DatasourceConditionDetail.Type.IS_PRESENT;
            case IS_NOT_PRESENT -> DatasourceConditionDetail.Type.IS_NOT_PRESENT;
            case IS_GREATER -> DatasourceConditionDetail.Type.IS_GREATER;
            case IS_GREATER_OR_EQUAL -> DatasourceConditionDetail.Type.IS_GREATER_OR_EQUAL;
            case IS_LESS -> DatasourceConditionDetail.Type.IS_LESS;
            case IS_LESS_OR_EQUAL -> DatasourceConditionDetail.Type.IS_LESS_OR_EQUAL;
            default -> DatasourceConditionDetail.Type.valueOf(input.name());
        };
    }
}
