package de.cybine.quarkus.util.datasource;

import de.cybine.quarkus.exception.datasource.*;
import de.cybine.quarkus.util.*;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.persistence.criteria.Order;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.query.*;

import java.util.*;

@Data
@SuppressWarnings("unused")
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasourceRelationInfo
{
    @NotNull
    private final String property;

    private final boolean fetch;

    private final DatasourceConditionInfo condition;

    @Singular("field")
    private final List<String> fields;

    @Singular("order")
    private final List<DatasourceOrderInfo> order;

    @Singular
    private final List<DatasourceRelationInfo> relations;

    public Optional<DatasourceConditionInfo> getCondition( )
    {
        return Optional.ofNullable(this.condition);
    }

    @SuppressWarnings("java:S1117")
    public List<String> getAllFields(String parent)
    {
        if(!this.isFetch())
            return Collections.emptyList();

        List<String> fields = new ArrayList<>();
        for (String field : this.fields)
            fields.add(parent == null ? field : parent + "." + field);

        for (DatasourceOrderInfo order : this.order)
        {
            String fieldName = parent == null ? order.getProperty() : parent + "." + order.getProperty();
            if(fields.contains(fieldName))
                continue;

            fields.add(fieldName);
        }

        for (DatasourceRelationInfo relation : this.relations)
            fields.addAll(relation.getAllFields(parent + "." + relation.getProperty()));

        return fields;
    }

    public List<BiTuple<Order, Integer>> getAllOrderings(CriteriaBuilder criteriaBuilder, Path<?> parent)
    {
        if(!this.isFetch())
            return Collections.emptyList();

        Path<?> path = parent.get(this.property);
        List<BiTuple<Order, Integer>> orderings = new ArrayList<>();
        for (DatasourceOrderInfo ordering : this.order)
            orderings.add(ordering.toOrder(criteriaBuilder, path));

        for (DatasourceRelationInfo relation : this.relations)
            orderings.addAll(relation.getAllOrderings(criteriaBuilder, path));

        return orderings;
    }

    public List<Predicate> getConditions(CriteriaBuilder criteriaBuilder, Path<?> parent)
    {
        try
        {
            Path<?> path = parent.get(this.property);
            List<Predicate> conditions = new ArrayList<>();
            this.getCondition().flatMap(item -> item.toPredicate(criteriaBuilder, path)).ifPresent(conditions::add);

            for (DatasourceRelationInfo relation : this.relations)
                conditions.addAll(relation.getConditions(criteriaBuilder, path));

            return conditions;
        }
        catch (IllegalArgumentException exception)
        {
            if (exception.getCause() instanceof SemanticException semanticException)
            {
                throw new UnknownRelationException(semanticException.getMessage(), semanticException).addData("name",
                        this.property);
            }

            throw exception;
        }
    }

    public List<BiTuple<String, Object>> getParameters( )
    {
        List<BiTuple<String, Object>> parameters = new ArrayList<>();
        this.getCondition().map(DatasourceConditionInfo::getParameterValues).ifPresent(parameters::addAll);
        for (DatasourceRelationInfo relation : this.relations)
            parameters.addAll(relation.getParameters());

        return parameters;
    }

    public void addRelations(EntityGraph<?> parent)
    {
        Subgraph<Object> graph = parent.addSubgraph(this.property);
        for (DatasourceRelationInfo relation : this.relations)
        {
            if (!relation.isFetch())
                continue;

            relation.addRelations(graph);
        }
    }

    public void addRelations(Subgraph<?> parent)
    {
        Subgraph<Object> graph = parent.addSubgraph(this.property);
        for (DatasourceRelationInfo relation : this.relations)
        {
            if (!relation.isFetch())
                continue;

            relation.addRelations(graph);
        }
    }
}
