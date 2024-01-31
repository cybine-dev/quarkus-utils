package de.cybine.quarkus.util.event.deployment;

import de.cybine.quarkus.util.event.*;
import io.quarkus.arc.deployment.*;
import io.quarkus.deployment.annotations.*;
import io.quarkus.deployment.builditem.*;

public class EventManagerExtensionProcessor
{
    private static final String FEATURE = "event-manager";

    @BuildStep
    public FeatureBuildItem feature( )
    {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public AdditionalBeanBuildItem createEventManager( )
    {
        return new AdditionalBeanBuildItem(EventManager.class);
    }
}
