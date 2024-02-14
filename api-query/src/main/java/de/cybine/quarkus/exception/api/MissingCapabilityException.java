package de.cybine.quarkus.exception.api;

import de.cybine.quarkus.exception.*;
import org.jboss.resteasy.reactive.*;

@SuppressWarnings("unused")
public class MissingCapabilityException extends ServiceException
{
    public MissingCapabilityException(String message)
    {
        this(message, null);
    }

    public MissingCapabilityException(String message, Throwable cause)
    {
        super("missing-capability", RestResponse.Status.UNAUTHORIZED.getStatusCode(), message, cause);
    }
}
