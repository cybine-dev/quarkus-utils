package de.cybine.quarkus.util.action.data;

import lombok.*;
import lombok.experimental.*;

import java.util.*;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionResult<T>
{
    private final ActionMetadata metadata;

    private final ActionData<T> data;

    @Accessors(fluent = true)
    private final boolean hasData;

    public Optional<ActionData<T>> getData( )
    {
        return Optional.ofNullable(this.data);
    }

    public static ActionResult<Object> empty(ActionMetadata metadata)
    {
        return new ActionResult<>(metadata, null, false);
    }

    public static <T> ActionResult<T> of(ActionMetadata metadata, ActionData<T> data)
    {
        return new ActionResult<>(metadata, data, true);
    }
}
