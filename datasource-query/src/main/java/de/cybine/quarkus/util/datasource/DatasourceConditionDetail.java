package de.cybine.quarkus.util.datasource;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import de.cybine.quarkus.util.*;
import io.quarkus.arc.*;
import io.smallrye.common.constraint.*;
import jakarta.persistence.criteria.*;
import lombok.*;
import lombok.experimental.*;
import org.eclipse.microprofile.openapi.annotations.media.*;

import java.util.*;

@Data
@SuppressWarnings("unused")
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasourceConditionDetail<T>
{
    @Builder.Default
    private final UUID id = UUID.randomUUID();

    @With
    @NotNull
    private final String property;

    private final Type type;

    private final T value;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ParameterExpression<? extends T> parameter;

    public Optional<T> getValue( )
    {
        return Optional.ofNullable(this.value);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Predicate toPredicate(CriteriaBuilder criteriaBuilder, Path<?> parent)
    {
        Path<T> path = DatasourceFieldPath.resolvePath(parent, this.property);
        ParameterExpression<? extends T> param = this.toParameter(criteriaBuilder, path);

        return switch (this.type)
        {
            case IS_NULL -> criteriaBuilder.isNull(path);
            case IS_NOT_NULL -> criteriaBuilder.isNotNull(path);

            case IS_EQUAL -> criteriaBuilder.equal(path, param);
            case IS_NOT_EQUAL -> criteriaBuilder.notEqual(path, param);

            case IS_LIKE -> criteriaBuilder.like((Path<String>) path, (Expression<String>) param);
            case IS_NOT_LIKE -> criteriaBuilder.notLike((Path<String>) path, (Expression<String>) param);

            case IS_IN -> path.in(param);
            case IS_NOT_IN -> criteriaBuilder.not(path.in(param));

            case IS_PRESENT -> param.in(path);
            case IS_NOT_PRESENT -> criteriaBuilder.not(param.in(path));

            case IS_GREATER -> criteriaBuilder.greaterThan((Path) path, (Expression) param);
            case IS_GREATER_OR_EQUAL -> criteriaBuilder.greaterThanOrEqualTo((Path) path, (Expression) param);
            case IS_LESS -> criteriaBuilder.lessThan((Path) path, (Expression) param);
            case IS_LESS_OR_EQUAL -> criteriaBuilder.lessThanOrEqualTo((Path) path, (Expression) param);
        };
    }

    public Optional<BiTuple<String, Object>> toParameterValue( )
    {
        if (!this.type.requiresData())
            return Optional.empty();

        return Optional.of(new BiTuple<>(this.getName(), this.value));
    }

    public String getName( )
    {
        return this.property + "-" + this.getId().toString();
    }

    @SuppressWarnings("unchecked")
    private ParameterExpression<? extends T> toParameter(CriteriaBuilder criteriaBuilder, Path<T> property)
    {
        if (this.parameter == null)
        {
            TypeFactory typeFactory = Arc.container().select(ObjectMapper.class).get().getTypeFactory();

            JavaType itemType = typeFactory.constructType(property.getJavaType());
            if (this.type.requiresIterable())
                itemType = typeFactory.constructCollectionType(List.class, itemType);

            this.parameter = (ParameterExpression<? extends T>) criteriaBuilder.parameter(itemType.getRawClass(),
                    this.getName());
        }

        return this.parameter;
    }

    @Getter
    @Accessors(fluent = true)
    @Schema(name = "ConditionType")
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Type
    {
        IS_NULL(false, false),
        IS_NOT_NULL(false, false),
        IS_EQUAL(true, false),
        IS_NOT_EQUAL(true, false),
        IS_LIKE(true, false),
        IS_NOT_LIKE(true, false),
        IS_IN(true, true),
        IS_NOT_IN(true, true),
        IS_PRESENT(true, false),
        IS_NOT_PRESENT(true, false),
        IS_GREATER(true, false),
        IS_GREATER_OR_EQUAL(true, false),
        IS_LESS(true, false),
        IS_LESS_OR_EQUAL(true, false);

        private final boolean requiresData;
        private final boolean requiresIterable;
    }
}
