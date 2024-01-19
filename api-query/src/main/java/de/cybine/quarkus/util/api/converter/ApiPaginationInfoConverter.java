package de.cybine.quarkus.util.api.converter;

import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;

public class ApiPaginationInfoConverter implements Converter<ApiPaginationInfo, DatasourcePaginationInfo>
{
    @Override
    public Class<ApiPaginationInfo> getInputType( )
    {
        return ApiPaginationInfo.class;
    }

    @Override
    public Class<DatasourcePaginationInfo> getOutputType( )
    {
        return DatasourcePaginationInfo.class;
    }

    @Override
    public DatasourcePaginationInfo convert(ApiPaginationInfo input, ConversionHelper helper)
    {
        return DatasourcePaginationInfo.builder()
                                       .size(input.getSize().orElse(null))
                                       .offset(input.getOffset().orElse(null))
                                       .includeTotal(input.includeTotal())
                                       .build();
    }
}
