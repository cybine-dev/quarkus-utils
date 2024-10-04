package de.cybine.quarkus.config;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jdk8.*;
import io.quarkus.jackson.*;
import jakarta.enterprise.context.*;

@Dependent
public class ObjectMapperConfig implements ObjectMapperCustomizer
{
    @Override
    public void customize(ObjectMapper objectMapper)
    {
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    }
}
