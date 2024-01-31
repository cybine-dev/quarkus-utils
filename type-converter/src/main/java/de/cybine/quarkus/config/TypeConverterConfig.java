package de.cybine.quarkus.config;

import de.cybine.quarkus.util.converter.ConverterConstraint.*;
import io.quarkus.runtime.annotations.*;
import io.smallrye.config.*;

@ConfigMapping(prefix = "quarkus.cybine.converter")
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public interface TypeConverterConfig
{
    /**
     *
     */
    @WithDefault("5")
    @WithName("default.max-depth")
    int maxDepth( );

    /**
     *
     */
    @WithDefault("true")
    @WithName("default.filter-null-values")
    boolean filterNullValues( );

    /**
     *
     */
    @WithDefault("false")
    @WithName("default.allow-empty-collections")
    boolean allowEmptyCollections( );

    /**
     *
     */
    @WithDefault("IGNORE_ALL")
    @WithName("default.duplicate-policy")
    DuplicatePolicy duplicatePolicy( );
}
