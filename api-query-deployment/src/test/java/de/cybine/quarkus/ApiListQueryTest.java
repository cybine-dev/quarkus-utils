package de.cybine.quarkus;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.api.v1.asset.*;
import de.cybine.quarkus.api.v1.customer.*;
import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.data.customer.*;
import de.cybine.quarkus.data.rental.*;
import de.cybine.quarkus.data.util.primitive.*;
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

import static de.cybine.quarkus.util.api.query.ApiConditionDetail.Type.*;
import static de.cybine.quarkus.util.api.query.ApiConditionInfo.EvaluationMethod.*;
import static org.hamcrest.CoreMatchers.*;

@Slf4j
@QuarkusTest
@RequiredArgsConstructor
@DisplayName("ApiQuery (List)")
@TestProfile(TestProfiles.Database.class)
class ApiListQueryTest
{
    private Type assetListRef;
    private Type customerListRef;

    @TestHTTPResource
    @TestHTTPEndpoint(AssetApi.class)
    URL assetApiUrl;

    @TestHTTPResource
    @TestHTTPEndpoint(CustomerApi.class)
    URL customerApiUrl;

    private final ObjectMapper objectMapper;

    private Jackson2Mapper jackson;

    @BeforeEach
    void setup( )
    {
        this.jackson = new Jackson2Mapper((type, charset) -> this.objectMapper);
        TypeFactory typeFactory = this.objectMapper.getTypeFactory();

        CollectionType assetList = typeFactory.constructCollectionType(List.class, Asset.class);
        this.assetListRef = typeFactory.constructParametricType(ApiResponse.class, assetList);

        CollectionType customerList = typeFactory.constructCollectionType(List.class, Customer.class);
        this.customerListRef = typeFactory.constructParametricType(ApiResponse.class, customerList);
    }

    @Test
    @DisplayName("can fetch items")
    void canFetchItems( )
    {
        // @formatter:off
        ApiResponse<List<Asset>> response =
                RestAssured.given().contentType(ContentType.JSON)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(assetListRef);
        // @formatter:on

        Assertions.assertNotNull(response.getValue());
        Assertions.assertTrue(response.getErrors().isEmpty());
        Assertions.assertEquals(4, response.getValue().size());
    }

    @Test
    @DisplayName("can paginate items")
    void canPaginateItems( )
    {
        ApiQueryPagination pagination1 = ApiQueryPagination.builder().size(2).includeTotal(true).build();
        ApiQuery query1 = ApiQuery.builder().pagination(pagination1).build();

        ApiQueryPagination pagination2 = ApiQueryPagination.builder().size(1).offset(2).build();
        ApiQuery query2 = ApiQuery.builder().pagination(pagination2).build();

        // @formatter:off
        ApiResponse<List<Asset>> assets1 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query1)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(assetListRef);

        ApiResponse<List<Asset>> assets2 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query2)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(assetListRef);
        // @formatter:on

        Assertions.assertNotNull(assets1.getValue());
        Assertions.assertTrue(assets1.getErrors().isEmpty());
        Assertions.assertEquals(2, assets1.getValue().size());

        ApiResourceInfo info1 = assets1.getSelf().orElse(null);
        Assertions.assertNotNull(info1);
        Assertions.assertTrue(info1.getTotal().isPresent());
        Assertions.assertEquals(4, info1.getTotal().orElseThrow());

        Assertions.assertNotNull(assets2.getValue());
        Assertions.assertTrue(assets2.getErrors().isEmpty());
        Assertions.assertEquals(1, assets2.getValue().size());

        ApiResourceInfo info2 = assets2.getSelf().orElse(null);
        Assertions.assertNotNull(info2);
        Assertions.assertTrue(info2.getTotal().isEmpty());

        Asset asset = assets2.getValue().get(0);
        Assertions.assertFalse(assets1.getValue().contains(asset));
    }

    @Test
    @DisplayName("can sort items")
    void canSortItems( )
    {
        ApiOrderInfo order1 = ApiOrderInfo.builder().isAscending(true).property("id").build();
        ApiQuery query1 = ApiQuery.builder().order(order1).build();

        ApiOrderInfo order2 = ApiOrderInfo.builder().isAscending(false).property("id").build();
        ApiQuery query2 = ApiQuery.builder().order(order2).build();

        // @formatter:off
        ApiResponse<List<Customer>> customers1 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query1)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(customerListRef);

        ApiResponse<List<Customer>> customers2 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query2)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(customerListRef);
        // @formatter:on

        Assertions.assertEquals(1, customers1.getValue().get(0).getId().getValue());
        Assertions.assertEquals(3, customers1.getValue().get(2).getId().getValue());

        Assertions.assertEquals(3, customers2.getValue().get(0).getId().getValue());
        Assertions.assertEquals(1, customers2.getValue().get(2).getId().getValue());
    }

    @Test
    @DisplayName("can sort items by multiple properties")
    void canSortItemsByMultipleProperties( )
    {
        ApiOrderInfo order1 = ApiOrderInfo.builder().isAscending(true).property("firstname").priority(1).build();
        ApiOrderInfo order2 = ApiOrderInfo.builder().isAscending(true).property("lastname").priority(2).build();
        ApiQuery query1 = ApiQuery.builder().order(order1).order(order2).build();

        ApiOrderInfo order3 = ApiOrderInfo.builder().isAscending(true).property("firstname").priority(2).build();
        ApiOrderInfo order4 = ApiOrderInfo.builder().isAscending(true).property("lastname").priority(1).build();
        ApiQuery query2 = ApiQuery.builder().order(order3).order(order4).build();

        // @formatter:off
        ApiResponse<List<Customer>> customers1 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query1)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(customerListRef);

        ApiResponse<List<Customer>> customers2 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query2)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(customerListRef);
        // @formatter:on

        Assertions.assertEquals(3, customers1.getValue().get(0).getId().getValue());
        Assertions.assertEquals(2, customers1.getValue().get(2).getId().getValue());

        Assertions.assertEquals(3, customers2.getValue().get(0).getId().getValue());
        Assertions.assertEquals(1, customers2.getValue().get(2).getId().getValue());
    }

    @Test
    @DisplayName("can filter items")
    void canFilterItems( )
    {
        ApiConditionDetail detail1 = ApiConditionDetail.builder().property("id").type(IS_LESS).value(3).build();
        ApiConditionDetail detail2 = ApiConditionDetail.builder().property("id").type(IS_IN).value(List.of(3)).build();
        ApiConditionDetail detail3 = ApiConditionDetail.builder().property("id").type(IS_NOT_EQUAL).value(1).build();

        ApiConditionInfo condition1 = ApiConditionInfo.builder().detail(detail1).build();
        ApiQuery query1 = ApiQuery.builder().condition(condition1).build();

        ApiConditionInfo condition2 = ApiConditionInfo.builder().detail(detail2).build();
        ApiQuery query2 = ApiQuery.builder().condition(condition2).build();

        ApiConditionInfo condition3 = ApiConditionInfo.builder().detail(detail1).detail(detail3).build();
        ApiQuery query3 = ApiQuery.builder().condition(condition3).build();

        ApiConditionInfo condition4 = ApiConditionInfo.builder().type(OR).detail(detail1).detail(detail2).build();
        ApiQuery query4 = ApiQuery.builder().condition(condition4).build();

        // @formatter:off
        ApiResponse<List<Customer>> customers1 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query1)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(customerListRef);

        ApiResponse<List<Customer>> customers2 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query2)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(customerListRef);

        ApiResponse<List<Customer>> customers3 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query3)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(customerListRef);

        ApiResponse<List<Customer>> customers4 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query4)
                           .when().post(this.customerApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(customerListRef);
        // @formatter:on

        Assertions.assertEquals(2, customers1.getValue().size());
        Assertions.assertEquals(List.of(1L, 2L),
                customers1.getValue().stream().map(Customer::getId).map(Id::getValue).toList());

        Assertions.assertEquals(1, customers2.getValue().size());
        Assertions.assertEquals(3, customers2.getValue().get(0).getId().getValue());

        Assertions.assertEquals(1, customers3.getValue().size());
        Assertions.assertEquals(2, customers3.getValue().get(0).getId().getValue());

        Assertions.assertEquals(3, customers4.getValue().size());
    }

    @Test
    @DisplayName("can fetch relation")
    void canFetchRelation( )
    {
        AssetId assetId = AssetId.of(UUID.fromString("01924362-448a-730e-bc2d-18044c5c6e3d"));
        ApiConditionDetail detail = ApiConditionDetail.builder().property("id").type(IS_EQUAL).value(assetId).build();
        ApiConditionInfo condition = ApiConditionInfo.builder().detail(detail).build();

        ApiRelationInfo relation1 = ApiRelationInfo.builder().property("rentals").fetch(true).build();
        ApiQuery query1 = ApiQuery.builder().condition(condition).relation(relation1).build();

        ApiRelationInfo relation2 = ApiRelationInfo.builder().property("rentals").fetch(false).build();
        ApiQuery query2 = ApiQuery.builder().condition(condition).relation(relation2).build();

        // @formatter:off
        ApiResponse<List<Asset>> assets1 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query1)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(assetListRef, this.jackson);

        ApiResponse<List<Asset>> assets2 =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query2)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(assetListRef, this.jackson);
        // @formatter:on

        Assertions.assertTrue(assets1.getValue().get(0).getRentals().isPresent());

        List<Rental> rentals = assets1.getValue().get(0).getRentals().orElseThrow();
        Assertions.assertEquals(1, rentals.size());
        Assertions.assertEquals(RentalId.of(UUID.fromString("01924362-448a-7780-96a5-26b1cdcb1c14")),
                rentals.get(0).getId());

        Assertions.assertTrue(assets2.getValue().get(0).getRentals().isEmpty());
    }

    @Test
    @DisplayName("can filter relations")
    void canFilterRelations( )
    {
        // @formatter:off
        RentalId rentalId = RentalId.of(UUID.fromString("01924362-448a-7780-96a5-26b1cdcb1c14"));
        ApiConditionDetail detail = ApiConditionDetail.builder().property("id").type(IS_EQUAL).value(rentalId.getValue()).build();
        ApiConditionInfo condition = ApiConditionInfo.builder().detail(detail).build();

        ApiRelationInfo relation = ApiRelationInfo.builder().property("rentals").fetch(true).condition(condition).build();
        ApiQuery query = ApiQuery.builder().relation(relation).build();

        ApiResponse<List<Asset>> assets =
                RestAssured.given().contentType(ContentType.JSON)
                           .and().body(query)
                           .when().post(this.assetApiUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(assetListRef, this.jackson);
        // @formatter:on

        Asset asset = assets.getValue().get(0);
        AssetId assetId = AssetId.of(UUID.fromString("01924362-448a-730e-bc2d-18044c5c6e3d"));

        Assertions.assertEquals(assetId, asset.getId());
        Assertions.assertTrue(asset.getRentals().isPresent());

        List<Rental> rentals = asset.getRentals().orElseThrow();
        Assertions.assertEquals(1, rentals.size());
        Assertions.assertEquals(rentalId, rentals.get(0).getId());
    }
}
