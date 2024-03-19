package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;

import java.util.*;

public class ApiConditionInfoConverter implements Converter<ApiConditionInfo, DatasourceConditionInfo>
{
    @Override
    public Class<ApiConditionInfo> getInputType( )
    {
        return ApiConditionInfo.class;
    }

    @Override
    public Class<DatasourceConditionInfo> getOutputType( )
    {
        return DatasourceConditionInfo.class;
    }

    @Override
    public ConverterMetadataBuilder getMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(ApiConditionInfo.EvaluationMethod.class,
                               DatasourceConditionInfo.EvaluationMethod.class)
                       .withRelation(ApiConditionDetail.class, DatasourceConditionDetail.class)
                       .withRelation(ApiConditionInfo.class, DatasourceConditionInfo.class);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public DatasourceConditionInfo convert(ApiConditionInfo input, ConversionHelper helper)
    {
        DatasourceConditionInfo.EvaluationMethod type = helper.toItem(ApiConditionInfo.EvaluationMethod.class,
                DatasourceConditionInfo.EvaluationMethod.class).apply(input::getType);

        return DatasourceConditionInfo.builder()
                                      .type(type)
                                      .isInverted(input.isInverted())
                                      .details((Collection) helper.toList(ApiConditionDetail.class,
                                              DatasourceConditionDetail.class).apply(input::getDetails))
                                      .subConditions(
                                              helper.toList(ApiConditionInfo.class, DatasourceConditionInfo.class)
                                                    .apply(input::getSubConditions))
                                      .build();
    }
}
