package de.cybine.quarkus.api.v1.customer;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.data.customer.*;
import de.cybine.quarkus.data.rental.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;

import java.util.*;

import static de.cybine.quarkus.data.customer.CustomerEntity_.*;

@Startup
@RequiredArgsConstructor
public class CustomerResource implements CustomerApi
{
    // @formatter:off
    private final GenericApiQueryService<CustomerEntity, Customer> service =
            GenericApiQueryService.forType(CustomerEntity.class, Customer.class);
    // @formatter:on

    private final ApiFieldResolver resolver;

    @PostConstruct
    void setup( )
    {
        this.resolver.registerType(Customer.class)
                     .withField("id", ID)
                     .withField("firstname", FIRSTNAME)
                     .withField("lastname", LASTNAME)
                     .withRelation("rentals", RENTALS, Rental.class);
    }

    @Override
    public RestResponse<ApiResponse<List<Customer>>> fetch(ApiQuery query)
    {
        if (query == null)
            query = ApiQuery.builder().build();

        return ApiResponse.<List<Customer>>builder()
                          .value(this.service.fetch(query))
                          .build()
                          .transform(ApiQueryHelper::createResponse);
    }

    @Override
    public RestResponse<ApiResponse<Customer>> fetchSingle(ApiQuery query)
    {
        if (query == null)
            query = ApiQuery.builder().build();

        return ApiResponse.<Customer>builder()
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
