package de.cybine.quarkus.api.v1.rental;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.data.customer.*;
import de.cybine.quarkus.data.rental.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;

import java.util.*;

import static de.cybine.quarkus.data.rental.RentalEntity_.*;

@Startup
@RequiredArgsConstructor
public class RentalResource implements RentalApi
{
    // @formatter:off
    private final GenericApiQueryService<RentalEntity, Rental> service =
            GenericApiQueryService.forType(RentalEntity.class, Rental.class);
    // @formatter:on

    private final ApiFieldResolver resolver;

    @PostConstruct
    void setup( )
    {
        this.resolver.registerType(Rental.class)
                     .withField("id", ID)
                     .withField("asset_id", ASSET_ID)
                     .withRelation("asset", ASSET, Asset.class)
                     .withField("customer_id", CUSTOMER_ID)
                     .withRelation("customer", CUSTOMER, Customer.class)
                     .withField("begins_at", BEGINS_AT)
                     .withField("ends_at", ENDS_AT)
                     .withField("returned_at", RETURNED_AT);
    }

    @Override
    public RestResponse<ApiResponse<List<Rental>>> fetch(ApiQuery query)
    {
        if (query == null)
            query = ApiQuery.builder().build();

        return ApiResponse.<List<Rental>>builder()
                          .value(this.service.fetch(query))
                          .build()
                          .transform(ApiQueryHelper::createResponse);
    }

    @Override
    public RestResponse<ApiResponse<Rental>> fetchSingle(ApiQuery query)
    {
        if (query == null)
            query = ApiQuery.builder().build();

        return ApiResponse.<Rental>builder()
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
