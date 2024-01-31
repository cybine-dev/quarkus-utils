package de.cybine.quarkus.util.event.handler;

import de.cybine.quarkus.util.event.*;
import io.smallrye.mutiny.*;
import lombok.experimental.*;
import lombok.extern.slf4j.*;

import java.lang.reflect.*;
import java.util.*;

@Slf4j
@UtilityClass
public class CustomEventHandler
{
    public static Uni<Event> handle(Event event, Collection<EventHandlerInfo> handlers, EventManager eventManager)
    {
        return Uni.createFrom()
                  .item(event)
                  .onItem()
                  .invoke(item -> handlers.forEach(handler -> handleGracefully(event, handler)));
    }

    private static void handleGracefully(Event event, EventHandlerInfo handler)
    {
        try
        {
            handler.call(event);
        }
        catch (InvocationTargetException | IllegalAccessException exception)
        {
            log.error("An error occurred while processing an event.", exception);
        }
    }
}
