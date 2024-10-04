package de.cybine.quarkus.util.datasource;

import de.cybine.quarkus.util.*;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.*;

import java.util.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasourceQuery
{
    private final DatasourcePaginationInfo pagination;

    private final DatasourceConditionInfo condition;

    @Singular
    private final List<String> fields;

    @Singular("order")
    private final List<DatasourceOrderInfo> order;

    @Singular
    private final List<DatasourceRelationInfo> relations;

    public Optional<DatasourcePaginationInfo> getPagination( )
    {
        return Optional.ofNullable(this.pagination);
    }

    public Optional<DatasourceConditionInfo> getCondition( )
    {
        return Optional.ofNullable(this.condition);
    }

    public Optional<String> getFirstField( )
    {
        if (this.fields.isEmpty())
            return Optional.empty();

        return Optional.of(this.fields.get(0));
    }

    @SuppressWarnings("java:S1117")
    public List<String> getFields( )
    {
        List<String> fields = new ArrayList<>(this.fields);
        for (DatasourceRelationInfo relation : this.relations)
            fields.addAll(relation.getAllFields(null));

        for (DatasourceOrderInfo order : this.order)
        {
            if(fields.contains(order.getProperty()))
                continue;

            fields.add(order.getProperty());
        }

        return fields;
    }

    public List<Order> getSortedOrderings(CriteriaBuilder criteriaBuilder, Root<?> root)
    {
        List<BiTuple<Order, Integer>> orderings = new ArrayList<>();
        for (DatasourceOrderInfo ordering : this.order)
            orderings.add(ordering.toOrder(criteriaBuilder, root));

        for (DatasourceRelationInfo relation : this.relations)
            orderings.addAll(relation.getAllOrderings(criteriaBuilder, root));

        return orderings.stream().sorted(Comparator.comparing(BiTuple::second)).map(BiTuple::first).toList();
    }

    public List<Predicate> getConditions(CriteriaBuilder criteriaBuilder, Root<?> root)
    {
        List<Predicate> conditions = new ArrayList<>();
        this.getCondition().flatMap(item -> item.toPredicate(criteriaBuilder, root)).ifPresent(conditions::add);

        for (DatasourceRelationInfo relation : this.relations)
            conditions.addAll(relation.getConditions(criteriaBuilder, root));

        return conditions;
    }

    public List<BiTuple<String, Object>> getParameters( )
    {
        List<BiTuple<String, Object>> parameters = new ArrayList<>();
        this.getCondition().map(DatasourceConditionInfo::getParameterValues).ifPresent(parameters::addAll);
        for (DatasourceRelationInfo relation : this.relations)
            parameters.addAll(relation.getParameters());

        return parameters;
    }

    public void addRelations(EntityGraph<?> graph)
    {
        for (DatasourceRelationInfo relation : this.relations)
        {
            if (!relation.isFetch())
                continue;

            relation.addRelations(graph);
        }
    }
}
