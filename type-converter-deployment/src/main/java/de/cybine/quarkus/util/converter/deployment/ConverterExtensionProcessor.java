package de.cybine.quarkus.util.converter.deployment;

import de.cybine.quarkus.util.converter.*;
import io.quarkus.arc.deployment.*;
import io.quarkus.deployment.annotations.*;
import io.quarkus.deployment.builditem.*;

public class ConverterExtensionProcessor
{
    private static final String FEATURE = "type-converter";

    @BuildStep
    public FeatureBuildItem feature( )
    {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public AdditionalBeanBuildItem createConverterRegistry( )
    {
        return new AdditionalBeanBuildItem(ConverterRegistry.class);
    }
}
