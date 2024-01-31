package de.cybine.quarkus.exception.action;

import de.cybine.quarkus.exception.*;

@SuppressWarnings("unused")
public class ActionPreconditionException extends ServiceException
{
    public ActionPreconditionException(String message)
    {
        this(message, null);
    }

    public ActionPreconditionException(String message, Throwable cause)
    {
        super("action-precondition-unfulfilled", 400, message, cause);
    }
}
