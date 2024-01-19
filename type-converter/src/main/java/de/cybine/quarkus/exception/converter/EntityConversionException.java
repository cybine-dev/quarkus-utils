package de.cybine.quarkus.exception.converter;

import de.cybine.quarkus.exception.*;

@SuppressWarnings("unused")
public class EntityConversionException extends ServiceException
{
    public EntityConversionException(String message)
    {
        this(message, null);
    }

    public EntityConversionException(Throwable cause)
    {
        this(null, cause);
    }

    public EntityConversionException(String message, Throwable cause)
    {
        super("entity-conversion-error", 500, message, cause);
    }
}
