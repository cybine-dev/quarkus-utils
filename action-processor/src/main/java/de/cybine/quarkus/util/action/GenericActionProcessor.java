package de.cybine.quarkus.util.action;

import de.cybine.quarkus.util.action.data.*;
import lombok.*;

import java.util.function.*;

@AllArgsConstructor
public class GenericActionProcessor implements ActionProcessor
{
    @Getter
    private final ActionProcessorMetadata metadata;

    private final BiPredicate<Action, ActionHelper>                 executionCondition;
    private final BiFunction<Action, ActionHelper, ActionResult<?>> executor;

    @Override
    public boolean shouldExecute(Action action, ActionHelper helper)
    {
        return this.executionCondition.test(action, helper);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ActionResult<T> apply(Action action, ActionHelper helper)
    {
        return (ActionResult<T>) this.executor.apply(action, helper);
    }
}
