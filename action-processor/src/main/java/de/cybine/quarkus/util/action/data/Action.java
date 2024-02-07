package de.cybine.quarkus.util.action.data;

import lombok.*;

import java.time.*;
import java.util.*;

@Data
@AllArgsConstructor(staticName = "of")
public class Action
{
    private final ActionMetadata metadata;

    private final ActionData<?> data;

    @SuppressWarnings("unchecked")
    public <T> Optional<ActionData<T>> getData( )
    {
        return Optional.ofNullable((ActionData<T>) this.data);
    }

    public String getNamespace( )
    {
        return this.metadata.getNamespace();
    }

    public String getCategory( )
    {
        return this.metadata.getCategory();
    }

    public String getName( )
    {
        return this.metadata.getName();
    }

    public String getAction( )
    {
        return this.metadata.getAction();
    }

    public String getEventId( )
    {
        return this.metadata.getEventId();
    }

    public Optional<Integer> getPriority( )
    {
        return this.metadata.getPriority();
    }

    public Optional<String> getItemId( )
    {
        return this.metadata.getItemId();
    }

    public Optional<String> getCorrelationId( )
    {
        return this.metadata.getCorrelationId();
    }

    public Optional<String> getSource( )
    {
        return this.metadata.getSource();
    }

    public Optional<String> getDescription( )
    {
        return this.metadata.getDescription();
    }

    public Optional<ZonedDateTime> getCreatedAt( )
    {
        return this.metadata.getCreatedAt();
    }

    public Optional<ZonedDateTime> getDueAt( )
    {
        return this.metadata.getDueAt();
    }

    public String toShortForm( )
    {
        return this.metadata.toShortForm();
    }
}
