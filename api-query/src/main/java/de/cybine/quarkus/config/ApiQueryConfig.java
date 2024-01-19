package de.cybine.quarkus.config;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.*;

@ConfigMapping(prefix = "quarkus.cybine.api.query")
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public interface ApiQueryConfig
{
    /**
     *
     */
    @WithDefault("false")
    boolean allowMultiLevelRelations( );

    /**
     *
     */
    @WithName("paths")
    FilePaths paths( );

    interface FilePaths
    {
        /**
         *
         */
        @WithName("rbac-path")
        @WithDefault("%resources%/rbac.json")
        String rbacPath( );

        /**
         *
         */
        @WithName("api-permissions-path")
        @WithDefault("%resources%/api-permissions.json")
        String apiPermissionsPath( );
    }
}
