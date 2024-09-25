package de.cybine.quarkus.util.api;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.config.*;
import de.cybine.quarkus.util.api.api.v1.secret.*;
import de.cybine.quarkus.util.test.*;
import io.quarkus.test.common.http.*;
import io.quarkus.test.junit.*;
import io.restassured.*;
import lombok.*;
import org.junit.jupiter.api.*;

import java.net.*;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@RequiredArgsConstructor
@TestProfile(TestProfiles.Integration.class)
class ApiQueryExtensionProcessorTest
{
    private static final String API_SECRET_HEADER = "x-qu-api-secret";

    private final ApiQueryConfig config;

    @TestHTTPResource
    @TestHTTPEndpoint(SecretResource.class)
    URL secretResourceUrl;

    @Test
    @SuppressWarnings("unchecked")
    void testSecret( )
    {
        // @formatter:off
        String secret = RestAssured.given().queryParam("name", "test").queryParam("value", "test")
                                   .when().put(this.secretResourceUrl)
                                   .then().statusCode(200).header(API_SECRET_HEADER, notNullValue())
                                   .extract().header(API_SECRET_HEADER);

        ApiResponse<Map<String, Object>> response =
                RestAssured.given().header(API_SECRET_HEADER, secret)
                           .when().get(secretResourceUrl)
                           .then().statusCode(200)
                           .and().body(notNullValue())
                           .extract().body().as(ApiResponse.class);

        Assertions.assertEquals("test", response.getValue().get("test"));
        Assertions.assertFalse(response.getValue().containsKey("test2"));

        secret = RestAssured.given().queryParam("name", "test2").queryParam("value", "test")
                            .when().put(this.secretResourceUrl)
                            .then().statusCode(200).header(API_SECRET_HEADER, notNullValue())
                            .extract().header(API_SECRET_HEADER);

        response = RestAssured.given().header(API_SECRET_HEADER, secret)
                              .when().get(secretResourceUrl)
                              .then().statusCode(200)
                              .and().body(notNullValue())
                              .and().header(API_SECRET_HEADER, notNullValue())
                              .extract().body().as(ApiResponse.class);

        Assertions.assertEquals("test", response.getValue().get("test"));
        Assertions.assertEquals("test2", response.getValue().get("test"));
        // @formatter:on
    }
}