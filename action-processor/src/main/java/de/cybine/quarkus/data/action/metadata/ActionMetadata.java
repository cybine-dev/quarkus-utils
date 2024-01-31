package de.cybine.quarkus.data.action.metadata;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import de.cybine.quarkus.data.action.context.*;
import de.cybine.quarkus.util.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionMetadata implements Serializable, WithId<ActionMetadataId>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @JsonDeserialize(using = ActionMetadataId.Deserializer.class)
    private final ActionMetadataId id;

    @JsonProperty("namespace")
    private final String namespace;

    @JsonProperty("category")
    private final String category;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("type")
    private final ActionType type;

    @JsonProperty("contexts")
    private final Set<ActionContext> contexts;

    public Optional<Set<ActionContext>> getContexts( )
    {
        return Optional.ofNullable(this.contexts);
    }

    @JsonProperty("context_ids")
    private Optional<Set<ActionContextId>> getContextIds( )
    {
        return this.getContexts().map(items -> items.stream().map(WithId::getId).collect(Collectors.toSet()));
    }

    @Override
    public boolean equals(Object other)
    {
        if(other == null)
            return false;

        if(this.getClass() != other.getClass())
            return false;

        WithId<?> that = ((WithId<?>) other);
        if (this.findId().isEmpty() || that.findId().isEmpty())
            return false;

        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode( )
    {
        return this.findId().map(Object::hashCode).orElse(0);
    }
}
