package de.cybine.quarkus.util.api.api.v1.secret;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.secret.*;
import jakarta.ws.rs.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;

import java.util.*;

@AllArgsConstructor
@Path("/api/v1/secret")
public class SecretResource
{
    private final ApiQueryContext context;

    @PUT
    public RestResponse<ApiResponse<Void>> addSecretValue(@QueryParam("name") String name,
            @QueryParam("value") String value)
    {
        SecretData secretData = this.context.getSecretData().orElse(null);
        if (secretData == null)
            this.context.setSecretData(secretData = SecretData.builder().build());

        secretData.getProperties().put(name, value);
        return ApiResponse.<Void>builder().build().transform(ApiQueryHelper::createResponse);
    }

    @GET
    public RestResponse<ApiResponse<Map<String, Object>>> getSecretValues( )
    {
        SecretData secretData = this.context.getSecretData().orElse(null);
        if (secretData == null)
            return ApiResponse.<Map<String, Object>>builder()
                              .value(new HashMap<>())
                              .build()
                              .transform(ApiQueryHelper::createResponse);

        return ApiResponse.<Map<String, Object>>builder()
                          .value(this.context.getSecretData().map(SecretData::getProperties).orElseThrow())
                          .build()
                          .transform(ApiQueryHelper::createResponse);
    }
}
