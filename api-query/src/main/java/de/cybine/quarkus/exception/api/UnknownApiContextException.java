package de.cybine.quarkus.exception.api;

import de.cybine.quarkus.exception.*;

@SuppressWarnings("unused")
public class UnknownApiContextException extends ServiceException
{
    public UnknownApiContextException(String message)
    {
        this(message, null);
    }

    public UnknownApiContextException(String message, Throwable cause)
    {
        super("unknown-api-context", 400, message, cause);
    }
}
