package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;

import java.util.*;

public class CountInfoMapper implements EntityMapper<DatasourceCountInfo, ApiCountInfo>
{
    @Override
    public Class<DatasourceCountInfo> getEntityType( )
    {
        return DatasourceCountInfo.class;
    }

    @Override
    public Class<ApiCountInfo> getDataType( )
    {
        return ApiCountInfo.class;
    }

    @Override
    public DatasourceCountInfo toEntity(ApiCountInfo data, ConversionHelper helper)
    {
        ApiQueryContext context = helper.getContextOrThrow(ApiQueryConverter.API_CONTEXT_PROPERTY);
        List<ApiFieldNameTranslation> translations = context.getTranslations();

        Map<String, Object> groupKey = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.getGroupKey().entrySet())
        {
            String key = translations.stream()
                                     .filter(item -> item.getFieldName().equals(entry.getKey()))
                                     .findAny()
                                     .map(ApiFieldNameTranslation::getTranslationOrDefault)
                                     .orElseThrow(( ) -> new NoSuchElementException(
                                             "Unknown field translation: " + entry.getKey()));

            groupKey.put(key, entry.getValue());
        }

        return DatasourceCountInfo.builder().groupKey(groupKey).count(data.getCount()).build();
    }

    @Override
    public ApiCountInfo toData(DatasourceCountInfo entity, ConversionHelper helper)
    {
        ApiQueryContext context = helper.getContextOrThrow(ApiQueryConverter.API_CONTEXT_PROPERTY);
        List<ApiFieldNameTranslation> translations = context.getTranslations();

        Map<String, Object> groupKey = new HashMap<>();
        for (Map.Entry<String, Object> entry : entity.getGroupKey().entrySet())
        {
            String key = translations.stream()
                                     .filter(item -> item.getTranslationOrDefault().equals(entry.getKey()))
                                     .findAny()
                                     .map(ApiFieldNameTranslation::getFieldName)
                                     .orElseThrow(( ) -> new NoSuchElementException(
                                             "Unknown field translation: " + entry.getKey()));

            groupKey.put(key, entry.getValue());
        }

        return ApiCountInfo.builder().groupKey(groupKey).count(entity.getCount()).build();
    }
}
