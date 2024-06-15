package de.cybine.quarkus.config;

import de.cybine.quarkus.util.api.permission.*;
import io.quarkus.runtime.annotations.*;
import io.smallrye.config.*;

import static de.cybine.quarkus.util.FilePathHelper.*;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.cybine.api.query")
public interface ApiQueryConfig
{
    /**
     * Defines if it is allowed to write api-queries that result in nested joins in the sql output
     */
    @WithDefault("false")
    @WithName("allow-multi-level-relations")
    boolean allowMultiLevelRelations( );

    /**
     * Defines if failures during response property filtering will be ignored or stop the response
     */
    @WithDefault("false")
    @WithName("property-filter.ignore-failures")
    boolean ignorePropertyFilterFailures( );

    /**
     * Defines the scope of responses to which the property filter will be applied
     */
    @WithDefault("API_FIELDS")
    @WithName("property-filter.scope")
    PropertyFilterScope propertyFilterScope( );

    /**
     * Defines paths to additional configuration files
     */
    @WithName("paths")
    FilePaths paths( );

    interface FilePaths
    {
        /**
         * Defines the path to the RBAC (role-based access control) configuration file
         */
        @WithName("rbac-path")
        @WithDefault(RESOURCES_PLACEHOLDER + "/rbac.json")
        String rbacPath( );

        /**
         * Defines the path to the api-permission file
         */
        @WithName("api-permissions-path")
        @WithDefault(RESOURCES_PLACEHOLDER + "/api-permissions.json")
        String apiPermissionsPath( );
    }
}
