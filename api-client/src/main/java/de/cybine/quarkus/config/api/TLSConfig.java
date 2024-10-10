package de.cybine.quarkus.config.api;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.*;

import java.nio.file.*;
import java.util.*;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.cybine.api.tls")
public interface TLSConfig
{
    /**
     *
     */
    @WithName("default-trust-store-path")
    @WithDefault("${java.home}/lib/security/cacerts")
    Path defaultTrustStorePath( );

    /**
     *
     */
    @WithName("context")
    Map<String, TLSContext> contexts( );

    interface TLSContext
    {
        /**
         *
         */
        @WithDefault("true")
        @WithName("verify-hostnames")
        boolean verifyHostnames( );

        /**
         *
         */
        @WithName("client-certificate")
        Optional<TLSCertificate> clientCertificate( );

        /**
         *
         */
        @WithName("trust-certificates")
        Set<TLSCertificate> trustCertificates( );
    }

    interface TLSCertificate
    {
        /**
         *
         */
        @WithName("alias")
        Optional<String> alias( );

        /**
         *
         */
        @WithName("key-path")
        Optional<Path> keyPath( );

        /**
         *
         */
        @WithName("certificate-path")
        Path certificatePath( );
    }
}
