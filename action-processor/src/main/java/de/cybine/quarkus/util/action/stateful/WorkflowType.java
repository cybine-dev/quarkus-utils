package de.cybine.quarkus.util.action.stateful;

import de.cybine.quarkus.util.*;
import lombok.*;

import java.util.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum WorkflowType implements WithDatasourceKey<String>
{
    UPDATE("update", true), ACTION("action", false), LOG("log", false);

    private final String name;

    private final boolean isExclusive;

    @Override
    public String getDatasourceKey( )
    {
        return this.getName();
    }

    public static Optional<WorkflowType> findByName(String name)
    {
        if (name == null)
            return Optional.empty();

        return Arrays.stream(WorkflowType.values()).filter(item -> item.getName().equals(name)).findAny();
    }
}
