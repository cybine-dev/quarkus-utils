package de.cybine.quarkus;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.api.v1.asset.*;
import de.cybine.quarkus.api.v1.customer.*;
import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.test.*;
import io.quarkus.test.common.http.*;
import io.quarkus.test.junit.*;
import io.restassured.*;
import io.restassured.http.*;
import io.restassured.internal.mapping.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.stream.*;

import static de.cybine.quarkus.util.api.query.ApiConditionDetail.Type.*;
import static de.cybine.quarkus.util.api.query.ApiConditionInfo.EvaluationMethod.*;
import static org.hamcrest.CoreMatchers.*;

@Slf4j
@QuarkusTest
@RequiredArgsConstructor
@DisplayName("ApiQuery (Options)")
@TestProfile(TestProfiles.Database.class)
class ApiOptionsQueryTest
{
    private Type optionsTypeRef;

    @TestHTTPResource("options")
    @TestHTTPEndpoint(AssetApi.class)
    URL assetApiUrl;

    @TestHTTPResource("options")
    @TestHTTPEndpoint(CustomerApi.class)
    URL customerApiUrl;

    private final ObjectMapper objectMapper;

    private Jackson2Mapper jackson;

    @BeforeEach
    void setup( )
    {
        this.jackson = new Jackson2Mapper((type, charset) -> this.objectMapper);
        TypeFactory typeFactory = this.objectMapper.getTypeFactory();

        MapType objectMap = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
        CollectionType mapList = typeFactory.constructCollectionType(List.class, objectMap);
        this.optionsTypeRef = typeFactory.constructParametricType(ApiResponse.class, mapList);
    }

    @Test
    @DisplayName("cannot fetch options without field")
    void cannotFetchOptionsWithoutField( )
    {
        ApiQuery query = ApiQuery.builder().build();

        // @formatter:off
        ApiResponse<List<Map<String, Object>>> response =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(400)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);
        // @formatter:on

        List<ApiError> apiErrors = response.getErrors().orElse(null);
        Assertions.assertNotNull(apiErrors);
        Assertions.assertEquals(1, apiErrors.size());
        Assertions.assertEquals("invalid-datasource-query", apiErrors.get(0).getCode());
    }

    @Test
    @DisplayName("can fetch options")
    void canFetchOptions( )
    {
        ApiQuery query = ApiQuery.builder().field("id").build();

        // @formatter:off
        ApiResponse<List<Map<String, Object>>> response =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);
        // @formatter:on

        Assertions.assertTrue(response.getErrors().isEmpty());
        Assertions.assertEquals(4, response.getValue().size());

        Set<String> keys = response.getValue().get(0).keySet();
        Assertions.assertEquals(1, keys.size());
        Assertions.assertEquals(Set.of("id"), keys);

        List<AssetId> ids = response.getValue()
                                    .stream()
                                    .map(item -> (String) item.get("id"))
                                    .map(UUID::fromString)
                                    .map(AssetId::of)
                                    .toList();

        Assertions.assertEquals(4, ids.size());
    }

    @Test
    @DisplayName("can fetch multiple options")
    void canFetchMultipleOptions( )
    {
        ApiQuery query = ApiQuery.builder().field("id").field("name").build();

        // @formatter:off
        ApiResponse<List<Map<String, Object>>> response =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);
        // @formatter:on

        Assertions.assertTrue(response.getErrors().isEmpty());
        Assertions.assertEquals(4, response.getValue().size());

        Set<String> keys = response.getValue().get(0).keySet();
        Assertions.assertEquals(2, keys.size());
        Assertions.assertEquals(Set.of("id", "name"), keys);

        List<String> names = response.getValue().stream().map(item -> (String) item.get("name")).toList();
        List<AssetId> ids = response.getValue()
                                    .stream()
                                    .map(item -> (String) item.get("id"))
                                    .map(UUID::fromString)
                                    .map(AssetId::of)
                                    .toList();

        Assertions.assertEquals(4, names.size());
        Assertions.assertEquals(4, ids.size());
    }

    @Test
    @DisplayName("can paginate options")
    void canPaginateOptions( )
    {
        ApiOrderInfo order = ApiOrderInfo.builder().property("id").isAscending(true).build();

        ApiQueryPagination pagination1 = ApiQueryPagination.builder().size(2).includeTotal(true).build();
        ApiQuery query1 = ApiQuery.builder().field("id").field("name").pagination(pagination1).order(order).build();

        ApiQueryPagination pagination2 = ApiQueryPagination.builder().size(1).offset(2).build();
        ApiQuery query2 = ApiQuery.builder().field("id").field("name").pagination(pagination2).order(order).build();

        // @formatter:off
        ApiResponse<List<Map<String, Object>>> options1 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query1)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);

        ApiResponse<List<Map<String, Object>>> options2 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query2)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);
        // @formatter:on

        Assertions.assertNotNull(options1.getValue());
        Assertions.assertTrue(options1.getErrors().isEmpty());
        Assertions.assertEquals(2, options1.getValue().size());

        ApiResourceInfo info1 = options1.getSelf().orElse(null);
        Assertions.assertNotNull(info1);
        Assertions.assertTrue(info1.getTotal().isPresent());
        Assertions.assertEquals(4, info1.getTotal().orElseThrow());

        Assertions.assertNotNull(options2.getValue());
        Assertions.assertTrue(options2.getErrors().isEmpty());
        Assertions.assertEquals(1, options2.getValue().size());

        ApiResourceInfo info2 = options2.getSelf().orElse(null);
        Assertions.assertNotNull(info2);
        Assertions.assertTrue(info2.getTotal().isEmpty());

        String name = (String) options2.getValue().get(0).get("name");
        Set<String> names = options1.getValue()
                                    .stream()
                                    .map(item -> (String) item.get("name"))
                                    .collect(Collectors.toSet());

        Assertions.assertFalse(names.contains(name));
    }

    @Test
    @DisplayName("can sort options")
    void canSortOptions( )
    {
        ApiOrderInfo order1 = ApiOrderInfo.builder().property("id").isAscending(true).build();
        ApiQuery query1 = ApiQuery.builder().field("id").order(order1).build();

        ApiOrderInfo order2 = ApiOrderInfo.builder().property("id").isAscending(false).build();
        ApiQuery query2 = ApiQuery.builder().field("id").order(order2).build();

        // @formatter:off
        ApiResponse<List<Map<String, Object>>> options1 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query1)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);

        ApiResponse<List<Map<String, Object>>> options2 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query2)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);
        // @formatter:on

        Assertions.assertEquals(1, options1.getValue().get(0).get("id"));
        Assertions.assertEquals(3, options1.getValue().get(2).get("id"));

        Assertions.assertEquals(3, options2.getValue().get(0).get("id"));
        Assertions.assertEquals(1, options2.getValue().get(2).get("id"));
    }

    @Test
    @DisplayName("can sort options by multiple properties")
    void canSortOptionsByMultipleProperties( )
    {
        ApiOrderInfo order1 = ApiOrderInfo.builder().isAscending(true).property("firstname").priority(1).build();
        ApiOrderInfo order2 = ApiOrderInfo.builder().isAscending(true).property("lastname").priority(2).build();
        ApiQuery query1 = ApiQuery.builder().field("id").order(order1).order(order2).build();

        ApiOrderInfo order3 = ApiOrderInfo.builder().isAscending(true).property("firstname").priority(2).build();
        ApiOrderInfo order4 = ApiOrderInfo.builder().isAscending(true).property("lastname").priority(1).build();
        ApiQuery query2 = ApiQuery.builder().field("id").order(order3).order(order4).build();

        // @formatter:off
        ApiResponse<List<Map<String, Object>>> options1 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query1)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);

        ApiResponse<List<Map<String, Object>>> options2 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query2)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);
        // @formatter:on

        Assertions.assertEquals(3, options1.getValue().get(0).get("id"));
        Assertions.assertEquals(2, options1.getValue().get(2).get("id"));

        Assertions.assertEquals(3, options2.getValue().get(0).get("id"));
        Assertions.assertEquals(1, options2.getValue().get(2).get("id"));
    }

    @Test
    @DisplayName("can filter options")
    void canFilterOptions( )
    {
        ApiConditionDetail detail1 = ApiConditionDetail.builder().property("id").type(IS_LESS).value(3).build();
        ApiConditionDetail detail2 = ApiConditionDetail.builder().property("id").type(IS_IN).value(List.of(3)).build();
        ApiConditionDetail detail3 = ApiConditionDetail.builder().property("id").type(IS_NOT_EQUAL).value(1).build();

        ApiConditionInfo condition1 = ApiConditionInfo.builder().detail(detail1).build();
        ApiQuery query1 = ApiQuery.builder().field("id").condition(condition1).build();

        ApiConditionInfo condition2 = ApiConditionInfo.builder().detail(detail2).build();
        ApiQuery query2 = ApiQuery.builder().field("id").condition(condition2).build();

        ApiConditionInfo condition3 = ApiConditionInfo.builder().detail(detail1).detail(detail3).build();
        ApiQuery query3 = ApiQuery.builder().field("id").condition(condition3).build();

        ApiConditionInfo condition4 = ApiConditionInfo.builder().type(OR).detail(detail1).detail(detail2).build();
        ApiQuery query4 = ApiQuery.builder().field("id").condition(condition4).build();

        // @formatter:off
        ApiResponse<List<Map<String, Object>>> options1 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query1)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);

        ApiResponse<List<Map<String, Object>>> options2 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query2)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);

        ApiResponse<List<Map<String, Object>>> options3 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query3)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);

        ApiResponse<List<Map<String, Object>>> options4 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query4)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);
        // @formatter:on

        Assertions.assertEquals(2, options1.getValue().size());
        Assertions.assertEquals(Set.of(1, 2),
                Set.of(options1.getValue().stream().map(item -> (Integer) item.get("id")).toArray(Integer[]::new)));

        Assertions.assertEquals(1, options2.getValue().size());
        Assertions.assertEquals(3, options2.getValue().get(0).get("id"));

        Assertions.assertEquals(1, options3.getValue().size());
        Assertions.assertEquals(2, options3.getValue().get(0).get("id"));

        Assertions.assertEquals(3, options4.getValue().size());
    }

    @Test
    @DisplayName("can fetch relation")
    void canFetchRelation( )
    {
        AssetId assetId = AssetId.of(UUID.fromString("01924362-448a-730e-bc2d-18044c5c6e3d"));
        ApiConditionDetail detail = ApiConditionDetail.builder().property("id").type(IS_EQUAL).value(assetId).build();
        ApiConditionInfo condition = ApiConditionInfo.builder().detail(detail).build();

        ApiRelationInfo relation1 = ApiRelationInfo.builder().property("rentals").fetch(true).field("id").build();
        ApiQuery query1 = ApiQuery.builder().field("id").condition(condition).relation(relation1).build();

        ApiRelationInfo relation2 = ApiRelationInfo.builder().property("rentals").fetch(false).field("id").build();
        ApiQuery query2 = ApiQuery.builder().field("id").condition(condition).relation(relation2).build();

        // @formatter:off
        ApiResponse<List<Map<String, Object>>> options1 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query1)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);

        ApiResponse<List<Map<String, Object>>> options2 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query2)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);
        // @formatter:on

        Map<String, Object> asset1 = options1.getValue().get(0);
        Assertions.assertTrue(asset1.containsKey("id"));
        Assertions.assertTrue(asset1.containsKey("rentals.id"));

        Map<String, Object> asset2 = options2.getValue().get(0);
        Assertions.assertTrue(asset2.containsKey("id"));
        Assertions.assertFalse(asset2.containsKey("rentals.id"));
    }

    @Test
    @DisplayName("can filter relations")
    void canFilterRelations( )
    {
        // @formatter:off
        String rentalId = "01924362-448a-7780-96a5-26b1cdcb1c14";
        ApiConditionDetail detail = ApiConditionDetail.builder().property("id").type(IS_EQUAL).value(rentalId).build();
        ApiConditionInfo condition = ApiConditionInfo.builder().detail(detail).build();

        ApiRelationInfo relation = ApiRelationInfo.builder().property("rentals").fetch(true).condition(condition).field("id").build();
        ApiQuery query = ApiQuery.builder().relation(relation).field("id").build();

        ApiResponse<List<Map<String, Object>>> options =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(optionsTypeRef, this.jackson);
        // @formatter:on

        Map<String, Object> data = options.getValue().get(0);

        Assertions.assertEquals("01924362-448a-730e-bc2d-18044c5c6e3d", data.get("id"));

        Assertions.assertTrue(data.containsKey("rentals.id"));
        Assertions.assertEquals(rentalId, data.get("rentals.id"));
    }
}
