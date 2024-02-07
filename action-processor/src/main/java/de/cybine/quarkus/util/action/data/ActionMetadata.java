package de.cybine.quarkus.util.action.data;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.data.util.*;
import lombok.*;

import java.time.*;
import java.util.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionMetadata
{
    @JsonProperty("namespace")
    private final String namespace;

    @JsonProperty("category")
    private final String category;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("action")
    private final String action;

    @JsonProperty("priority")
    private final Integer priority;

    @Builder.Default
    @JsonProperty("event_id")
    private final String eventId = UUIDv7.generate().toString();

    @JsonProperty("item_id")
    private final String itemId;

    @JsonProperty("correlation_id")
    private final String correlationId;

    @JsonProperty("source")
    private final String source;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("created_at")
    private final ZonedDateTime createdAt;

    @JsonProperty("due_at")
    private final ZonedDateTime dueAt;

    public Optional<Integer> getPriority( )
    {
        return Optional.ofNullable(this.priority);
    }

    public Optional<String> getItemId( )
    {
        return Optional.ofNullable(this.itemId);
    }

    public Optional<String> getCorrelationId( )
    {
        return Optional.ofNullable(this.correlationId);
    }

    public Optional<String> getSource( )
    {
        return Optional.ofNullable(this.source);
    }

    public Optional<String> getDescription( )
    {
        return Optional.ofNullable(this.description);
    }

    public Optional<ZonedDateTime> getCreatedAt( )
    {
        return Optional.ofNullable(this.createdAt);
    }

    public Optional<ZonedDateTime> getDueAt( )
    {
        return Optional.ofNullable(this.dueAt);
    }

    public String toShortForm( )
    {
        return String.format("%s:%s:%s:%s", this.namespace, this.category, this.name, this.action);
    }
}
