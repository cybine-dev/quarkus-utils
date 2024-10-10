package de.cybine.quarkus.util.api.tls;

import de.cybine.quarkus.exception.api.*;
import lombok.*;

import javax.net.ssl.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.util.*;
import java.util.concurrent.atomic.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicTLSContext
{
    @Getter
    private final String name;

    private final ReloadableKeyManager   keyManager;
    private final ReloadableTrustManager trustManager;

    private final AtomicReference<SSLContext> contextRef = new AtomicReference<>();

    private DynamicTLSContext init( )
    {
        try
        {
            KeyManager[] keyManagers = { this.keyManager };
            TrustManager[] trustManagers = { this.trustManager };

            this.contextRef.set(SSLContext.getInstance("TLS"));
            this.contextRef.get().init(keyManagers, trustManagers, SecureRandom.getInstanceStrong());

            return this;
        }
        catch (KeyManagementException | NoSuchAlgorithmException exception)
        {
            throw new TLSLoaderException(exception);
        }
    }

    public SSLContext getContext( )
    {
        return this.contextRef.get();
    }

    public boolean isVerifyingHostnames( )
    {
        return this.trustManager.isVerifyingHostnames();
    }

    public DynamicTLSContext updateClientCertificate(PrivateKey key, X509Certificate... chain)
    {
        this.keyManager.updateCertificate(new KeyStore.PrivateKeyEntry(key, chain));
        return this;
    }

    public DynamicTLSContext addTrustCertificate(String alias, X509Certificate certificate)
    {
        this.trustManager.addCertificate(alias, certificate);
        this.trustManager.reload();
        return this;
    }

    public DynamicTLSContext removeTrustCertificate(String alias)
    {
        this.trustManager.removeCertificate(alias);
        this.trustManager.reload();
        return this;
    }

    public DynamicTLSContext clearTrustCertificates( )
    {
        this.trustManager.clearCertificates();
        this.trustManager.reload();
        return this;
    }

    void $addTrustCertificate(String alias, X509Certificate certificate)
    {
        this.trustManager.addCertificate(alias, certificate);
    }

    void $reload()
    {
        this.trustManager.reload();
    }

    public static DynamicTLSContext create(String name, boolean verifyHostnames, Path trustStorePath)
    {
        ReloadableKeyManager keyManager = ReloadableKeyManager.empty();
        ReloadableTrustManager trustManager = ReloadableTrustManager.create(verifyHostnames, trustStorePath);
        return new DynamicTLSContext(name, keyManager, trustManager).init();
    }

    public static X509Certificate createCertificate(byte[] certificate) throws CertificateException, IOException
    {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(certificate))
        {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(stream);
        }
    }

    public static X509Certificate createCertificate(Path certificatePath) throws CertificateException, IOException
    {
        String content = Files.readString(certificatePath)
                              .replaceAll("-----\\w+ CERTIFICATE-----", "")
                              .replaceAll("\\s+", "");

        return DynamicTLSContext.createCertificate(Base64.getDecoder().decode(content));
    }

    public static PrivateKey createKey(byte[] privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory factory = KeyFactory.getInstance("RSA");

        return factory.generatePrivate(spec);
    }

    public static PrivateKey createKey(Path keyPath)
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException
    {
        String content = Files.readString(keyPath)
                              .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                              .replaceAll("\\s+", "");

        return DynamicTLSContext.createKey(Base64.getDecoder().decode(content));
    }
}
