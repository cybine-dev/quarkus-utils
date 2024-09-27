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
@DisplayName("SecretProvider")
@TestProfile(TestProfiles.Integration.class)
class ApiSecretTest
{
    private static final String API_SECRET_HEADER = "x-qu-api-secret";

    private final ApiQueryConfig config;

    @TestHTTPResource
    @TestHTTPEndpoint(SecretResource.class)
    URL secretResourceUrl;

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("can create api-secrets")
    void canCreateSecret( )
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
        // @formatter:on
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("can modify api-secrets")
    void canModifySecret()
    {
        // @formatter:off
        String secret = RestAssured.given().queryParam("name", "test").queryParam("value", "test")
                                   .when().put(this.secretResourceUrl)
                                   .then().statusCode(200).header(API_SECRET_HEADER, notNullValue())
                                   .extract().header(API_SECRET_HEADER);

        secret = RestAssured.given().queryParam("name", "test2").queryParam("value", "test")
                            .and().header(API_SECRET_HEADER, secret)
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
        Assertions.assertEquals("test", response.getValue().get("test2"));
        // @formatter:on
    }

    @Test
    @DisplayName("can reflect api-secrets")
    void canReflectSecret()
    {
        // @formatter:off
        String secret = RestAssured.given().queryParam("name", "test").queryParam("value", "test")
                                   .when().put(this.secretResourceUrl)
                                   .then().statusCode(200).header(API_SECRET_HEADER, notNullValue())
                                   .extract().header(API_SECRET_HEADER);

        RestAssured.given().header(API_SECRET_HEADER, secret)
                   .when().get(secretResourceUrl)
                   .then().statusCode(200)
                   .and().header(API_SECRET_HEADER, notNullValue());
        // @formatter:on
    }
}