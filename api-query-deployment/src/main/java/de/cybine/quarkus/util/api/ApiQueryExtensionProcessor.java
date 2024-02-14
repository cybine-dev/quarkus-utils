package de.cybine.quarkus.util.api;

import de.cybine.quarkus.config.*;
import de.cybine.quarkus.util.api.permission.*;
import de.cybine.quarkus.util.api.query.*;
import io.quarkus.arc.deployment.*;
import io.quarkus.deployment.annotations.*;
import io.quarkus.deployment.builditem.*;
import io.quarkus.resteasy.reactive.spi.*;

public class ApiQueryExtensionProcessor
{
    private static final String FEATURE = "api-query";

    @BuildStep
    public FeatureBuildItem feature( )
    {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public AdditionalBeanBuildItem createApiFieldResolver( )
    {
        return new AdditionalBeanBuildItem(ApiFieldResolver.class);
    }

    @BuildStep
    public AdditionalBeanBuildItem createRBACResolver( )
    {
        return new AdditionalBeanBuildItem(RBACResolver.class);
    }

    @BuildStep
    public CustomContainerRequestFilterBuildItem createRequestDataEnhancer( )
    {
        return new CustomContainerRequestFilterBuildItem(ResourceDataEnhancer.class.getName());
    }

    @BuildStep
    public CustomContainerResponseFilterBuildItem createResponseDataEnhancer( )
    {
        return new CustomContainerResponseFilterBuildItem(ResourceDataEnhancer.class.getName());
    }

    @BuildStep
    public AdditionalBeanBuildItem createApiQueryConverterConfig( )
    {
        return new AdditionalBeanBuildItem(ApiQueryConverterConfig.class);
    }

    @BuildStep
    public AdditionalBeanBuildItem createApiPaginationInfo( )
    {
        return new AdditionalBeanBuildItem(ApiPaginationInfo.class);
    }
}
