package de.cybine.quarkus.util.api.converter;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import de.cybine.quarkus.exception.converter.*;
import de.cybine.quarkus.util.*;
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
    private final ObjectMapper objectMapper;

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
    @SuppressWarnings("unchecked")
    public DatasourceConditionDetail convert(ApiConditionDetail input, ConversionHelper helper)
    {

        Type type = input.getType();
        Object value = input.getValue().orElse(null);
        DatasourceFieldPath fieldPath = ApiQueryConverter.getFieldPathOrThrow(helper, input.getProperty());
        DatasourceConditionDetail.Generator builder = DatasourceConditionDetail.builder()
                                                                               .property(fieldPath.asString())
                                                                               .type(type);

        if (value == null)
            return builder.build();

        try
        {
            DatasourceField field = fieldPath.getLast();

            JavaType itemType = field.getItemType();
            if (type.requiresIterable())
                itemType = this.objectMapper.getTypeFactory().constructCollectionType(List.class, field.getItemType());

            value = this.objectMapper.readValue(this.objectMapper.writeValueAsString(value), itemType);
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
