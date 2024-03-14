package de.cybine.quarkus.exception.action;

import de.cybine.quarkus.exception.*;

@SuppressWarnings("unused")
public class UnauthorizedActionException extends ServiceException
{
    public UnauthorizedActionException(String message)
    {
        this(message, null);
    }

    public UnauthorizedActionException(String message, Throwable cause)
    {
        super("unauthorized-action", 401, message, cause);
    }
}
