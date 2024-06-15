package de.cybine.quarkus.util.api.filter;

import jakarta.ws.rs.container.*;

public interface ResponseFilter
{
    void apply(final ContainerResponseContext response);
}
