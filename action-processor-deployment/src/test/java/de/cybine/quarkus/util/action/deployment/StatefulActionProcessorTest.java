package de.cybine.quarkus.util.action.deployment;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import de.cybine.quarkus.exception.action.*;
import de.cybine.quarkus.util.action.data.*;
import de.cybine.quarkus.util.action.deployment.data.action.context.*;
import de.cybine.quarkus.util.action.deployment.data.action.process.*;
import de.cybine.quarkus.util.action.deployment.service.action.*;
import de.cybine.quarkus.util.action.stateful.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.test.*;
import io.quarkus.arc.*;
import io.quarkus.test.junit.*;
import lombok.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static de.cybine.quarkus.util.action.ActionProcessorBuilder.*;
import static de.cybine.quarkus.util.action.data.ActionProcessorMetadata.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@RequiredArgsConstructor
@TestProfile(TestProfiles.Container.class)
class StatefulActionProcessorTest
{
    private final ActionService actionService;

    @BeforeAll
    static void setup( )
    {
        ConverterRegistry converterRegistry = Arc.container().select(ConverterRegistry.class).get();
        converterRegistry.addEntityMapper(new ActionContextMapper());
        converterRegistry.addEntityMapper(new ActionProcessMapper());

        TypeFactory typeFactory = Arc.container().select(ObjectMapper.class).get().getTypeFactory();
        ActionDataTypeRegistry typeRegistry = Arc.container().select(ActionDataTypeRegistry.class).get();
        typeRegistry.registerType("string", typeFactory.constructType(String.class));

        StatefulActionService actionService = Arc.container().select(StatefulActionService.class).get();
        WorkflowBuilder.create("test", "test", "test")
                       .type(WorkflowType.LOG)
                       .with(on("log").from(ANY))
                       .with(on(ActionService.TERMINATED_STATE).from("log"))
                       .apply(actionService);
    }

    @Test
    @DisplayName("Workflow initializes successfully")
    void testWorkflowInitiation( )
    {
        String correlationId = this.actionService.beginWorkflow("test", "test", "test");
        ActionProcess initialState = this.actionService.fetchCurrentState(correlationId).orElse(null);

        assertNotNull(initialState);
        assertEquals(Workflow.INITIAL_STATE, initialState.getStatus());
    }

    @Test
    @DisplayName("Workflow executes successfully")
    void testWorkflowExecution( )
    {
        String correlationId = this.actionService.beginWorkflow("test", "test", "test");

        assertThrows(UnknownActionException.class, ( ) -> this.actionService.perform(
                Action.of(this.createMetadata(ActionService.TERMINATED_STATE, correlationId), null)));
        assertEquals(Workflow.INITIAL_STATE,
                this.actionService.fetchCurrentState(correlationId).map(ActionProcess::getStatus).orElseThrow());

        assertDoesNotThrow(
                ( ) -> this.actionService.perform(Action.of(this.createMetadata("log", correlationId), null)));
        assertEquals("log",
                this.actionService.fetchCurrentState(correlationId).map(ActionProcess::getStatus).orElseThrow());

        assertDoesNotThrow(( ) -> this.actionService.perform(
                Action.of(this.createMetadata(ActionService.TERMINATED_STATE, correlationId), null)));
        assertEquals(ActionService.TERMINATED_STATE,
                this.actionService.fetchCurrentState(correlationId).map(ActionProcess::getStatus).orElseThrow());
    }

    @Test
    @DisplayName("Workflow batch-processing executes successfully")
    void testWorkflowBatchExecution( )
    {
        String correlationId = this.actionService.beginWorkflow("test", "test", "test");
        List<Action> actions = List.of(Action.of(this.createMetadata("log", correlationId), null),
                Action.of(this.createMetadata(ActionService.TERMINATED_STATE, correlationId), null));

        assertDoesNotThrow(( ) -> this.actionService.bulkPerform(actions));
        assertEquals(ActionService.TERMINATED_STATE,
                this.actionService.fetchCurrentState(correlationId).map(ActionProcess::getStatus).orElseThrow());
    }

    @Test
    @DisplayName("Workflow batch-processing reverts on invalid action")
    void testInvalidWorkflowBatchExecution( )
    {
        String correlationId = this.actionService.beginWorkflow("test", "test", "test");
        List<Action> actions = List.of(Action.of(this.createMetadata("log", correlationId), null),
                Action.of(this.createMetadata(ActionService.TERMINATED_STATE, correlationId), null),
                Action.of(this.createMetadata("invalid", correlationId), null));

        assertThrows(UnknownActionException.class, ( ) -> this.actionService.bulkPerform(actions));
        assertEquals(Workflow.INITIAL_STATE,
                this.actionService.fetchCurrentState(correlationId).map(ActionProcess::getStatus).orElseThrow());
    }

    @Test
    @DisplayName("Available actions are determined correctly")
    void testAvailableActions( )
    {
        String correlationId = this.actionService.beginWorkflow("test", "test", "test");
        assertIterableEquals(List.of("log"), this.getActions(this.actionService.availableActions(correlationId)));

        this.actionService.perform(Action.of(this.createMetadata("log", correlationId), null));
        assertIterableEquals(List.of("log", ActionService.TERMINATED_STATE),
                this.getActions(this.actionService.availableActions(correlationId)));
    }

    @Test
    @DisplayName("Data is stored successfully")
    void testDataStorage( )
    {
        String correlationId = this.actionService.beginWorkflow("test", "test", "test");
        assertDoesNotThrow(( ) -> this.actionService.perform(
                Action.of(this.createMetadata("log", correlationId), ActionData.of("test"))));
        assertEquals("log",
                this.actionService.fetchCurrentState(correlationId).map(ActionProcess::getStatus).orElseThrow());

        ActionData<String> data = this.actionService.fetchCurrentState(correlationId)
                                                    .flatMap(ActionProcess::<String>getData)
                                                    .orElse(null);

        assertNotNull(data);
        assertEquals("test", data.value());
    }

    private ActionMetadata createMetadata(String action, String correlationId)
    {
        return ActionMetadata.builder()
                             .namespace("test")
                             .category("test")
                             .name("test")
                             .action(action)
                             .correlationId(correlationId)
                             .build();
    }

    private List<String> getActions(List<ActionProcessorMetadata> metadata)
    {
        return metadata.stream().map(ActionProcessorMetadata::getAction).toList();
    }
}
