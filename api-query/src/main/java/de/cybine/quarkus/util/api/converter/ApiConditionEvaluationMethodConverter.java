package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;

public class ApiConditionEvaluationMethodConverter
        implements Converter<ApiConditionInfo.EvaluationMethod, DatasourceConditionInfo.EvaluationMethod>
{
    @Override
    public Class<ApiConditionInfo.EvaluationMethod> getInputType( )
    {
        return ApiConditionInfo.EvaluationMethod.class;
    }

    @Override
    public Class<DatasourceConditionInfo.EvaluationMethod> getOutputType( )
    {
        return DatasourceConditionInfo.EvaluationMethod.class;
    }

    @Override
    public DatasourceConditionInfo.EvaluationMethod convert(ApiConditionInfo.EvaluationMethod input,
            ConversionHelper helper)
    {
        return switch (input)
        {
            case AND -> DatasourceConditionInfo.EvaluationMethod.AND;
            case OR -> DatasourceConditionInfo.EvaluationMethod.OR;
            default -> DatasourceConditionInfo.EvaluationMethod.valueOf(input.name());
        };
    }
}
