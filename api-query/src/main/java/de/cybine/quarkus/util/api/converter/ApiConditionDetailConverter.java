package de.cybine.quarkus.util.api.converter;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import de.cybine.quarkus.exception.api.*;
import de.cybine.quarkus.exception.converter.*;
import de.cybine.quarkus.util.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;
import de.cybine.quarkus.util.datasource.DatasourceConditionDetail.*;
import lombok.*;

import java.util.*;

@RequiredArgsConstructor
@SuppressWarnings("rawtypes")
public class ApiConditionDetailConverter implements Converter<ApiConditionDetail, DatasourceConditionDetail>
{
    @Override
    public Class<ApiConditionDetail> getInputType( )
    {
        return ApiConditionDetail.class;
    }

    @Override
    public Class<DatasourceConditionDetail> getOutputType( )
    {
        return DatasourceConditionDetail.class;
    }

    @Override
    public ConverterMetadataBuilder getMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(ApiConditionDetail.Type.class, DatasourceConditionDetail.Type.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DatasourceConditionDetail convert(ApiConditionDetail input, ConversionHelper helper)
    {
        int steps = input.getProperty().split("\\.").length;
        helper.updateContext(ApiQueryConverter.FIELD_PATH_PROPERTY,
                path -> String.format("%s.%s", path, input.getProperty()));

        Type type = helper.toItem(ApiConditionDetail.Type.class, DatasourceConditionDetail.Type.class)
                          .apply(input::getType);

        ApiFieldPath path = ApiQueryConverter.getFieldPathOrThrow(helper);
        DatasourceConditionDetail.Generator builder = DatasourceConditionDetail.builder()
                                                                               .property(
                                                                                       path.toDatasourceFieldPath(steps)
                                                                                           .asString())
                                                                               .type(type);

        // TODO: Update to use Scopes
        ApiField field = path.getLast();
        ApiFieldResolverContext context = helper.getContextOrThrow(ApiQueryConverter.CONTEXT_PROPERTY);
        if (!context.hasAnyCapability(field.getObjectType(), ApiQuery.SEARCH_CAPABILITY, field.getName()))
            throw new MissingCapabilityException(String.format("Cannot search for '%s'", path.asString())).addData(
                    "path", path.asString());

        Object value = input.getValue().orElse(null);
        if (value == null)
            return builder.build();

        try
        {
            DatasourceField datasourceField = field.getDatasourceField();

            JavaType itemType = datasourceField.getItemType();
            ObjectMapper mapper = helper.getContextOrThrow(ApiQueryConverter.OBJECT_MAPPER_PROPERTY);
            if (type.requiresIterable())
                itemType = mapper.getTypeFactory().constructCollectionType(List.class, datasourceField.getItemType());

            value = mapper.readValue(mapper.writeValueAsString(value), itemType);
            if (value instanceof WithDatasourceKey<?> datasourceKey)
                value = datasourceKey.getDatasourceKey();

            if (value instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof WithDatasourceKey<?>)
                value = list.stream()
                            .map(WithDatasourceKey.class::cast)
                            .map(WithDatasourceKey::getDatasourceKey)
                            .toList();

            return builder.value(value).build();
        }
        catch (JsonProcessingException exception)
        {
            throw new EntityConversionException(exception);
        }
    }
}
