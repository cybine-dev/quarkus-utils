package de.cybine.quarkus.util.action;

import de.cybine.quarkus.util.action.data.*;
import de.cybine.quarkus.util.action.stateful.*;
import io.quarkus.security.identity.*;
import lombok.*;

import java.util.*;
import java.util.function.*;

@AllArgsConstructor
public class ActionHelper
{
    private final ActionService service;

    private final ActionMetadata  metadata;
    private final ActionResult<?> previousState;

    private final SecurityIdentity securityIdentity;

    private final BiConsumer<String, String> updateItemId;

    @SuppressWarnings("unchecked")
    public <T extends ActionService> T getService( )
    {
        return (T) this.service;
    }

    public Optional<Workflow> getWorkflow( )
    {
        if (!(this.service instanceof StatefulActionService statefulService))
            throw new UnsupportedOperationException();

        return statefulService.findWorkflow(this.metadata.getNamespace(), this.metadata.getCategory(),
                this.metadata.getName());
    }

    public void updateItemId(String itemId)
    {
        if (this.updateItemId == null)
            throw new UnsupportedOperationException();

        this.updateItemId.accept(this.metadata.getCorrelationId().orElseThrow(), itemId);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<ActionResult<T>> getPrevious( )
    {
        return Optional.ofNullable((ActionResult<T>) this.previousState);
    }

    public Optional<SecurityIdentity> getSecurityIdentity( )
    {
        return Optional.ofNullable(this.securityIdentity);
    }

    public ActionResult<Object> createResult( )
    {
        return ActionResult.empty(this.metadata);
    }

    public <T> ActionResult<T> createResult(T data)
    {
        return ActionResult.of(this.metadata, ActionData.of(data));
    }
}
