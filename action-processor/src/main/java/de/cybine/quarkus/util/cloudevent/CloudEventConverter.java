package de.cybine.quarkus.util.cloudevent;

import de.cybine.quarkus.util.action.data.*;
import de.cybine.quarkus.util.cloudevent.CloudEvent.*;
import de.cybine.quarkus.util.converter.*;
import jakarta.ws.rs.core.*;
import lombok.*;

@RequiredArgsConstructor
public class CloudEventConverter implements Converter<Action, CloudEvent>
{
    @Override
    public Class<Action> getInputType( )
    {
        return Action.class;
    }

    @Override
    public Class<CloudEvent> getOutputType( )
    {
        return CloudEvent.class;
    }

    @Override
    public CloudEvent convert(Action input, ConversionHelper helper)
    {
        Generator builder = CloudEvent.builder()
                                      .id(input.getEventId())
                                      .type(input.toShortForm())
                                      .subject(input.getItemId().orElse(null))
                                      .source(input.getSource().orElse(null))
                                      .time(input.getCreatedAt().orElse(null))
                                      .correlationId(input.getCorrelationId().orElse(null))
                                      .priority(input.getPriority().orElse(null));

        input.getData().ifPresent(data -> builder.contentType(MediaType.APPLICATION_JSON).data(data));

        return builder.build();
    }
}
