package de.cybine.quarkus.util.action.stateful;

import de.cybine.quarkus.util.action.data.*;
import lombok.*;

import java.util.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Workflow
{
    public static final String INITIAL_STATE = "initialized";

    private final String namespace;
    private final String category;
    private final String name;

    @Builder.Default
    private final String initialState = INITIAL_STATE;

    @Builder.Default
    private final WorkflowType type = WorkflowType.ACTION;

    @Singular("step")
    private final List<ActionProcessorMetadata> workflowSteps;

    public String toShortForm( )
    {
        return String.format("%s:%s:%s", this.namespace, this.category, this.name);
    }
}
