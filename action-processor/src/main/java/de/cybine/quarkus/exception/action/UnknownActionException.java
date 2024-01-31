package de.cybine.quarkus.exception.action;

import de.cybine.quarkus.exception.*;

@SuppressWarnings("unused")
public class UnknownActionException extends ServiceException
{
    public UnknownActionException(String message)
    {
        this(message, null);
    }

    public UnknownActionException(String message, Throwable cause)
    {
        super("unknown-action", 404, message, cause);
    }
}
