package de.cybine.quarkus.util.api.filter;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.*;
import com.fasterxml.jackson.databind.node.*;
import de.cybine.quarkus.config.*;
import de.cybine.quarkus.exception.api.*;
import de.cybine.quarkus.util.api.*;
import jakarta.ws.rs.container.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.*;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.*;

@Slf4j
@RequiredArgsConstructor
public class GenericResponseFilter implements ResponseFilter
{
    private final ApiQueryConfig config;

    private final ApiFieldResolverContext context;

    private final ObjectMapper objectMapper;

    @Override
    public void apply(ContainerResponseContext response)
    {
        try
        {
            JsonNode node = this.getObjectMapper().valueToTree(response.getEntity());
            this.filter(node);
            response.setEntity(node.toString());
        }
        catch (PropertyFilterException exception)
        {
            if (this.config.ignorePropertyFilterFailures())
            {
                log.error("Could not filter properties", exception);
                return;
            }

            throw exception;
        }
    }

    private void filter(JsonNode jsonNode)
    {
        if (jsonNode == null)
            return;

        if (jsonNode instanceof ArrayNode node)
            this.filterArrayNode(node);
        else if (jsonNode instanceof ObjectNode node)
            this.filterObjectNode(node);
    }

    private void filterArrayNode(ArrayNode node)
    {
        assert node != null;

        for (int i = 0; i < node.size(); i++)
        {
            JsonNode childNode = node.get(i);
            if (childNode instanceof ArrayNode arrayNode && !arrayNode.isEmpty())
                node.set(i, this.removeTypeInfoFromArray(arrayNode));

            this.filter(node.get(i));
        }
    }

    private void filterObjectNode(ObjectNode node)
    {
        assert node != null;

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

            if (!this.context.isAvailable(type, field))
            {
                log.trace("Removing field {} from response: insufficient permissions", field);
                node.remove(field);
                continue;
            }

            JsonNode childNode = node.get(field);
            if (childNode instanceof ArrayNode arrayNode && !arrayNode.isEmpty())
                node.set(field, this.removeTypeInfoFromArray(arrayNode));

            this.filter(node.get(field));
        }
    }

    private JsonNode removeTypeInfoFromArray(ArrayNode node)
    {
        assert node != null;

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
