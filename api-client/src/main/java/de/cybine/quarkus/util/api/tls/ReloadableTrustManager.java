package de.cybine.quarkus.util.api.tls;

import de.cybine.quarkus.exception.api.*;
import lombok.*;

import javax.net.ssl.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReloadableTrustManager implements X509TrustManager
{
    @Getter
    private final Path trustStorePath;

    private final boolean verifyHostnames;

    private final ConcurrentHashMap<String, X509Certificate> certificates = new ConcurrentHashMap<>();

    private final AtomicReference<X509TrustManager> trustManagerRef = new AtomicReference<>();

    public boolean isVerifyingHostnames( )
    {
        return this.verifyHostnames;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
    {
        if (!this.verifyHostnames)
            return;

        this.trustManagerRef.get().checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
    {
        if (!this.verifyHostnames)
            return;

        this.trustManagerRef.get().checkServerTrusted(chain, authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers( )
    {
        return this.trustManagerRef.get().getAcceptedIssuers();
    }

    public ReloadableTrustManager reload( ) throws TLSLoaderException
    {
        try
        {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            try (InputStream stream = Files.newInputStream(this.trustStorePath))
            {
                keyStore.load(stream, null);
            }

            for (Map.Entry<String, X509Certificate> item : this.certificates.entrySet())
                keyStore.setCertificateEntry(item.getKey(), item.getValue());

            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init(keyStore);

            this.trustManagerRef.set(Arrays.stream(factory.getTrustManagers())
                                           .filter(X509TrustManager.class::isInstance)
                                           .map(X509TrustManager.class::cast)
                                           .findAny()
                                           .orElseThrow(( ) -> new NoSuchAlgorithmException(
                                                   "No X509TrustManager in TrustManagerFactory")));

            return this;
        }
        catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException exception)
        {
            throw new TLSLoaderException(exception);
        }
    }

    public ReloadableTrustManager addCertificate(String alias, X509Certificate certificate)
    {
        this.certificates.put(alias, certificate);
        return this;
    }

    public ReloadableTrustManager removeCertificate(String alias)
    {
        this.certificates.remove(alias);
        return this;
    }

    public ReloadableTrustManager clearCertificates( )
    {
        this.certificates.clear();
        return this;
    }

    public static ReloadableTrustManager create(boolean verifyHostnames, Path trustStorePath)
    {
        return new ReloadableTrustManager(trustStorePath, verifyHostnames);
    }
}
