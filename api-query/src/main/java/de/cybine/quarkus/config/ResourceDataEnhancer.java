package de.cybine.quarkus.config;

import com.fasterxml.jackson.databind.*;
import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.exception.api.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.filter.*;
import de.cybine.quarkus.util.api.secret.*;
import io.smallrye.config.common.utils.*;
import io.smallrye.jwt.auth.principal.*;
import io.vertx.core.http.*;
import jakarta.enterprise.inject.*;
import jakarta.inject.*;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.server.*;
import org.jose4j.jwt.consumer.*;

import java.util.*;

@Slf4j
@Singleton
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ResourceDataEnhancer
{
    private final ApiQueryContext context;
    private final SecretProvider  secretProvider;

    private final ResourceInfo      resourceInfo;
    private final HttpServerRequest request;

    private final ObjectMapper objectMapper;

    private final Instance<ResponseFilter> responseFilter;

    @ServerRequestFilter
    public Optional<RestResponse<Void>> enhanceRequest(ContainerRequestContext context)
    {
        try
        {
            MultivaluedMap<String, String> queryParameters = context.getUriInfo().getQueryParameters();
            String size = queryParameters.getFirst("size");
            if (size != null && StringUtil.isNumeric(size))
                this.context.getPaginationInfo().setSize(Integer.valueOf(size));

            String offset = queryParameters.getFirst("offset");
            if (offset != null && StringUtil.isNumeric(offset))
                this.context.getPaginationInfo().setOffset(Integer.valueOf(offset));

            String includeTotal = queryParameters.getFirst("total");
            if (includeTotal != null)
                this.context.getPaginationInfo().includeTotal(includeTotal.equalsIgnoreCase("true"));
        }
        catch (NumberFormatException ignored)
        {
            // NOOP
        }

        this.decodeApiSecret(context);

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
            this.context.getPaginationInfo().getSizeAsLong().ifPresent(info::size);
            this.context.getPaginationInfo().getOffsetAsLong().ifPresent(info::offset);
            this.context.getPaginationInfo().getTotal().ifPresent(info::total);

            context.setEntity(response.withSelf(info.build()));
        }

        try
        {
            String secret = this.context.getRawSecretData().orElse(null);
            if (this.secretProvider.canCreateSecret())
                secret = this.secretProvider.createSecret(this.context.getSecretData());

            if (secret != null)
                context.getHeaders().add(SecretProvider.API_SECRET_HEADER, secret);
        }
        catch (SecretProviderException exception)
        {
            log.error("Could not create secret.", exception);
        }

        if (this.responseFilter.isResolvable())
            this.responseFilter.get().apply(context);
    }

    private void decodeApiSecret(ContainerRequestContext context)
    {
        String secret = context.getHeaderString(SecretProvider.API_SECRET_HEADER);
        this.context.setRawSecretData(secret);
        if (secret == null || !this.secretProvider.canReadSecret())
            return;

        String errorMessage = "Could not parse api-secret header";
        try
        {
            SecretData secretData = this.secretProvider.readSecret(secret);
            this.context.setSecretData(secretData);
            log.debug("Decoded api-secret for user '{}", secretData.getUserId().orElse("anonymous"));
        }
        catch (SecretProviderException exception)
        {
            log.warn(errorMessage, exception);
        }
        catch (ParseException exception)
        {
            Throwable cause = exception.getCause();
            if (cause == null)
            {
                log.warn(errorMessage, exception);
                return;
            }

            if (!(cause instanceof InvalidJwtException invalidJwt))
            {
                log.warn(errorMessage, exception);
                return;
            }

            if (invalidJwt.hasExpired())
            {
                log.debug("Rejected api-secret header: Token no longer valid");
                return;
            }

            log.warn(errorMessage, invalidJwt);
        }
    }
}
