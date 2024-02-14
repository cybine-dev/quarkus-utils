package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.exception.api.*;
import de.cybine.quarkus.exception.datasource.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;

public class ApiOptionQueryConverter implements Converter<ApiOptionQuery, DatasourceQuery>
{
    @Override
    public Class<ApiOptionQuery> getInputType( )
    {
        return ApiOptionQuery.class;
    }

    @Override
    public Class<DatasourceQuery> getOutputType( )
    {
        return DatasourceQuery.class;
    }

    @Override
    public DatasourceQuery convert(ApiOptionQuery input, ConversionHelper helper)
    {
        int steps = input.getProperty().split("\\.").length;
        helper.updateContext(ApiQueryConverter.FIELD_PATH_PROPERTY,
                path -> String.format("%s.%s", path, input.getProperty()));

        ApiFieldPath path = ApiQueryConverter.getFieldPathOrThrow(helper);
        if (steps > 1)
            throw new UnknownRelationException(
                    String.format("Cannot traverse multiple elements while fetching options for field (%s)",
                            input.getProperty()));

        // TODO: Update to use Scopes
        ApiField field = path.getLast();
        ApiFieldResolverContext context = helper.getContextOrThrow(ApiQueryConverter.CONTEXT_PROPERTY);
        if (!context.isAvailable(field.getObjectType(), field.getName()))
            throw new PropertyUnavailableException(
                    String.format("Property '%s' is not available", path.asString())).addData("path", path.asString());

        if (path.getLast().getDatasourceField().isRelation())
            throw new UnknownRelationException(
                    String.format("Cannot fetch options for relation (%s)", input.getProperty()));

        return DatasourceQuery.builder()
                              .property(path.toDatasourceFieldPath(steps).asString())
                              .pagination(helper.toItem(ApiPaginationInfo.class, DatasourcePaginationInfo.class)
                                                .map(input::getPagination))
                              .condition(helper.toItem(ApiConditionInfo.class, DatasourceConditionInfo.class)
                                               .map(input::getCondition))
                              .build();
    }
}
