package de.cybine.quarkus.util.action.stateful;

import de.cybine.quarkus.util.action.*;
import de.cybine.quarkus.util.action.data.*;

import java.util.*;

public interface StatefulActionService extends ActionService
{
    void registerWorkflow(Workflow workflow);

    Optional<Workflow> findWorkflow(String namespace, String category, String name);

    String beginWorkflow(String namespace, String category, String name);

    String beginWorkflow(String namespace, String category, String name, String itemId);

    List<ActionProcessorMetadata> availableActions(String correlationId);
}