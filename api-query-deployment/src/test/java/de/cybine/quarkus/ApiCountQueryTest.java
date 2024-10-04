package de.cybine.quarkus;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.api.v1.customer.*;
import de.cybine.quarkus.api.v1.rental.*;
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
import java.time.*;
import java.util.*;
import java.util.stream.*;

import static org.hamcrest.CoreMatchers.*;

@Slf4j
@QuarkusTest
@RequiredArgsConstructor
@DisplayName("ApiQuery (Count)")
@TestProfile(TestProfiles.Database.class)
class ApiCountQueryTest
{
    private Type countTypeRef;

    @TestHTTPResource("count")
    @TestHTTPEndpoint(RentalApi.class)
    URL rentalApiUrl;

    @TestHTTPResource("count")
    @TestHTTPEndpoint(CustomerApi.class)
    URL customerApiUrl;

    private final ObjectMapper objectMapper;

    private Jackson2Mapper jackson;

    @BeforeEach
    void setup( )
    {
        this.jackson = new Jackson2Mapper((type, charset) -> this.objectMapper);
        TypeFactory typeFactory = this.objectMapper.getTypeFactory();

        CollectionType countList = typeFactory.constructCollectionType(List.class, ApiCountInfo.class);
        this.countTypeRef = typeFactory.constructParametricType(ApiResponse.class, countList);
    }

    @Test
    @DisplayName("can fetch counts")
    void canFetchCounts( )
    {
        ApiQuery query = ApiQuery.builder().build();

        // @formatter:off
        ApiResponse<List<ApiCountInfo>> response =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(countTypeRef, this.jackson);
        // @formatter:on

        Assertions.assertTrue(response.getErrors().isEmpty());
        Assertions.assertEquals(1, response.getValue().size());

        ApiCountInfo count = response.getValue().get(0);
        Assertions.assertTrue(count.getGroupKey().isEmpty());
        Assertions.assertEquals(3, count.getCount());
    }

    @Test
    @DisplayName("can fetch grouped counts")
    void canFetchGroupedCounts( )
    {
        ApiQuery query = ApiQuery.builder().field("begins_at").build();

        // @formatter:off
        ApiResponse<List<ApiCountInfo>> response =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query)
                           .when().post(this.rentalApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(countTypeRef, this.jackson);
        // @formatter:on

        Assertions.assertTrue(response.getErrors().isEmpty());
        Assertions.assertEquals(2, response.getValue().size());

        // @formatter:off
        Map<LocalDate, Long> counts =
                response.getValue()
                        .stream()
                        .collect(Collectors.toMap(
                                item -> LocalDate.parse((String) item.getGroupKey().get("begins_at")),
                                ApiCountInfo::getCount));
        // @formatter:on

        Assertions.assertEquals(2, counts.get(LocalDate.parse("2024-03-09")));
        Assertions.assertEquals(1, counts.get(LocalDate.parse("2024-08-04")));
    }

    @Test
    @DisplayName("can sort counts")
    void canSortCounts( )
    {
        ApiOrderInfo order1 = ApiOrderInfo.builder().property("begins_at").isAscending(true).build();
        ApiQuery query1 = ApiQuery.builder().field("begins_at").order(order1).build();

        ApiOrderInfo order2 = ApiOrderInfo.builder().property("begins_at").isAscending(false).build();
        ApiQuery query2 = ApiQuery.builder().field("begins_at").order(order2).build();

        // @formatter:off
        ApiResponse<List<ApiCountInfo>> counts1 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query1)
                           .when().post(this.rentalApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(countTypeRef, this.jackson);

        ApiResponse<List<ApiCountInfo>> counts2 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query2)
                           .when().post(this.rentalApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(countTypeRef, this.jackson);
        // @formatter:on

        Assertions.assertEquals("2024-03-09", counts1.getValue().get(0).getGroupKey().get("begins_at"));
        Assertions.assertEquals("2024-08-04", counts1.getValue().get(1).getGroupKey().get("begins_at"));

        Assertions.assertEquals("2024-08-04", counts2.getValue().get(0).getGroupKey().get("begins_at"));
        Assertions.assertEquals("2024-03-09", counts2.getValue().get(1).getGroupKey().get("begins_at"));
    }

    // @Test
    // @DisplayName("can filter counts")
    // void canFilterCounts( )
    // {
    //     ApiConditionDetail detail1 = ApiConditionDetail.builder().property("id").type(IS_LESS).value(3).build();
    //     ApiConditionDetail detail2 = ApiConditionDetail.builder().property("id").type(IS_IN).value(List.of(3))
    //     .build();
    //     ApiConditionDetail detail3 = ApiConditionDetail.builder().property("id").type(IS_NOT_EQUAL).value(1).build();
    //
    //     ApiConditionInfo condition1 = ApiConditionInfo.builder().detail(detail1).build();
    //     ApiQuery query1 = ApiQuery.builder().field("id").condition(condition1).build();
    //
    //     ApiConditionInfo condition2 = ApiConditionInfo.builder().detail(detail2).build();
    //     ApiQuery query2 = ApiQuery.builder().field("id").condition(condition2).build();
    //
    //     ApiConditionInfo condition3 = ApiConditionInfo.builder().detail(detail1).detail(detail3).build();
    //     ApiQuery query3 = ApiQuery.builder().field("id").condition(condition3).build();
    //
    //     ApiConditionInfo condition4 = ApiConditionInfo.builder().type(OR).detail(detail1).detail(detail2).build();
    //     ApiQuery query4 = ApiQuery.builder().field("id").condition(condition4).build();
    //
    //     // @formatter:off
    //     ApiResponse<List<Map<String, Object>>> options1 =
    //             RestAssured.given().contentType(ContentType.JSON)
    //                        .and().body(query1)
    //                        .when().post(this.customerApiUrl)
    //                        .then().statusCode(200)
    //                        .and().body(notNullValue())
    //                        .extract().body().as(countTypeRef, this.jackson);
    //
    //     ApiResponse<List<Map<String, Object>>> options2 =
    //             RestAssured.given().contentType(ContentType.JSON)
    //                        .and().body(query2)
    //                        .when().post(this.customerApiUrl)
    //                        .then().statusCode(200)
    //                        .and().body(notNullValue())
    //                        .extract().body().as(countTypeRef, this.jackson);
    //
    //     ApiResponse<List<Map<String, Object>>> options3 =
    //             RestAssured.given().contentType(ContentType.JSON)
    //                        .and().body(query3)
    //                        .when().post(this.customerApiUrl)
    //                        .then().statusCode(200)
    //                        .and().body(notNullValue())
    //                        .extract().body().as(countTypeRef, this.jackson);
    //
    //     ApiResponse<List<Map<String, Object>>> options4 =
    //             RestAssured.given().contentType(ContentType.JSON)
    //                        .and().body(query4)
    //                        .when().post(this.customerApiUrl)
    //                        .then().statusCode(200)
    //                        .and().body(notNullValue())
    //                        .extract().body().as(countTypeRef, this.jackson);
    //     // @formatter:on
    //
    //     Assertions.assertEquals(2, options1.getValue().size());
    //     Assertions.assertEquals(Set.of(1, 2),
    //             Set.of(options1.getValue().stream().map(item -> (Integer) item.get("id")).toArray(Integer[]::new)));
    //
    //     Assertions.assertEquals(1, options2.getValue().size());
    //     Assertions.assertEquals(3, options2.getValue().get(0).get("id"));
    //
    //     Assertions.assertEquals(1, options3.getValue().size());
    //     Assertions.assertEquals(2, options3.getValue().get(0).get("id"));
    //
    //     Assertions.assertEquals(3, options4.getValue().size());
    // }
    //
    // @Test
    // @DisplayName("can fetch relation")
    // void canFetchRelation( )
    // {
    //     AssetId assetId = AssetId.of(UUID.fromString("01924362-448a-730e-bc2d-18044c5c6e3d"));
    //     ApiConditionDetail detail = ApiConditionDetail.builder().property("id").type(IS_EQUAL).value(assetId)
    //     .build();
    //     ApiConditionInfo condition = ApiConditionInfo.builder().detail(detail).build();
    //
    //     ApiRelationInfo relation1 = ApiRelationInfo.builder().property("rentals").fetch(true).field("id").build();
    //     ApiQuery query1 = ApiQuery.builder().field("id").condition(condition).relation(relation1).build();
    //
    //     ApiRelationInfo relation2 = ApiRelationInfo.builder().property("rentals").fetch(false).field("id").build();
    //     ApiQuery query2 = ApiQuery.builder().field("id").condition(condition).relation(relation2).build();
    //
    //     // @formatter:off
    //     ApiResponse<List<Map<String, Object>>> options1 =
    //             RestAssured.given().contentType(ContentType.JSON)
    //                        .and().body(query1)
    //                        .when().post(this.assetApiUrl)
    //                        .then().statusCode(200)
    //                        .and().body(notNullValue())
    //                        .extract().body().as(countTypeRef, this.jackson);
    //
    //     ApiResponse<List<Map<String, Object>>> options2 =
    //             RestAssured.given().contentType(ContentType.JSON)
    //                        .and().body(query2)
    //                        .when().post(this.assetApiUrl)
    //                        .then().statusCode(200)
    //                        .and().body(notNullValue())
    //                        .extract().body().as(countTypeRef, this.jackson);
    //     // @formatter:on
    //
    //     Map<String, Object> asset1 = options1.getValue().get(0);
    //     Assertions.assertTrue(asset1.containsKey("id"));
    //     Assertions.assertTrue(asset1.containsKey("rentals.id"));
    //
    //     Map<String, Object> asset2 = options2.getValue().get(0);
    //     Assertions.assertTrue(asset2.containsKey("id"));
    //     Assertions.assertFalse(asset2.containsKey("rentals.id"));
    // }
    //
    // @Test
    // @DisplayName("can filter relations")
    // void canFilterRelations( )
    // {
    //     // @formatter:off
    //     String rentalId = "01924362-448a-7780-96a5-26b1cdcb1c14";
    //     ApiConditionDetail detail = ApiConditionDetail.builder().property("id").type(IS_EQUAL).value(rentalId).build();
    //     ApiConditionInfo condition = ApiConditionInfo.builder().detail(detail).build();
    //
    //     ApiRelationInfo relation = ApiRelationInfo.builder().property("rentals").fetch(true).condition(condition).field("id").build();
    //     ApiQuery query = ApiQuery.builder().relation(relation).field("id").build();
    //
    //     ApiResponse<List<Map<String, Object>>> options =
    //             RestAssured.given().contentType(ContentType.JSON)
    //                        .and().body(query)
    //                        .when().post(this.assetApiUrl)
    //                        .then().statusCode(200)
    //                        .and().body(notNullValue())
    //                        .extract().body().as(countTypeRef, this.jackson);
    //     // @formatter:on
    //
    //     Map<String, Object> data = options.getValue().get(0);
    //
    //     Assertions.assertEquals("01924362-448a-730e-bc2d-18044c5c6e3d", data.get("id"));
    //
    //     Assertions.assertTrue(data.containsKey("rentals.id"));
    //     Assertions.assertEquals(rentalId, data.get("rentals.id"));
    // }
}
