package de.cybine.quarkus.api.v1.library;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.data.library.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;

import java.util.*;

import static de.cybine.quarkus.data.library.LibraryEntity_.*;

@Startup
@RequiredArgsConstructor
public class LibraryResource implements LibraryApi
{
    // @formatter:off
    private final GenericApiQueryService<LibraryEntity, Library> service =
            GenericApiQueryService.forType(LibraryEntity.class, Library.class);
    // @formatter:on

    private final ApiFieldResolver resolver;

    @PostConstruct
    void setup( )
    {
        this.resolver.registerType(Library.class)
                     .withField("id", ID)
                     .withField("name", NAME)
                     .withField("address", ADDRESS)
                     .withRelation("assets", ASSETS, Asset.class);
    }

    @Override
    public RestResponse<ApiResponse<List<Library>>> fetch(ApiQuery query)
    {
        if (query == null)
            query = ApiQuery.builder().build();

        return ApiResponse.<List<Library>>builder()
                          .value(this.service.fetch(query))
                          .build()
                          .transform(ApiQueryHelper::createResponse);
    }

    @Override
    public RestResponse<ApiResponse<Library>> fetchSingle(ApiQuery query)
    {
        if (query == null)
            query = ApiQuery.builder().build();

        return ApiResponse.<Library>builder()
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
