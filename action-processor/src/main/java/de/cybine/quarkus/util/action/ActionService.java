package de.cybine.quarkus.util.action;

import de.cybine.quarkus.util.action.data.*;

import java.util.*;

public interface ActionService
{
    void registerProcessor(ActionProcessor processor);

    Optional<ActionProcessor> findProcessor(ActionProcessorMetadata metadata);

    List<ActionResult<?>> bulkPerform(List<Action> actions);

    <T> ActionResult<T> perform(Action action);
}
