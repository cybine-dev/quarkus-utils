package de.cybine.quarkus.util.action.data;

import lombok.*;

import java.util.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionProcessorMetadata
{
    public static final String ANY = "*";

    private final String namespace;
    private final String category;
    private final String name;

    private final String action;
    private final String from;

    public Optional<String> getFrom( )
    {
        return Optional.ofNullable(this.from);
    }

    public boolean isStateless( )
    {
        return this.from == null;
    }

    public boolean isStateful( )
    {
        return !this.isStateless();
    }

    public boolean isApplicable(String shortForm)
    {
        String[] args = shortForm.split(":");
        if (args.length < 4)
            throw new IllegalArgumentException("An action must have at least 4 parts separated by a colon");

        return this.isApplicable(args[ 0 ], args[ 1 ], args[ 2 ], args[ 3 ], args.length >= 5 ? args[ 4 ] : null);
    }

    public boolean isApplicable(String namespace, String category, String name, String action)
    {
        return this.isApplicable(namespace, category, name, action, null);
    }

    public boolean isApplicable(String namespace, String category, String name, String action, String state)
    {
        if (!this.namespace.equals(namespace))
            return false;

        if (!this.category.equals(category))
            return false;

        if (!this.name.equals(name))
            return false;

        if (!this.action.equals(action))
            return false;

        return this.isStateless() || this.from.equals(ANY) || this.from.equals(state);
    }

    public String toShortForm( )
    {
        return String.format("%s:%s:%s:%s", this.namespace, this.category, this.name, this.action);
    }
}
