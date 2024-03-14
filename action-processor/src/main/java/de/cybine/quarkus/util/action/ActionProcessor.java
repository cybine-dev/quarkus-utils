package de.cybine.quarkus.util.action;

import de.cybine.quarkus.util.action.data.*;

public interface ActionProcessor
{
    ActionProcessorMetadata getMetadata( );

    boolean hasPermission(Action action, ActionHelper helper);

    boolean shouldExecute(Action action, ActionHelper helper);

    <T> ActionResult<T> apply(Action action, ActionHelper helper);
}
