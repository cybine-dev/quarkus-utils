package de.cybine.quarkus.util.converter.deployment;

import de.cybine.quarkus.util.converter.*;
import io.quarkus.test.*;
import jakarta.enterprise.inject.*;
import jakarta.inject.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConverterExtensionTest
{
    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().withEmptyApplication();

    @Inject
    Instance<ConverterRegistry> registryInstance;

    @Test
    void testConverterRegistry( )
    {
        ConverterRegistry registry = this.registryInstance.get();
        registry.addConverter(
                new GenericConverter<>(String.class, UUID.class, (input, helper) -> UUID.fromString(input)));

        ConversionProcessor<String, UUID> processor = registry.getProcessor(String.class, UUID.class);

        UUID id = UUID.randomUUID();
        UUID convertedId = assertDoesNotThrow(( ) -> processor.toItem(id.toString()).result());
        assertEquals(id, convertedId);
    }
}
