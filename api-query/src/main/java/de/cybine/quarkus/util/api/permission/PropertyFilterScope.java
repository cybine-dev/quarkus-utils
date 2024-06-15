package de.cybine.quarkus.util.api.permission;

import de.cybine.quarkus.util.api.*;

public enum PropertyFilterScope
{
    /**
     * Apply property filter to all response data
     */
    ALL,

    /**
     * Apply property filter only when data is defined as {@link ApiField}
     */
    API_FIELDS,

    /**
     * Disable property filter
     */
    NONE
}
