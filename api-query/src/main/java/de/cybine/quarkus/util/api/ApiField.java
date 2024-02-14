package de.cybine.quarkus.util.api;

import de.cybine.quarkus.util.datasource.*;
import lombok.*;

import java.lang.reflect.*;
import java.util.*;

@Data
@AllArgsConstructor
public class ApiField
{
    private final String name;

    private final Type objectType;
    private final Type fieldType;

    private final DatasourceField datasourceField;

    public Optional<Type> getFieldType( )
    {
        return Optional.ofNullable(this.fieldType);
    }
}
