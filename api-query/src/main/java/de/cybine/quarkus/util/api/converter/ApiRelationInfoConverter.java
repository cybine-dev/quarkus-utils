package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.config.*;
import de.cybine.quarkus.exception.datasource.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;
import lombok.*;

@RequiredArgsConstructor
public class ApiRelationInfoConverter implements Converter<ApiRelationInfo, DatasourceRelationInfo>
{
    private final ApiQueryConfig config;

    @Override
    public Class<ApiRelationInfo> getInputType( )
    {
        return ApiRelationInfo.class;
    }

    @Override
    public Class<DatasourceRelationInfo> getOutputType( )
    {
        return DatasourceRelationInfo.class;
    }

    @Override
    public DatasourceRelationInfo convert(ApiRelationInfo input, ConversionHelper helper)
    {
        if (input.getRelations() != null && !input.getRelations().isEmpty() && !this.allowMultiLevelRelations())
            throw new UnknownRelationException("Cannot reference multiple relation levels");

        DatasourceFieldPath path = ApiQueryConverter.getFieldPathOrThrow(helper, input.getProperty());
        if (path.getLength() > 1)
            throw new UnknownRelationException(
                    String.format("Cannot traverse multiple elements while resolving relations (%s)",
                            input.getProperty()));

        helper.withContext(ApiQueryConverter.DATA_TYPE_PROPERTY, path.getLast().getFieldType());

        return DatasourceRelationInfo.builder()
                                     .property(path.asString())
                                     .fetch(input.isFetch())
                                     .condition(helper.toItem(ApiConditionInfo.class, DatasourceConditionInfo.class)
                                                      .map(input::getCondition))
                                     .order(helper.toList(ApiOrderInfo.class, DatasourceOrderInfo.class)
                                                  .apply(input::getOrder))
                                     .relations(helper.toList(ApiRelationInfo.class, DatasourceRelationInfo.class)
                                                      .apply(input::getRelations))
                                     .build();
    }

    private boolean allowMultiLevelRelations( )
    {
        return this.config.allowMultiLevelRelations();
    }
}
