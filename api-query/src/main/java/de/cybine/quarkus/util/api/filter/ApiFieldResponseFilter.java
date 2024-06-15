package de.cybine.quarkus.util.api.filter;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.config.*;
import de.cybine.quarkus.exception.api.*;
import de.cybine.quarkus.util.api.*;
import jakarta.inject.*;
import jakarta.ws.rs.container.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.lang.reflect.*;
import java.util.*;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class ApiFieldResponseFilter implements ResponseFilter
{
    private final ApiQueryConfig config;

    private final ApiFieldResolverContext context;

    private final ObjectMapper objectMapper;

    @Override
    public void apply(ContainerResponseContext response)
    {
        try
        {
            Object entity = response.getEntity();
            JsonNode node = this.objectMapper.valueToTree(entity);
            if (entity instanceof ApiResponse<?> apiResponse)
            {
                Object value = apiResponse.getValue();
                while (value instanceof Collection<?> collection && !collection.isEmpty())
                    value = collection.iterator().next();

                this.filter(node.get("value"), value.getClass());
            }
            else
            {
                this.filter(node, entity.getClass());
            }

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

    private void filter(JsonNode jsonNode, Type type)
    {
        assert type != null;
        if (jsonNode == null)
            return;

        if (jsonNode instanceof ArrayNode node)
            this.filterArrayNode(node, type);
        else if (jsonNode instanceof ObjectNode node)
            this.filterObjectNode(node, type);
    }

    private void filterArrayNode(ArrayNode node, Type type)
    {
        assert node != null;
        assert type != null;

        node.elements().forEachRemaining(item -> this.filter(item, type));
    }

    private void filterObjectNode(ObjectNode node, Type type)
    {
        assert node != null;
        assert type != null;

        List<String> fieldNames = new ArrayList<>();
        Iterator<String> fields = node.fieldNames();
        while (fields.hasNext())
            fieldNames.add(fields.next());

        for (String field : fieldNames)
        {
            if (!this.context.isAvailable(type, field))
            {
                log.trace("Removing field {} from response: insufficient permissions", field);
                node.remove(field);
                continue;
            }

            this.context.findField(type, field)
                        .map(ApiFieldPath::getLast)
                        .flatMap(ApiField::getFieldType)
                        .ifPresent(childType -> this.filter(node.get(field), childType));
        }
    }
}
