package de.cybine.quarkus.util.action.data;

import de.cybine.quarkus.util.action.*;
import lombok.*;
import lombok.experimental.*;

import java.util.*;
import java.util.function.*;

// TODO add setters for namespace, category, name
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionProcessorBuilder
{
    @Setter
    private String namespace;

    @Setter
    private String category;

    @Setter
    private String name;

    private final String action;

    private String from;

    private BiPredicate<Action, ActionHelper> condition = (action, helper) -> true;

    private BiFunction<Action, ActionHelper, ActionResult<?>> executor = (action, helper) -> action.getData()
                                                                                                   .map(ActionData::value)
                                                                                                   .map(helper::createResult)
                                                                                                   .orElse(helper.createResult());

    private ActionProcessorBuilder(String namespace, String category, String name, String action)
    {
        this.namespace = namespace;
        this.category = category;
        this.name = name;
        this.action = action;
    }

    public ActionProcessorBuilder from(String from)
    {
        this.from = from;
        return this;
    }

    public ActionProcessorBuilder when(BiPredicate<Action, ActionHelper> condition)
    {
        this.condition = condition;
        return this;
    }

    public ActionProcessorBuilder apply(BiFunction<Action, ActionHelper, ActionResult<?>> executor)
    {
        this.executor = executor;
        return this;
    }

    public ActionProcessor build( )
    {
        return new GenericActionProcessor(this.toMetadata(), this.condition, this.executor);
    }

    private ActionProcessorMetadata toMetadata( )
    {
        return ActionProcessorMetadata.builder()
                                      .namespace(this.namespace)
                                      .category(this.category)
                                      .name(this.name)
                                      .action(this.action)
                                      .from(this.from)
                                      .build();
    }

    public static ActionProcessorBuilder on(String action)
    {
        return new ActionProcessorBuilder(action);
    }

    public static ActionProcessorBuilder on(String namespace, String category, String name, String action)
    {
        return new ActionProcessorBuilder(namespace, category, name, action);
    }

    @SafeVarargs
    public static BiPredicate<ActionMetadata, ActionHelper> all(BiPredicate<ActionMetadata, ActionHelper>... conditions)
    {
        return (metadata, helper) -> Arrays.stream(conditions).allMatch(condition -> condition.test(metadata, helper));
    }

    @SafeVarargs
    public static BiPredicate<ActionMetadata, ActionHelper> any(BiPredicate<ActionMetadata, ActionHelper>... conditions)
    {
        return (metadata, helper) -> Arrays.stream(conditions).anyMatch(condition -> condition.test(metadata, helper));
    }
}
