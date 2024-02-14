package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.config.*;
import de.cybine.quarkus.exception.api.*;
import de.cybine.quarkus.exception.datasource.*;
import de.cybine.quarkus.util.api.*;
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

        int steps = input.getProperty().split("\\.").length;
        helper.updateContext(ApiQueryConverter.FIELD_PATH_PROPERTY,
                path -> String.format("%s.%s", path, input.getProperty()));

        if (steps > 1)
            throw new UnknownRelationException(
                    String.format("Cannot traverse multiple elements while resolving relations (%s)",
                            input.getProperty()));

        ApiFieldPath path = ApiQueryConverter.getFieldPathOrThrow(helper);

        // TODO: Update to use Scopes
        ApiField field = path.getLast();
        ApiFieldResolverContext context = helper.getContextOrThrow(ApiQueryConverter.CONTEXT_PROPERTY);
        if (!context.isAvailable(field.getObjectType(), field.getName()))
            throw new PropertyUnavailableException(
                    String.format("Property '%s' is not available", path.asString())).addData("path", path.asString());

        return DatasourceRelationInfo.builder()
                                     .property(path.toDatasourceFieldPath(steps).asString())
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
