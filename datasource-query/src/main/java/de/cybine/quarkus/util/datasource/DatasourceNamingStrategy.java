package de.cybine.quarkus.util.datasource;

import lombok.*;
import lombok.extern.slf4j.*;
import org.eclipse.microprofile.config.*;
import org.hibernate.boot.model.naming.*;
import org.hibernate.engine.jdbc.env.spi.*;

@Slf4j
@SuppressWarnings("unused")
public class DatasourceNamingStrategy implements PhysicalNamingStrategy
{
    @Override
    public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment)
    {
        return this.evaluateIdentifier(identifier, Type.CATALOG);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment)
    {
        return this.evaluateIdentifier(identifier, Type.SCHEMA);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment)
    {
        return this.evaluateIdentifier(identifier, Type.TABLE);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment)
    {
        return this.evaluateIdentifier(identifier, Type.SEQUENCE);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment)
    {
        return this.evaluateIdentifier(identifier, Type.COLUMN);
    }

    private Identifier evaluateIdentifier(Identifier identifier, Type type)
    {
        if (identifier == null)
            return null;

        String text = identifier.getText();
        if (text == null || !text.startsWith("$dq."))
            return identifier;

        log.trace("Searching for {} name replacement for token '{}'...", type.getDisplay().toLowerCase(), text);
        String key = text.replaceFirst("\\$dq\\.", "");
        String configBase = String.format("quarkus.cybine.datasource.query.names.%s", type.name().toLowerCase());
        String quotedKey = String.format("%s.\"%s\"", configBase, key);
        String unquotedKey = String.format("%s.%s", configBase, key);

        Config config = ConfigProvider.getConfig();
        String replacement = config.getOptionalValue(unquotedKey, String.class)
                                   .or(() -> config.getOptionalValue(quotedKey, String.class))
                                   .orElse(null);

        if (replacement == null)
        {
            log.warn("{} name replacement for token '{}' is unknown", type.getDisplay(), key);
            return Identifier.toIdentifier(key, identifier.isQuoted());
        }

        log.trace("Found {} name replacement for token '{}': {}", type.getDisplay().toLowerCase(), text, replacement);
        return Identifier.toIdentifier(replacement, identifier.isQuoted());
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type
    {
        CATALOG("Catalog"), SCHEMA("Schema"), TABLE("Table"), SEQUENCE("Sequence"), COLUMN("Column");

        private final String display;
    }
}
