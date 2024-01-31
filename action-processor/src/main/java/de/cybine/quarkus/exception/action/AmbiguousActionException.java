package de.cybine.quarkus.exception.action;

import de.cybine.quarkus.exception.*;

@SuppressWarnings("unused")
public class AmbiguousActionException extends ServiceException
{
    public AmbiguousActionException(String message)
    {
        this(message, null);
    }

    public AmbiguousActionException(String message, Throwable cause)
    {
        super("ambiguous-action", 409, message, cause);
    }
}
