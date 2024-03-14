package de.cybine.quarkus.config;

import de.cybine.quarkus.util.api.converter.*;
import de.cybine.quarkus.util.converter.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import lombok.*;

@Startup
@RequiredArgsConstructor
public class ApiQueryConverterConfig
{
    private final ConverterRegistry registry;

    private final ApiQueryConfig config;

    @PostConstruct
    public void setup( )
    {
        this.registry.addConverter(new ApiConditionDetailConverter());
        this.registry.addConverter(new ApiConditionDetailTypeConverter());
        this.registry.addConverter(new ApiConditionEvaluationMethodConverter());
        this.registry.addConverter(new ApiConditionInfoConverter());
        this.registry.addConverter(new ApiCountQueryConverter());
        this.registry.addConverter(new ApiCountRelationConverter());
        this.registry.addConverter(new ApiQueryConverter());
        this.registry.addConverter(new ApiOptionQueryConverter());
        this.registry.addConverter(new ApiOrderInfoConverter());
        this.registry.addConverter(new ApiOptionQueryConverter());
        this.registry.addConverter(new ApiPaginationInfoConverter());
        this.registry.addConverter(new ApiRelationInfoConverter(this.config));
        this.registry.addEntityMapper(new CountInfoMapper());
    }
}
