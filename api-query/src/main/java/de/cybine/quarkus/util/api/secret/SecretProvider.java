package de.cybine.quarkus.util.api.secret;

import com.fasterxml.jackson.databind.*;
import de.cybine.quarkus.config.*;
import de.cybine.quarkus.exception.api.*;
import de.cybine.quarkus.util.*;
import io.quarkus.security.identity.*;
import io.smallrye.jwt.auth.principal.*;
import io.smallrye.jwt.build.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.inject.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.eclipse.microprofile.jwt.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.time.*;
import java.util.*;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SecretProvider
{
    private static final String IDENTITY = "quarkus-utils:api-query";
    private static final String CLAIM    = "quarkus-utils.secret";

    public static final String API_SECRET_HEADER = "X-Qu-Api-Secret";

    private final ApiQueryConfig config;

    private final JWTParser    parser;
    private final ObjectMapper mapper;

    private final Instance<SecurityIdentity> securityIdentityRef;

    public String createSecret(SecretData data)
    {
        JwtClaimsBuilder builder = Jwt.issuer(IDENTITY)
                                      .audience(IDENTITY)
                                      .issuedAt(Instant.now())
                                      .expiresIn(this.config.secretProvider().tokenValidity())
                                      .claim(CLAIM, this.encryptValue(data));

        if (!this.securityIdentityRef.isResolvable())
        {
            log.trace("No security identity found");
            if (!this.config.secretProvider().allowEmptySubject())
                throw new SecretProviderException("Cannot create secret without user identity");
        }

        if (this.securityIdentityRef.isResolvable())
        {
            SecurityIdentity identity = this.securityIdentityRef.get();
            if (identity.isAnonymous())
            {
                log.trace("Security identity is anonymous");
                if (!this.config.secretProvider().allowEmptySubject())
                    throw new SecretProviderException("Cannot create secret without user identity");
            }

            log.trace("Security identity has principal: {}", identity.getPrincipal().getName());
            builder.subject(identity.getPrincipal().getName());
        }

        return builder.sign(this.getSigningKey());
    }

    public SecretData readSecret(String secret) throws ParseException
    {
        JsonWebToken token = this.parser.verify(secret, this.getValidationKey());
        if (!token.getAudience().contains(IDENTITY))
            throw new SecretProviderException("Cannot read secret: Invalid audience");

        if (!token.containsClaim(CLAIM))
            throw new SecretProviderException("Cannot read secret: Invalid claim");

        String subject = token.getSubject();
        if (subject == null && !this.config.secretProvider().allowEmptySubject())
            throw new SecretProviderException("Cannot read secret: Empty subject is not allowed");

        if (subject != null)
        {
            if (!this.securityIdentityRef.isResolvable())
            {
                log.trace("No security identity found");
                throw new SecretProviderException("Cannot read secret: Invalid user identity");
            }

            SecurityIdentity identity = this.securityIdentityRef.get();
            Principal principal = identity.getPrincipal();
            boolean isAnonymous = identity.isAnonymous() || principal == null;
            if (isAnonymous)
            {
                log.trace("Security identity is anonymous");
                if (!this.config.secretProvider().allowEmptySubject())
                    throw new SecretProviderException("Cannot read secret: Invalid user identity");
            }

            if (!isAnonymous && !principal.getName().equals(subject))
            {
                log.trace("Security identity does not match token identity");
                throw new SecretProviderException("Cannot read secret: Invalid user identity");
            }
        }

        log.trace("Decrypting secret");
        return this.decryptValue(token.getClaim(CLAIM));
    }

    private String encryptValue(SecretData data)
    {
        try
        {
            byte[] value = this.mapper.writeValueAsBytes(data);
            byte[] iv = this.generateSalt();
            byte[] salt = this.generateSalt();

            Cipher cipher = Cipher.getInstance(this.config.secretProvider().symmetricCipher());
            cipher.init(Cipher.ENCRYPT_MODE, this.getEncryptionKey(salt), new IvParameterSpec(iv));
            byte[] ciphertext = cipher.doFinal(value);

            byte[] result = new byte[ ciphertext.length + 32 ];
            System.arraycopy(iv, 0, result, 0, 16);
            System.arraycopy(salt, 0, result, 16, 16);
            System.arraycopy(ciphertext, 0, result, 32, ciphertext.length);

            return Base64.getEncoder().encodeToString(result);
        }
        catch (NoSuchPaddingException | InvalidKeyException | BadPaddingException | IOException |
                NoSuchAlgorithmException | IllegalBlockSizeException | InvalidAlgorithmParameterException |
                InvalidKeySpecException exception)
        {
            throw new SecretProviderException("Could not encrypt value", exception);
        }
    }

    public boolean canCreateSecret( )
    {
        // @formatter:off
        return this.config.secretProvider().privateKeyPath().isPresent() &&
                this.config.secretProvider().symmetricKey().isPresent();
        // @formatter:on
    }

    public boolean canReadSecret( )
    {
        // @formatter:off
        return this.config.secretProvider().publicKeyPath().isPresent() &&
                this.config.secretProvider().symmetricKey().isPresent();
        // @formatter:on
    }

    private SecretData decryptValue(String value)
    {
        try
        {
            byte[] encryptedValue = Base64.getDecoder().decode(value);
            byte[] iv = new byte[ 16 ];
            byte[] salt = new byte[ 16 ];
            byte[] ciphertext = new byte[ encryptedValue.length - 32 ];
            System.arraycopy(encryptedValue, 0, iv, 0, 16);
            System.arraycopy(encryptedValue, 16, salt, 0, 16);
            System.arraycopy(encryptedValue, 32, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(this.config.secretProvider().symmetricCipher());
            cipher.init(Cipher.DECRYPT_MODE, this.getEncryptionKey(salt), new IvParameterSpec(iv));

            byte[] data = cipher.doFinal(ciphertext);
            return this.mapper.readValue(data, SecretData.class);
        }
        catch (IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                BadPaddingException | IOException | InvalidAlgorithmParameterException |
                InvalidKeySpecException exception)
        {
            throw new SecretProviderException("Could not decrypt value", exception);
        }
    }

    private PrivateKey getSigningKey( )
    {
        try
        {
            Path path = FilePathHelper.resolvePath(this.config.secretProvider().privateKeyPath().orElseThrow())
                                      .orElseThrow();

            String keyString = Files.readString(path).replaceAll("-----\\w+ PRIVATE KEY-----", "").strip();
            byte[] key = Base64.getDecoder().decode(keyString);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");

            return keyFactory.generatePrivate(keySpec);
        }
        catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | URISyntaxException exception)
        {
            throw new SecretProviderException("Could not create private key", exception);
        }
    }

    private PublicKey getValidationKey( )
    {
        try
        {
            Path path = FilePathHelper.resolvePath(this.config.secretProvider().publicKeyPath().orElseThrow())
                                      .orElseThrow();

            String keyString = Files.readString(path).replaceAll("-----\\w+ PUBLIC KEY-----", "").strip();
            byte[] key = Base64.getDecoder().decode(keyString);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");

            return keyFactory.generatePublic(keySpec);
        }
        catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | URISyntaxException exception)
        {
            throw new SecretProviderException("Could not create private key", exception);
        }
    }

    private SecretKey getEncryptionKey(byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        char[] password = this.config.secretProvider().symmetricKey().orElseThrow().toCharArray();
        KeySpec spec = new PBEKeySpec(password, salt, 256 * 256, 256);

        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private byte[] generateSalt( )
    {
        byte[] salt = new byte[ 16 ];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
}
