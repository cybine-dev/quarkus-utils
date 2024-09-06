package de.cybine.quarkus.util.api;

import de.cybine.quarkus.config.*;
import de.cybine.quarkus.util.test.*;
import io.quarkus.test.*;
import io.quarkus.test.junit.*;
import jakarta.enterprise.inject.*;
import jakarta.inject.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

import static org.junit.jupiter.api.Assertions.*;

@TestProfile(TestProfiles.Integration.class)
class ApiQueryExtensionProcessorTest
{
    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().withEmptyApplication();

    @Inject
    Instance<ApiQueryConfig> configInstance;

    @Test
    void testApiQueryConfig()
    {
        assertFalse(this.configInstance.get().allowMultiLevelRelations());
    }
}