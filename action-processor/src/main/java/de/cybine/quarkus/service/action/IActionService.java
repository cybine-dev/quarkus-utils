package de.cybine.quarkus.service.action;

import de.cybine.quarkus.data.action.context.*;
import de.cybine.quarkus.data.action.metadata.*;
import de.cybine.quarkus.data.action.process.*;

import java.util.*;

public interface IActionService
{
    ActionContext createContext(ActionContextMetadata contextMetadata);

    void updateItemId(ActionContextId contextId, String itemId);

    <T> ActionProcessorResult<T> terminateContext(ActionContextId contextId);

    void bulkProcess(List<ActionProcessMetadata> states, boolean ignorePreconditionErrors);

    <T> T process(ActionProcessMetadata nextState);

    Optional<ActionMetadata> fetchMetadata(ActionContextMetadata context);

    Set<ActionContext> fetchActiveContexts(ActionContextMetadata context);

    Set<ActionContext> fetchContexts(ActionContextMetadata context);

    Optional<ActionProcess> fetchCurrentState(ActionContextId contextId);

    Optional<ActionProcess> fetchCurrentState(String correlationId);

    List<String> fetchAvailableActions(String correlationId);
}
