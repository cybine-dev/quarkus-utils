package de.cybine.quarkus.util.api.filter;

import io.quarkus.arc.*;
import jakarta.ws.rs.container.*;

@Unremovable
public class EmptyResponseFilter implements ResponseFilter
{
    @Override
    public void apply(ContainerResponseContext response)
    {

    }
}
