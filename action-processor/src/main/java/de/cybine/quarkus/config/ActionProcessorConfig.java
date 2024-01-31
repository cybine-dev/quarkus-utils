package de.cybine.quarkus.config;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.*;
import jakarta.validation.constraints.*;

@ConfigMapping(prefix = "quarkus.cybine.action")
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public interface ActionProcessorConfig
{
    /**
     *
     */
    @NotNull @NotBlank
    @WithName("emitter-id")
    String emitterId( );
}
