package de.cybine.quarkus.config;

import de.cybine.quarkus.util.api.permission.*;
import io.quarkus.runtime.annotations.*;
import io.smallrye.config.*;

import java.time.*;
import java.util.*;

import static de.cybine.quarkus.util.FilePathHelper.*;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.cybine.api.query")
public interface ApiQueryConfig
{
    /**
     * Defines if it is allowed to write api-queries that result in nested joins in the sql output
     */
    @WithDefault("false")
    @WithName("allow-multi-level-relations")
    boolean allowMultiLevelRelations( );

    /**
     * Defines if failures during response property filtering will be ignored or stop the response
     */
    @WithDefault("false")
    @WithName("property-filter.ignore-failures")
    boolean ignorePropertyFilterFailures( );

    /**
     * Defines the scope of responses to which the property filter will be applied
     */
    @WithDefault("API_FIELDS")
    @WithName("property-filter.scope")
    PropertyFilterScope propertyFilterScope( );

    /**
     * Defines paths to additional configuration files
     */
    @WithName("paths")
    FilePaths paths( );

    /**
     * Defines properties for the api-secret management
     */
    @WithName("secret")
    SecretProvider secretProvider();

    interface FilePaths
    {
        /**
         * Defines the path to the RBAC (role-based access control) configuration file
         */
        @WithName("rbac-path")
        @WithDefault(RESOURCES_PLACEHOLDER + "/rbac.json")
        String rbacPath( );

        /**
         * Defines the path to the api-permission file
         */
        @WithName("api-permissions-path")
        @WithDefault(RESOURCES_PLACEHOLDER + "/api-permissions.json")
        String apiPermissionsPath( );
    }

    interface SecretProvider
    {
        /**
         * Sets if a security-identity is required to create and consume api-secrets
         */
        @WithDefault("false")
        @WithName("allow-empty-subject")
        boolean allowEmptySubject( );

        /**
         * Sets how long a token is valid from the moment of creation onwards
         */
        @WithDefault("5m")
        @WithName("token-validity")
        Duration tokenValidity( );

        /**
         * Sets the cipher that is used to encrypt api-secrets
         */
        @WithName("symmetric.cipher")
        @WithDefault("AES/CBC/PKCS5Padding")
        String symmetricCipher( );

        /**
         * Sets the passphrase that the symmetric key is derived from
         */
        @WithName("symmetric-key")
        Optional<String> symmetricKey( );

        /**
         * Sets the path to the private ed25519 key that is used to temper-proof api-secrets
         */
        @WithName("asymmetric.private-key-path")
        Optional<String> privateKeyPath( );

        /**
         * Sets the path to the public ed25519 key that is used to temper-proof api-secrets
         */
        @WithName("asymmetric.public-key-path")
        Optional<String> publicKeyPath( );
    }
}
