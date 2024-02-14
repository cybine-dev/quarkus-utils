package de.cybine.quarkus.exception.api;

import de.cybine.quarkus.exception.*;
import org.jboss.resteasy.reactive.*;

@SuppressWarnings("unused")
public class UnauthorizedActionException extends ServiceException
{
    public UnauthorizedActionException(String message)
    {
        this(message, null);
    }

    public UnauthorizedActionException(String message, Throwable cause)
    {
        super("action-not-available", RestResponse.Status.UNAUTHORIZED.getStatusCode(), message, cause);
    }
}
