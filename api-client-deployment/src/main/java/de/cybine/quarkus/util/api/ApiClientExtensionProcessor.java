package de.cybine.quarkus.util.api;

import de.cybine.quarkus.util.api.tls.*;
import io.quarkus.arc.deployment.*;
import io.quarkus.deployment.annotations.*;
import io.quarkus.deployment.builditem.*;

public class ApiClientExtensionProcessor
{
    private static final String FEATURE = "api-client";

    @BuildStep
    public FeatureBuildItem feature( )
    {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public AdditionalBeanBuildItem createDynamicTLSContextResolver( )
    {
        return AdditionalBeanBuildItem.unremovableOf(DynamicTLSContextResolver.class);
    }
}
