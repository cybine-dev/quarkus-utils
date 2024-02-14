package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.exception.api.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;

public class ApiOrderInfoConverter implements Converter<ApiOrderInfo, DatasourceOrderInfo>
{
    @Override
    public Class<ApiOrderInfo> getInputType( )
    {
        return ApiOrderInfo.class;
    }

    @Override
    public Class<DatasourceOrderInfo> getOutputType( )
    {
        return DatasourceOrderInfo.class;
    }

    @Override
    public DatasourceOrderInfo convert(ApiOrderInfo input, ConversionHelper helper)
    {
        int steps = input.getProperty().split("\\.").length;
        helper.updateContext(ApiQueryConverter.FIELD_PATH_PROPERTY,
                path -> String.format("%s.%s", path, input.getProperty()));

        ApiFieldPath path = ApiQueryConverter.getFieldPathOrThrow(helper);

        // TODO: Update to use Scopes
        ApiField field = path.getLast();
        ApiFieldResolverContext context = helper.getContextOrThrow(ApiQueryConverter.CONTEXT_PROPERTY);
        if (!context.hasAnyCapability(field.getObjectType(), ApiQuery.SEARCH_CAPABILITY, field.getName()))
            throw new MissingCapabilityException(String.format("Cannot search for '%s'", path.asString())).addData(
                    "path", path.asString());

        return DatasourceOrderInfo.builder()
                                  .property(path.toDatasourceFieldPath(steps).asString())
                                  .priority(input.getPriority())
                                  .isAscending(input.isAscending())
                                  .build();
    }
}
