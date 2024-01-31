package de.cybine.quarkus.service.action;

public interface ActionProcessor<T>
{
    ActionProcessorMetadata getMetadata( );

    boolean shouldExecute(ActionStateTransition transition);

    ActionProcessorResult<T> process(ActionStateTransition transition);
}
