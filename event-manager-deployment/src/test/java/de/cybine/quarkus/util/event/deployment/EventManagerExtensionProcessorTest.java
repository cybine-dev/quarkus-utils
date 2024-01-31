package de.cybine.quarkus.util.event.deployment;

import de.cybine.quarkus.util.event.*;
import io.quarkus.test.*;
import jakarta.enterprise.inject.*;
import jakarta.inject.*;
import lombok.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EventManagerExtensionProcessorTest
{
    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().withEmptyApplication();

    @Inject
    Instance<EventManager> eventManagerInstance;

    @Test
    void testEventManager( )
    {
        EventManager eventManager = this.eventManagerInstance.get();
        eventManager.registerHandlers(new TestSubscriber());

        UUID eventId = UUID.randomUUID();
        TestEvent event = new TestEvent(eventId);

        assertTrue(eventManager.isHandled(event));
        assertEquals(eventId, event.getId());

        TestEvent processedEvent = eventManager.handle(event).await().indefinitely();

        assertNotNull(processedEvent);
        assertNotEquals(eventId, processedEvent.getId());
    }

    @SuppressWarnings("unused")
    public static class TestSubscriber implements Subscriber
    {
        @EventHandler
        public void onEvent(TestEvent event)
        {
            event.setId(UUID.randomUUID());
        }
    }

    @Data
    @AllArgsConstructor
    public static class TestEvent implements Event
    {
        private UUID id;
    }
}