package de.cybine.quarkus.config.datasource;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.*;

import java.util.*;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.cybine.datasource.query")
public interface DatasourceQueryConfig
{
    /**
     *
     */
    @WithName("names")
    NamingStrategy naming( );

    interface NamingStrategy
    {
        /**
         *
         */
        @WithName("catalog")
        Map<String, String> catalog( );

        /**
         *
         */
        @WithName("schema")
        Map<String, String> schema( );

        /**
         *
         */
        @WithName("table")
        Map<String, String> table( );

        /**
         *
         */
        @WithName("sequence")
        Map<String, String> sequence( );

        /**
         *
         */
        @WithName("field")
        Map<String, String> field( );
    }
}
