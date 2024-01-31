package de.cybine.quarkus.util.event;

import de.cybine.quarkus.util.event.handler.*;
import io.smallrye.mutiny.*;
import jakarta.annotation.*;
import jakarta.inject.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.util.*;

@Slf4j
@Getter
@Singleton
@RequiredArgsConstructor
public class EventManager
{
    private final List<EventGroup<?>>    groups   = new ArrayList<>();
    private final List<EventHandlerInfo> handlers = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    private final List<Class<? extends Subscriber>> subscribers = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    private final Comparator<EventGroup<?>> comparator = new EventGroup.PrecisionComparator();

    @PostConstruct
    void setup( )
    {
        this.registerEventGroup(new EventGroup<>(Event.class, CustomEventHandler::handle));
    }

    public void registerEventGroup(EventGroup<?> eventGroup)
    {
        if (eventGroup == null)
            throw new IllegalArgumentException("Failed registering event group: The event group must not be null!");

        if (this.groups.contains(eventGroup))
            throw new IllegalStateException(String.format(
                    "Failed registering event group for superclass %s: An event group for this superclass is already " +
                            "present!",
                    eventGroup.superclass().getSimpleName()));

        this.groups.add(eventGroup);
    }

    public void unregisterEventGroup(Class<?> superclass)
    {
        if (superclass == null)
            throw new IllegalArgumentException("Failed unregistering event group: The superclass must not be null!");

        Iterator<EventGroup<?>> iterator = this.groups.iterator();
        while (iterator.hasNext())
        {
            EventGroup<?> eventGroup = iterator.next();
            if (eventGroup.superclass() != superclass)
                continue;

            iterator.remove();
        }
    }

    public void registerHandlers(Subscriber eventListener)
    {
        if (eventListener == null)
            throw new IllegalArgumentException(
                    "Failed registering event handlers: The event listener must not be null!");

        List<EventHandlerInfo> handlerInfoList = Arrays.stream(eventListener.getClass().getMethods())
                                                       .filter(method -> method.isAnnotationPresent(EventHandler.class))
                                                       .filter(method -> method.getParameterCount() == 1)
                                                       .map(method -> new EventHandlerInfo(eventListener, method))
                                                       .toList();

        this.subscribers.add(eventListener.getClass());
        this.handlers.addAll(handlerInfoList);
    }

    public void unregisterHandlers(Class<?> listenerClass)
    {
        if (listenerClass == null)
            throw new IllegalArgumentException(
                    "Failed unregistering event handlers: The listener class must not be null!");

        if (!this.subscribers.removeIf(clazz -> clazz == listenerClass))
            return;

        this.handlers.removeIf(handler -> handler.listener().getClass() == listenerClass);
    }

    public boolean isHandled(Object event)
    {
        if (event == null)
            throw new IllegalArgumentException("Failed checking if event is handled: Event must not be null!");

        return this.groups.stream().anyMatch(group -> group.isHandled(event));
    }

    public <T> Uni<T> handle(T event)
    {
        if (event == null)
            throw new IllegalArgumentException("Failed calling event handlers: Event must not be null!");

        // noinspection unchecked
        return this.groups.stream()
                          .filter(group -> group.isHandled(event))
                          .min(this.comparator)
                          .map(group -> (Uni<T>) ((EventGroup<? super T>) group).handle(event, this))
                          .orElse(Uni.createFrom().item(event));
    }
}
