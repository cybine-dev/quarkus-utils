package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.exception.api.*;
import de.cybine.quarkus.exception.datasource.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;

import java.util.*;

public class ApiCountRelationConverter implements Converter<ApiCountRelationInfo, DatasourceRelationInfo>
{
    @Override
    public Class<ApiCountRelationInfo> getInputType( )
    {
        return ApiCountRelationInfo.class;
    }

    @Override
    public Class<DatasourceRelationInfo> getOutputType( )
    {
        return DatasourceRelationInfo.class;
    }

    @Override
    public ConverterMetadataBuilder getMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(ApiConditionInfo.class, DatasourceConditionInfo.class);
    }

    @Override
    public DatasourceRelationInfo convert(ApiCountRelationInfo input, ConversionHelper helper)
    {
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

        // TODO: Update to use Scopes
        List<String> groupingPaths = new ArrayList<>();
        for (String groupingProperty : input.getGroupingProperties())
        {
            int groupingSteps = groupingProperty.split("\\.").length;
            ApiFieldPath groupingPath = ApiQueryConverter.getFieldPathOrThrow(helper, groupingProperty);

            ApiField groupingField = groupingPath.getLast();
            if (!context.hasAnyCapability(groupingField.getObjectType(), ApiQuery.GROUP_CAPABILITY,
                    groupingField.getName()))
                throw new MissingCapabilityException(
                        String.format("Cannot group by '%s'", groupingPath.asString())).addData("path",
                        groupingPath.asString());

            groupingPaths.add(groupingPath.toDatasourceFieldPath(groupingSteps).asString());
        }

        return DatasourceRelationInfo.builder()
                                     .property(path.toDatasourceFieldPath(steps).asString())
                                     .groupingProperties(groupingPaths)
                                     .condition(helper.toItem(ApiConditionInfo.class, DatasourceConditionInfo.class)
                                                      .map(input::getCondition))
                                     .build();
    }
}
