package de.cybine.quarkus.api.v1.asset;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.data.library.*;
import de.cybine.quarkus.data.rental.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;

import java.util.*;

import static de.cybine.quarkus.data.asset.AssetEntity_.*;

@Startup
@RequiredArgsConstructor
public class AssetResource implements AssetApi
{
    // @formatter:off
    private final GenericApiQueryService<AssetEntity, Asset> service =
            GenericApiQueryService.forType(AssetEntity.class, Asset.class);
    // @formatter:on

    private final ApiFieldResolver resolver;

    @PostConstruct
    void setup( )
    {
        this.resolver.registerType(Asset.class)
                     .withField("id", ID)
                     .withField("external_id", EXTERNAL_ID)
                     .withField("library_id", LIBRARY_ID)
                     .withRelation("library", LIBRARY, Library.class)
                     .withField("name", NAME)
                     .withField("description", DESCRIPTION)
                     .withRelation("rentals", RENTALS, Rental.class);
    }

    @Override
    public RestResponse<ApiResponse<List<Asset>>> fetch(ApiQuery query)
    {
        if (query == null)
            query = ApiQuery.builder().build();

        return ApiResponse.<List<Asset>>builder()
                          .value(this.service.fetch(query))
                          .build()
                          .transform(ApiQueryHelper::createResponse);
    }

    @Override
    public RestResponse<ApiResponse<Asset>> fetchSingle(ApiQuery query)
    {
        if (query == null)
            query = ApiQuery.builder().build();

        return ApiResponse.<Asset>builder()
                          .value(this.service.fetchSingle(query).orElseThrow())
                          .build()
                          .transform(ApiQueryHelper::createResponse);
    }

    @Override
    public RestResponse<ApiResponse<List<ApiCountInfo>>> fetchCount(ApiQuery query)
    {
        if (query == null)
            query = ApiQuery.builder().build();

        return ApiResponse.<List<ApiCountInfo>>builder()
                          .value(this.service.fetchTotal(query))
                          .build()
                          .transform(ApiQueryHelper::createResponse);
    }

    @Override
    public RestResponse<ApiResponse<List<Map<String, Object>>>> fetchOptions(ApiQuery query)
    {
        if (query == null)
            query = ApiQuery.builder().build();

        return ApiResponse.<List<Map<String, Object>>>builder()
                          .value(this.service.fetchOptions(query))
                          .build()
                          .transform(ApiQueryHelper::createResponse);
    }
}
