package de.cybine.quarkus.api.v1.secret;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.secret.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;

import java.util.*;

@AllArgsConstructor
@Path("/api/v1/secret")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SecretResource
{
    private final ApiQueryContext context;

    @PUT
    public RestResponse<ApiResponse<Void>> addSecretValue(@QueryParam("name") String name,
            @QueryParam("value") String value)
    {
        SecretData secretData = this.context.getSecretData();
        secretData.getProperties().put(name, value);

        return ApiResponse.<Void>builder().build().transform(ApiQueryHelper::createResponse);
    }

    @GET
    public RestResponse<ApiResponse<Map<String, Object>>> getSecretValues( )
    {
        return ApiResponse.<Map<String, Object>>builder()
                          .value(this.context.getSecretData().getProperties())
                          .build()
                          .transform(ApiQueryHelper::createResponse);
    }
}
