package de.cybine.quarkus.exception.api;

import de.cybine.quarkus.exception.*;

@SuppressWarnings("unused")
public class IllegalRequestParameterException extends ServiceException
{
    public IllegalRequestParameterException(String message)
    {
        this(message, null);
    }

    public IllegalRequestParameterException(String message, Throwable cause)
    {
        super("illegal-request-parameter", 400, message, cause);
    }
}
