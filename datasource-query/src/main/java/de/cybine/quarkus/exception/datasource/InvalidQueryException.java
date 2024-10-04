package de.cybine.quarkus.exception.datasource;

import de.cybine.quarkus.exception.*;

public class InvalidQueryException extends ServiceException
{
    public InvalidQueryException(String message)
    {
        this(message, null);
    }

    public InvalidQueryException(String message, Throwable cause)
    {
        super("invalid-datasource-query", 400, message, cause);
    }
}
