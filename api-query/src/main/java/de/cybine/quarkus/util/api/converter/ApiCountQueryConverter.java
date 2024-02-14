package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.exception.api.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;

import java.util.*;

public class ApiCountQueryConverter implements Converter<ApiCountQuery, DatasourceQuery>
{
    @Override
    public Class<ApiCountQuery> getInputType( )
    {
        return ApiCountQuery.class;
    }

    @Override
    public Class<DatasourceQuery> getOutputType( )
    {
        return DatasourceQuery.class;
    }

    @Override
    public DatasourceQuery convert(ApiCountQuery input, ConversionHelper helper)
    {
        // TODO: Update to use Scopes
        List<String> groupingPaths = new ArrayList<>();
        ApiFieldResolverContext context = helper.getContextOrThrow(ApiQueryConverter.CONTEXT_PROPERTY);
        for (String groupingProperty : input.getGroupingProperties())
        {
            int steps = groupingProperty.split("\\.").length;
            ApiFieldPath path = ApiQueryConverter.getFieldPathOrThrow(helper, groupingProperty);

            ApiField field = path.getLast();
            if (!context.hasAnyCapability(field.getObjectType(), ApiQuery.GROUP_CAPABILITY, field.getName()))
                throw new MissingCapabilityException(String.format("Cannot group by '%s'", path.asString())).addData(
                        "path", path.asString());

            groupingPaths.add(path.toDatasourceFieldPath(steps).asString());
        }

        return DatasourceQuery.builder()
                              .groupingProperties(groupingPaths)
                              .condition(helper.toItem(ApiConditionInfo.class, DatasourceConditionInfo.class)
                                               .apply(input::getCondition))
                              .relations(helper.toList(ApiCountRelationInfo.class, DatasourceRelationInfo.class)
                                               .apply(input::getRelations))
                              .build();
    }
}
