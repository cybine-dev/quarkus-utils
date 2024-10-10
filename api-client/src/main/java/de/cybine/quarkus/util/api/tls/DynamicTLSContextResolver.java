package de.cybine.quarkus.util.api.tls;

import de.cybine.quarkus.config.api.*;
import de.cybine.quarkus.config.api.TLSConfig.*;
import jakarta.annotation.*;
import jakarta.inject.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class DynamicTLSContextResolver
{
    private final Map<String, DynamicTLSContext> contexts = new ConcurrentHashMap<>();

    private final TLSConfig config;

    @PostConstruct
    void setup( ) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, CertificateException
    {
        Path trustStorePath = this.config.defaultTrustStorePath();
        for (Map.Entry<String, TLSContext> entry : this.config.contexts().entrySet())
        {
            String contextName = entry.getKey();
            TLSContext contextConfig = entry.getValue();
            boolean verifyHostnames = contextConfig.verifyHostnames();

            DynamicTLSContext context = DynamicTLSContext.create(contextName, verifyHostnames, trustStorePath);
            if (!verifyHostnames)
                log.warn("Hostname verification is disabled for context '{}'", contextName);

            for (TLSConfig.TLSCertificate cert : contextConfig.trustCertificates())
            {
                String alias = cert.alias().orElseGet(UUID.randomUUID()::toString);
                X509Certificate certificate = DynamicTLSContext.createCertificate(cert.certificatePath());

                context.$addTrustCertificate(alias, certificate);
            }

            if (!contextConfig.trustCertificates().isEmpty())
                context.$reload();

            if (contextConfig.clientCertificate().isPresent())
            {
                TLSCertificate cert = contextConfig.clientCertificate().orElseThrow();
                PrivateKey key = DynamicTLSContext.createKey(cert.keyPath().orElseThrow());
                X509Certificate certificate = DynamicTLSContext.createCertificate(cert.certificatePath());

                context.updateClientCertificate(key, certificate);
            }

            this.contexts.put(contextName, context);
        }
    }

    public DynamicTLSContext registerContext(DynamicTLSContext context)
    {
        if (context == null)
            throw new IllegalArgumentException("context cannot be null");

        this.contexts.put(context.getName(), context);
        return context;
    }

    public Optional<DynamicTLSContext> findContext(String name)
    {
        return Optional.ofNullable(this.contexts.get(name));
    }
}
