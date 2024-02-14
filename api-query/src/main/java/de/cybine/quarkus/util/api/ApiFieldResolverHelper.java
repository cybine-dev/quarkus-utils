package de.cybine.quarkus.util.api;

import de.cybine.quarkus.util.datasource.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.lang.reflect.*;
import java.util.function.*;

@Slf4j
@SuppressWarnings("unused")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ApiFieldResolverHelper
{
    private final ApiFieldResolver resolver;

    private final Type dataType;

    private final BiConsumer<Type, Type> registerTypeRepresentation;

    private final Consumer<ApiField> registerField;

    private Type entityType;

    public ApiFieldResolverHelper withField(String alias, DatasourceField field)
    {
        return this.withRelation(alias, field, null);
    }

    public ApiFieldResolverHelper withRelation(String alias, DatasourceField field, Type dataType)
    {
        if (this.entityType == null)
        {
            this.entityType = field.getObjectType();
            this.registerTypeRepresentation.accept(this.dataType, this.entityType);
        }

        if (this.entityType != field.getObjectType())
            throw new IllegalArgumentException("Cannot mix multiple entity types.");

        log.debug("Registering api-field: {}({})", field.getObjectType().getTypeName(), field.getName());
        this.registerField.accept(new ApiField(alias, this.dataType, dataType, field));
        return this;
    }
}
