package de.cybine.quarkus.util.api.tls;

import lombok.*;

import javax.net.ssl.*;
import java.net.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.util.*;
import java.util.concurrent.atomic.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReloadableKeyManager implements X509KeyManager
{
    private final String alias = UUID.randomUUID().toString();

    private final AtomicReference<KeyStore.PrivateKeyEntry> key = new AtomicReference<>();

    private Optional<KeyStore.PrivateKeyEntry> getKey( )
    {
        return Optional.ofNullable(this.key.get());
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] principals)
    {
        return new String[] { this.alias };
    }

    @Override
    public String chooseClientAlias(String[] keyTypes, Principal[] principals, Socket socket)
    {
        return this.alias;
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] principals)
    {
        return new String[] { this.alias };
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] principals, Socket socket)
    {
        return this.alias;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias)
    {
        if (!this.alias.equals(alias))
            return null;

        return this.getKey()
                   .map(KeyStore.PrivateKeyEntry::getCertificateChain)
                   .map(X509Certificate[].class::cast)
                   .orElse(null);
    }

    @Override
    public PrivateKey getPrivateKey(String alias)
    {
        if (!this.alias.equals(alias))
            return null;

        return this.getKey().map(KeyStore.PrivateKeyEntry::getPrivateKey).orElse(null);
    }

    public ReloadableKeyManager updateCertificate(KeyStore.PrivateKeyEntry key)
    {
        this.key.set(key);
        return this;
    }

    public static ReloadableKeyManager empty( )
    {
        return new ReloadableKeyManager();
    }

    public static ReloadableKeyManager of(PrivateKey key, Certificate[] chain)
    {
        return ReloadableKeyManager.of(new KeyStore.PrivateKeyEntry(key, chain));
    }

    public static ReloadableKeyManager of(KeyStore.PrivateKeyEntry key)
    {
        return ReloadableKeyManager.empty().updateCertificate(key);
    }
}
