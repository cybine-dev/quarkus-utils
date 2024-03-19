package de.cybine.quarkus.util.api;

import de.cybine.quarkus.api.response.*;
import lombok.experimental.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.RestResponse.*;

import java.net.*;

@UtilityClass
@SuppressWarnings("unused")
public class ApiQueryHelper
{
    public static <T> ResponseBuilder<ApiResponse<T>> createResponseBuilder(ApiResponse<T> response)
    {
        ResponseBuilder<ApiResponse<T>> builder = ResponseBuilder.create(
                RestResponse.Status.fromStatusCode(response.getStatusCode()), response);

        response.getSelf().map(ApiResourceInfo::getHref).map(URI::create).ifPresent(builder::location);

        return builder;
    }

    public static <T> RestResponse<ApiResponse<T>> createResponse(ApiResponse<T> response)
    {
        return ApiQueryHelper.createResponseBuilder(response).build();
    }
}
