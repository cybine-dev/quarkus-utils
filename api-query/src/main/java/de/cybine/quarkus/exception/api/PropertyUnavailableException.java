package de.cybine.quarkus.exception.api;

import de.cybine.quarkus.exception.*;
import org.jboss.resteasy.reactive.*;

@SuppressWarnings("unused")
public class PropertyUnavailableException extends ServiceException
{
    public PropertyUnavailableException(String message)
    {
        this(message, null);
    }

    public PropertyUnavailableException(String message, Throwable cause)
    {
        super("property-unavailable", RestResponse.Status.UNAUTHORIZED.getStatusCode(), message, cause);
    }
}
