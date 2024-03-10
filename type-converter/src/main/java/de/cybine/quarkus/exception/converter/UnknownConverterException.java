package de.cybine.quarkus.exception.converter;

import de.cybine.quarkus.exception.*;

@SuppressWarnings("unused")
public class UnknownConverterException extends ServiceException
{
    public UnknownConverterException(String message)
    {
        this(message, null);
    }

    public UnknownConverterException(Throwable cause)
    {
        this(null, cause);
    }

    public UnknownConverterException(String message, Throwable cause)
    {
        super("unknown-converter", 500, message, cause);
    }
}
