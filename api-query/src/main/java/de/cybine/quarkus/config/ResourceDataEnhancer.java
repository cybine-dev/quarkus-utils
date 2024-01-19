package de.cybine.quarkus.config;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.util.api.permission.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.api.response.*;
import io.smallrye.config.common.utils.*;
import io.vertx.core.http.*;
import jakarta.inject.*;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.server.*;

import java.lang.reflect.*;
import java.util.*;

@Singleton
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ResourceDataEnhancer
{
    private final ApiPaginationInfo paginationInfo;

    private final ResourceInfo      resourceInfo;
    private final HttpServerRequest request;

    @ServerRequestFilter
    public Optional<RestResponse<Void>> enhanceRequest(ContainerRequestContext context)
    {
        Method resourceMethod = this.resourceInfo.getResourceMethod();
        if (resourceMethod.isAnnotationPresent(ApiAction.class))
        {
            String action = resourceMethod.getAnnotation(ApiAction.class).value();
            // TODO: check permission
        }

        try
        {
            MultivaluedMap<String, String> queryParameters = context.getUriInfo().getQueryParameters();
            String size = queryParameters.getFirst("size");
            if (StringUtil.isNumeric(size))
                this.paginationInfo.setSize(Integer.valueOf(size));

            String offset = queryParameters.getFirst("offset");
            if (StringUtil.isNumeric(offset))
                this.paginationInfo.setOffset(Integer.valueOf(offset));

            String includeTotal = queryParameters.getFirst("total");
            if (includeTotal != null)
                this.paginationInfo.includeTotal(includeTotal.equals("true"));
        }
        catch (NumberFormatException ignored)
        {
            // NOOP
        }

        return Optional.empty();
    }

    @ServerResponseFilter
    public void enhanceResponse(ContainerResponseContext context)
    {
        if (!context.hasEntity())
            return;

        if (context.getEntity() instanceof ApiResponse<?> response)
        {
            ApiResourceInfo.Generator info = ApiResourceInfo.builder().href(this.request.absoluteURI());
            this.paginationInfo.getSizeAsLong().ifPresent(info::size);
            this.paginationInfo.getOffsetAsLong().ifPresent(info::offset);
            this.paginationInfo.getTotal().ifPresent(info::total);

            context.setEntity(response.withSelf(info.build()));
        }
    }
}
