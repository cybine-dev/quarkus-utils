package de.cybine.quarkus.util.action;

import de.cybine.quarkus.util.action.data.*;
import lombok.*;

import java.util.function.*;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class GenericActionProcessor implements ActionProcessor
{
    @Getter
    private final ActionProcessorMetadata metadata;

    private final BiPredicate<Action, ActionHelper> permissionCheck;
    private final BiPredicate<Action, ActionHelper> executionCondition;

    private final BiFunction<Action, ActionHelper, ActionResult<?>> executor;

    @Override
    public boolean hasPermission(Action action, ActionHelper helper)
    {
        return this.permissionCheck.test(action, helper);
    }

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
