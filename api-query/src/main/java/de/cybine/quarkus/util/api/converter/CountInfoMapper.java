package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;

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
        return DatasourceCountInfo.builder().groupKey(data.getGroupKey()).count(data.getCount()).build();
    }

    @Override
    public ApiCountInfo toData(DatasourceCountInfo entity, ConversionHelper helper)
    {
        return ApiCountInfo.builder().groupKey(entity.getGroupKey()).count(entity.getCount()).build();
    }
}
