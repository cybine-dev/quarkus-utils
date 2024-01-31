package de.cybine.quarkus.util.action.deployment;

import de.cybine.quarkus.config.*;
import de.cybine.quarkus.service.action.*;
import de.cybine.quarkus.service.action.data.*;
import io.quarkus.arc.deployment.*;
import io.quarkus.deployment.annotations.*;
import io.quarkus.deployment.builditem.*;

public class ActionProcessorExtensionProcessor
{
    private static final String FEATURE = "action-processor";

    @BuildStep
    public FeatureBuildItem feature( )
    {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public AdditionalBeanBuildItem createActionConfig()
    {
        return new AdditionalBeanBuildItem(ActionProcessorConfig.class);
    }

    @BuildStep
    public AdditionalBeanBuildItem createDataTypeRegistry()
    {
        return new AdditionalBeanBuildItem(ActionDataTypeRegistry.class);
    }

    @BuildStep
    public AdditionalBeanBuildItem createProcessorRegistry()
    {
        return new AdditionalBeanBuildItem(ActionProcessorRegistry.class);
    }
}
