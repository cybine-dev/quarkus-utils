package de.cybine.quarkus.util.action.stateless;

import de.cybine.quarkus.exception.action.*;
import de.cybine.quarkus.util.action.*;
import de.cybine.quarkus.util.action.data.*;
import io.quarkus.security.identity.*;
import jakarta.inject.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.util.*;
import java.util.stream.*;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class GenericStatelessActionService implements StatelessActionService
{
    private final List<ActionProcessor> processors = new ArrayList<>();

    private final SecurityIdentity securityIdentity;

    @Override
    public void registerProcessor(ActionProcessor processor)
    {
        if (processor.getMetadata().isStateful())
            throw new IllegalArgumentException("Cannot register stateful processors in a stateless service.");

        if (this.findProcessor(processor.getMetadata()).isPresent())
            throw new DuplicateProcessorDefinitionException("Processor already present");

        this.processors.add(processor);
    }

    @Override
    public Optional<ActionProcessor> findProcessor(ActionProcessorMetadata metadata)
    {
        return this.processors.stream()
                              .filter(item -> item.getMetadata().isApplicable(metadata.toShortForm()))
                              .findAny();
    }

    @Override
    public List<ActionResult<?>> bulkPerform(List<Action> actions)
    {
        return actions.stream().map(this::perform).collect(Collectors.toList());
    }

    @Override
    public <T> ActionResult<T> perform(Action action)
    {
        ActionProcessor processor = this.findProcessor(action).orElse(null);
        if (processor == null)
            throw new UnknownActionException(String.format("Action of type %s is unknown", action.toShortForm()));

        ActionMetadata metadata = action.getMetadata();
        ActionHelper helper = new ActionHelper(this, metadata, null, this.securityIdentity, null);
        if (!processor.hasPermission(action, helper))
            throw new UnauthorizedActionException("Executor is not allowed to perform this action.");

        if (!processor.shouldExecute(action, helper))
            throw new ActionPreconditionException("Action did not meet precondition.");

        return processor.apply(action, helper);
    }

    private Optional<ActionProcessor> findProcessor(Action action)
    {
        return this.processors.stream().filter(item -> item.getMetadata().isApplicable(action.toShortForm())).findAny();
    }
}
