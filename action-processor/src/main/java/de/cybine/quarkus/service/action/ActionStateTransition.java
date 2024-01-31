package de.cybine.quarkus.service.action;

import de.cybine.quarkus.data.action.process.*;
import lombok.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionStateTransition
{
    private final IActionService service;

    @Getter(AccessLevel.NONE)
    private final ActionProcessorMetadata metadata;

    private final ActionProcess         previousState;
    private final ActionProcessMetadata nextState;

    public String getNamespace( )
    {
        return this.metadata.getNamespace();
    }

    public String getCategory( )
    {
        return this.metadata.getCategory();
    }

    public String getName( )
    {
        return this.metadata.getName();
    }
}
