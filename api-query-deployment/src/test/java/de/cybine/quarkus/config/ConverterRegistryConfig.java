package de.cybine.quarkus.config;

import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.data.customer.*;
import de.cybine.quarkus.data.library.*;
import de.cybine.quarkus.data.rental.*;
import de.cybine.quarkus.util.converter.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import lombok.*;

@Startup
@Dependent
@RequiredArgsConstructor
public class ConverterRegistryConfig
{
    private final ConverterRegistry registry;

    @PostConstruct
    void setup()
    {
        this.registry.addEntityMapper(new AssetMapper());
        this.registry.addEntityMapper(new RentalMapper());
        this.registry.addEntityMapper(new LibraryMapper());
        this.registry.addEntityMapper(new CustomerMapper());
    }
}
