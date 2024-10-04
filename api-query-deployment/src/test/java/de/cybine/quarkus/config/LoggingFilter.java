package de.cybine.quarkus.config;

import com.fasterxml.jackson.databind.*;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.io.*;

@Slf4j
@Provider
@RequiredArgsConstructor
public class LoggingFilter implements ContainerResponseFilter
{
    @Context
    UriInfo info;

    private final ObjectMapper objectMapper;

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException
    {
        log.debug("{}: {}", this.info.getAbsolutePath().toString(),
                this.objectMapper.writeValueAsString(response.getEntity()));
    }
}
