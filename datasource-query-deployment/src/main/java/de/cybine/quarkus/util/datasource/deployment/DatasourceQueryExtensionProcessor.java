package de.cybine.quarkus.util.datasource.deployment;

import io.quarkus.deployment.annotations.*;
import io.quarkus.deployment.builditem.*;

public class DatasourceQueryExtensionProcessor
{
    private static final String FEATURE = "datasource-query";

    @BuildStep
    public FeatureBuildItem feature( )
    {
        return new FeatureBuildItem(FEATURE);
    }
}
