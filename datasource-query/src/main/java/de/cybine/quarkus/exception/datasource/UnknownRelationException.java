package de.cybine.quarkus.exception.datasource;

import de.cybine.quarkus.exception.*;

@SuppressWarnings("unused")
public class UnknownRelationException extends ServiceException
{
    public UnknownRelationException(String message)
    {
        this(message, null);
    }

    public UnknownRelationException(String message, Throwable cause)
    {
        super("unknown-relation", 400, message, cause);
    }
}
