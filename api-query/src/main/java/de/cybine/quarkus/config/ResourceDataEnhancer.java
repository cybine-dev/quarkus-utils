package de.cybine.quarkus.config;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.*;
import com.fasterxml.jackson.databind.node.*;
import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.exception.api.*;
import de.cybine.quarkus.util.api.*;
import io.quarkus.arc.*;
import io.smallrye.config.common.utils.*;
import io.vertx.core.http.*;
import jakarta.inject.*;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.server.*;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.*;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.*;

@Slf4j
@Singleton
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ResourceDataEnhancer
{
    private final ApiPaginationInfo paginationInfo;

    private final ResourceInfo      resourceInfo;
    private final HttpServerRequest request;

    private final ObjectMapper objectMapper;

    @ServerRequestFilter
    public Optional<RestResponse<Void>> enhanceRequest(ContainerRequestContext context)
    {
        try
        {
            MultivaluedMap<String, String> queryParameters = context.getUriInfo().getQueryParameters();
            String size = queryParameters.getFirst("size");
            if (size != null && StringUtil.isNumeric(size))
                this.paginationInfo.setSize(Integer.valueOf(size));

            String offset = queryParameters.getFirst("offset");
            if (offset != null && StringUtil.isNumeric(offset))
                this.paginationInfo.setOffset(Integer.valueOf(offset));

            String includeTotal = queryParameters.getFirst("total");
            if (includeTotal != null)
                this.paginationInfo.includeTotal(includeTotal.equalsIgnoreCase("true"));
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

        try
        {
            ApiFieldResolverContext userContext = Arc.container().select(ApiFieldResolver.class).get().getUserContext();
            ObjectNode node = this.getObjectMapper().valueToTree(context.getEntity());
            this.filter(node, userContext);
            context.setEntity(node.toString());
        }
        catch (PropertyFilterException exception)
        {
            ApiQueryConfig config = Arc.container().select(ApiQueryConfig.class).get();
            if (config.ignorePropertyFilterFailures())
            {
                log.error("Could not filter properties", exception);
                return;
            }

            throw exception;
        }
    }

    private void filter(JsonNode jsonNode, ApiFieldResolverContext context)
    {
        if (jsonNode == null)
            return;

        if (jsonNode instanceof ArrayNode node)
        {
            for (int i = 0; i < node.size(); i++)
            {
                JsonNode childNode = node.get(i);
                if (childNode instanceof ArrayNode arrayNode)
                    node.set(i, this.removeTypeInfoFromArray(arrayNode));

                this.filter(node.get(i), context);
            }
        }
        else if (jsonNode instanceof ObjectNode node)
        {
            List<String> fieldNames = new ArrayList<>();
            Iterator<String> fields = node.fieldNames();
            while (fields.hasNext())
                fieldNames.add(fields.next());

            Class<?> type = this.getTypeFromNode(node);
            for (String field : fieldNames)
            {
                if (field.equals("@class"))
                {
                    node.remove(field);
                    continue;
                }

                if (!context.isAvailable(type, field))
                {
                    log.trace("Removing field {} from response: insufficient permissions", field);
                    node.remove(field);
                    continue;
                }

                JsonNode childNode = node.get(field);
                if (childNode instanceof ArrayNode arrayNode && !arrayNode.isEmpty())
                {
                    childNode = this.removeTypeInfoFromArray(arrayNode);
                    node.set(field, childNode);
                }

                this.filter(childNode, context);
            }
        }
    }

    private JsonNode removeTypeInfoFromArray(ArrayNode node)
    {
        JsonNode property = node.get(0);
        if (!property.isTextual())
            return node;

        if (!this.isClassName(property.asText()))
            return node;

        if (node.size() != 2)
            return node;

        log.trace("Detected array that only has two values beginning with a class name: rewriting to single value");
        return this.objectMapper.valueToTree(node.get(1));
    }

    private ObjectMapper getObjectMapper( )
    {
        PolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
                                                                          .allowIfBaseType(Object.class)
                                                                          .build();

        return this.objectMapper.copy().activateDefaultTyping(validator, NON_FINAL, PROPERTY);
    }

    private boolean isClassName(String className)
    {
        try
        {
            Class.forName(className, true, Thread.currentThread().getContextClassLoader());
            return true;
        }
        catch (ClassNotFoundException ignored)
        {
            return false;
        }
    }

    private Class<?> getTypeFromNode(ObjectNode node)
    {
        try
        {
            return Class.forName(node.get("@class").asText(), true, Thread.currentThread().getContextClassLoader());
        }
        catch (ClassNotFoundException | NullPointerException exception)
        {
            throw new PropertyFilterException("Unable to determine datatype", exception);
        }
    }
}
