package de.cybine.quarkus.config;

import de.cybine.quarkus.util.converter.ConverterConstraint.*;
import io.quarkus.runtime.annotations.*;
import io.smallrye.config.*;

@ConfigMapping(prefix = "quarkus.cybine.converter")
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public interface TypeConverterConfig
{
    /**
     * Defines default maximum of nested objects to consider while executing converters. Additional layers of nested
     * objects will be ignored.
     */
    @WithDefault("5")
    @WithName("default.max-depth")
    int maxDepth( );

    /**
     * Defines if null values will be inserted into collections.
     */
    @WithDefault("true")
    @WithName("default.filter-null-values")
    boolean filterNullValues( );

    /**
     * Defines if converters may produce empty collections. If false, empty collections will be replaced with null.
     */
    @WithDefault("false")
    @WithName("default.allow-empty-collections")
    boolean allowEmptyCollections( );

    /**
     * Default policy to define handling of duplicate objects. If an object has been identified to be a duplicate of
     * another object further down in the conversion tree, the defined duplicate-policy will be used to handle a
     * possible loop.
     *
     * <ul>
     *     <li><b>PROCESS</b> ignores the output of the duplicate check and always executes the converter</li>
     *     <li><b>IGNORE_ITEM</b> skips a single item. While processing collections the item will be replaced with
     *     null.</li>
     *     <li><b>IGNORE_ALL</b> skips all items. While processing collections there will be no converted items.</li>
     * </ul>
     */
    @WithDefault("IGNORE_ALL")
    @WithName("default.duplicate-policy")
    DuplicatePolicy duplicatePolicy( );
}
