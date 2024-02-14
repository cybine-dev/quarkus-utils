package de.cybine.quarkus.util.action.stateful;

import de.cybine.quarkus.util.action.*;
import de.cybine.quarkus.util.action.data.*;
import lombok.*;

import java.util.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkflowBuilder
{
    private final String namespace;
    private final String category;
    private final String name;

    private final List<ActionProcessor> workflowSteps = new ArrayList<>();

    private String initialState = Workflow.INITIAL_STATE;

    private WorkflowType type;

    public WorkflowBuilder initialState(String initialState)
    {
        this.initialState = initialState;
        return this;
    }

    public WorkflowBuilder type(WorkflowType type)
    {
        this.type = type;
        return this;
    }

    public WorkflowBuilder with(ActionProcessorBuilder processorBuilder)
    {
        ActionProcessor processor = processorBuilder.namespace(this.namespace)
                                                    .category(this.category)
                                                    .name(this.name)
                                                    .build();

        ActionProcessorMetadata metadata = processor.getMetadata();
        if (metadata.isStateless())
            throw new IllegalArgumentException("A workflow may not contain stateless processors.");

        if (this.findProcessor(metadata).isPresent())
            throw new IllegalStateException("The workflow already contains a processor with the same metadata");

        this.workflowSteps.add(processor);
        return this;
    }

    public void apply(StatefulActionService service)
    {
        for (ActionProcessor step : this.workflowSteps)
        {
            if (service.findProcessor(step.getMetadata()).isPresent())
                continue;

            service.registerProcessor(step);
        }

        service.registerWorkflow(Workflow.builder()
                                         .namespace(this.namespace)
                                         .category(this.category)
                                         .name(this.name)
                                         .initialState(this.initialState)
                                         .type(this.type)
                                         .workflowSteps(
                                                 this.workflowSteps.stream().map(ActionProcessor::getMetadata).toList())
                                         .build());
    }

    private Optional<ActionProcessor> findProcessor(ActionProcessorMetadata criteria)
    {
        return this.workflowSteps.stream().filter(item -> item.equals(criteria)).findAny();
    }

    public static WorkflowBuilder create(String type)
    {
        if (type == null)
            throw new IllegalArgumentException("A workflow requires a type.");

        String[] args = type.split(":");
        if (args.length < 3)
            throw new IllegalArgumentException("A workflow type must have at least 3 parts separated by a colon");

        return WorkflowBuilder.create(args[ 0 ], args[ 1 ], args[ 2 ]);
    }

    public static WorkflowBuilder create(String namespace, String category, String name)
    {
        if (namespace == null || namespace.isBlank())
            throw new IllegalArgumentException("A workflow requires a namespace.");

        if (category == null || category.isBlank())
            throw new IllegalArgumentException("A workflow requires a category.");

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("A workflow requires a name.");

        return new WorkflowBuilder(namespace, category, name);
    }
}
