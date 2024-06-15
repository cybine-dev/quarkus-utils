package de.cybine.quarkus.config;

import com.fasterxml.jackson.databind.*;
import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.filter.*;
import io.quarkus.arc.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.inject.*;
import jakarta.inject.*;
import lombok.*;

@Dependent
@RequiredArgsConstructor
public class ApiQueryFilterConfig
{
    private final ApiQueryConfig config;

    private final ApiFieldResolverContext context;

    private final ObjectMapper objectMapper;

    @Produces
    @Singleton
    @DefaultBean
    public ResponseFilter responseFilter( )
    {
        return switch (this.config.propertyFilterScope())
        {
            case ALL -> new GenericResponseFilter(this.config, this.context, this.objectMapper);
            case API_FIELDS -> new ApiFieldResponseFilter(this.config, this.context, this.objectMapper);
            case NONE -> new EmptyResponseFilter();
        };
    }
}
