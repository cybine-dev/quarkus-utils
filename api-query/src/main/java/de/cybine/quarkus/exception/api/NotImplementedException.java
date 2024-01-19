package de.cybine.quarkus.exception.api;

import de.cybine.quarkus.exception.*;

@SuppressWarnings("unused")
public class NotImplementedException extends ServiceException
{
    public NotImplementedException( )
    {
        super("not-implemented", 501, "This method has not been implemented yet.");
    }
}
