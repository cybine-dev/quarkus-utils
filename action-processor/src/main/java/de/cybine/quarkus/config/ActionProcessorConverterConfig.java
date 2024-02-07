package de.cybine.quarkus.config;

import de.cybine.quarkus.util.cloudevent.*;
import de.cybine.quarkus.util.converter.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import lombok.*;

@Startup
@RequiredArgsConstructor
public class ActionProcessorConverterConfig
{
    private final ConverterRegistry registry;

    @PostConstruct
    void setup()
    {
        this.registry.addConverter(new CloudEventConverter());
    }
}
