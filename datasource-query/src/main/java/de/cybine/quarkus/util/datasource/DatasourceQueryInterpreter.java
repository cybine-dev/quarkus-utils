package de.cybine.quarkus.util.datasource;

import de.cybine.quarkus.util.*;
import io.quarkus.arc.*;
import jakarta.persistence.Parameter;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hibernate.jpa.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

@Slf4j
@SuppressWarnings("unused")
@AllArgsConstructor(staticName = "of")
public class DatasourceQueryInterpreter<T>
{
    private final Class<T>        type;
    private final DatasourceQuery datasourceQuery;

    private final EntityManager entityManager;

    public <O> TypedQuery<O> prepareOptionQuery( )
    {
        // @formatter:off
        if (this.datasourceQuery.getProperties().size() > 1)
            log.warn("Fetching options for datasource-query with more than one property defined: " +
                    "Only the first property will be used for this query.");
        // @formatter:on

        return this.prepareOptionQuery(this.datasourceQuery.getFirstProperty().orElseThrow());
    }

    @SuppressWarnings("unchecked")
    private <O> TypedQuery<O> prepareOptionQuery(String fieldName)
    {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = criteriaBuilder.createQuery();
        Root<T> root = query.from(this.type);

        query.select(root.get(fieldName))
             .distinct(true)
             .where(this.datasourceQuery.getConditions(criteriaBuilder, root).toArray(Predicate[]::new))
             .orderBy(this.datasourceQuery.getSortedOrderings(criteriaBuilder, root));

        TypedQuery<Object> typedQuery = this.entityManager.createQuery(query);

        List<BiTuple<String, Object>> parameters = this.datasourceQuery.getParameters();
        parameters.forEach(parameter -> typedQuery.setParameter(parameter.first(), parameter.second()));

        DatasourcePaginationInfo pagination = this.datasourceQuery.getPagination().orElse(null);
        if (pagination != null)
        {
            pagination.getSize().ifPresent(typedQuery::setMaxResults);
            pagination.getOffset().ifPresent(typedQuery::setFirstResult);

            if (pagination.includeTotal())
                pagination.setTotal(this.executeResultCountQuery(parameters, List.of(fieldName)));
        }

        return (TypedQuery<O>) typedQuery;
    }

    @SuppressWarnings("rawtypes")
    public TypedQuery<List> prepareMultiOptionQuery( )
    {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<List> query = criteriaBuilder.createQuery(List.class);
        Root<T> root = query.from(this.type);

        query.multiselect((Selection<?>) this.datasourceQuery.getProperties().stream().map(root::get).toList())
             .distinct(true)
             .where(this.datasourceQuery.getConditions(criteriaBuilder, root).toArray(Predicate[]::new))
             .orderBy(this.datasourceQuery.getSortedOrderings(criteriaBuilder, root));

        TypedQuery<List> typedQuery = this.entityManager.createQuery(query);

        List<BiTuple<String, Object>> parameters = this.datasourceQuery.getParameters();
        parameters.forEach(parameter -> typedQuery.setParameter(parameter.first(), parameter.second()));

        DatasourcePaginationInfo pagination = this.datasourceQuery.getPagination().orElse(null);
        if (pagination != null)
        {
            pagination.getSize().ifPresent(typedQuery::setMaxResults);
            pagination.getOffset().ifPresent(typedQuery::setFirstResult);

            if (pagination.includeTotal())
                pagination.setTotal(this.executeResultCountQuery(parameters, this.datasourceQuery.getProperties()));
        }

        return typedQuery;
    }

    public List<DatasourceCountInfo> executeCountQuery( )
    {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);
        Root<T> root = query.from(this.type);

        List<Path<?>> grouping = this.datasourceQuery.getGroupings(root);
        List<Selection<?>> selection = new ArrayList<>();
        selection.add(criteriaBuilder.count(root));
        selection.addAll(grouping);

        query.multiselect(selection)
             .where(this.datasourceQuery.getConditions(criteriaBuilder, root).toArray(Predicate[]::new))
             .groupBy(new ArrayList<>(grouping));

        TypedQuery<Object[]> typedQuery = this.entityManager.createQuery(query)
                                                            .setHint(SpecHints.HINT_SPEC_FETCH_GRAPH,
                                                                    this.getRelationGraph())
                                                            .setHint(HibernateHints.HINT_READ_ONLY, true);

        List<BiTuple<String, Object>> parameters = this.datasourceQuery.getParameters();
        parameters.forEach(parameter -> typedQuery.setParameter(parameter.first(), parameter.second()));

        return typedQuery.getResultList()
                         .stream()
                         .map(item -> DatasourceCountInfo.builder()
                                                         .count((long) item[ 0 ])
                                                         .groupKey(grouping.isEmpty() ? Collections.emptyList() :
                                                                 Arrays.asList(item).subList(1, item.length))
                                                         .build())
                         .toList();
    }

    public TypedQuery<T> prepareDataQuery( )
    {
        if (this.datasourceQuery.getRelations().stream().noneMatch(DatasourceRelationInfo::isFetch))
            return this.prepareRegularDataQuery();

        Field idField = this.findIdField().orElse(null);
        if (idField == null)
            return this.prepareRegularDataQuery();

        return this.prepareIdDataQuery(idField);
    }

    @SuppressWarnings("rawtypes")
    private TypedQuery<T> prepareIdDataQuery(Field idField)
    {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(this.type);
        Root<T> root = query.from(this.type);

        Parameter<List> idParameter = criteriaBuilder.parameter(List.class);
        List<Predicate> conditions = this.datasourceQuery.getConditions(criteriaBuilder, root);
        conditions.add(root.get(idField.getName()).in(idParameter));

        query.select(root)
             .where(conditions.toArray(Predicate[]::new))
             .orderBy(this.datasourceQuery.getSortedOrderings(criteriaBuilder, root));

        EntityGraph<T> graph = this.getRelationGraph();
        List<Object> ids = this.prepareOptionQuery(idField.getName()).getResultList();
        TypedQuery<T> typedQuery = this.entityManager.createQuery(query)
                                                     .setParameter(idParameter, ids)
                                                     .setHint(SpecHints.HINT_SPEC_FETCH_GRAPH, graph)
                                                     .setHint(HibernateHints.HINT_READ_ONLY, true);

        List<BiTuple<String, Object>> parameters = this.datasourceQuery.getParameters();
        parameters.forEach(parameter -> typedQuery.setParameter(parameter.first(), parameter.second()));

        return typedQuery;
    }

    private TypedQuery<T> prepareRegularDataQuery( )
    {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(this.type);
        Root<T> root = query.from(this.type);

        query.select(root)
             .where(this.datasourceQuery.getConditions(criteriaBuilder, root).toArray(Predicate[]::new))
             .orderBy(this.datasourceQuery.getSortedOrderings(criteriaBuilder, root));

        EntityGraph<T> graph = this.getRelationGraph();
        TypedQuery<T> typedQuery = this.entityManager.createQuery(query)
                                                     .setHint(SpecHints.HINT_SPEC_FETCH_GRAPH, graph)
                                                     .setHint(HibernateHints.HINT_READ_ONLY, true);

        List<BiTuple<String, Object>> parameters = this.datasourceQuery.getParameters();
        parameters.forEach(parameter -> typedQuery.setParameter(parameter.first(), parameter.second()));

        DatasourcePaginationInfo pagination = this.datasourceQuery.getPagination().orElse(null);
        if (pagination != null)
        {
            pagination.getSize().ifPresent(typedQuery::setMaxResults);
            pagination.getOffset().ifPresent(typedQuery::setFirstResult);

            if (pagination.includeTotal())
                pagination.setTotal(this.executeResultCountQuery(parameters));
        }

        return typedQuery;
    }

    private Long executeResultCountQuery(List<BiTuple<String, Object>> parameters)
    {
        return this.executeResultCountQuery(parameters, Collections.emptyList());
    }

    private Long executeResultCountQuery(List<BiTuple<String, Object>> parameters, List<String> properties)
    {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<T> root = query.from(this.type);

        query.select(criteriaBuilder.countDistinct(root))
             .where(this.datasourceQuery.getConditions(criteriaBuilder, root).toArray(Predicate[]::new));

        String idFieldName = this.findIdField().map(Field::getName).orElse(null);
        if (!properties.isEmpty() && (idFieldName == null || !properties.contains(idFieldName)))
            query.groupBy(properties.stream().map(root::get).collect(Collectors.toList()));

        TypedQuery<Long> typedQuery = this.entityManager.createQuery(query);
        parameters.forEach(parameter -> typedQuery.setParameter(parameter.first(), parameter.second()));

        return typedQuery.getSingleResult();
    }

    private EntityGraph<T> getRelationGraph( )
    {
        EntityGraph<T> graph = this.entityManager.createEntityGraph(this.type);
        this.datasourceQuery.addRelations(graph);

        return graph;
    }

    private Optional<Field> findIdField( )
    {
        List<Field> idFields = Arrays.stream(this.type.getDeclaredFields())
                                     .filter(item -> item.isAnnotationPresent(Id.class))
                                     .toList();

        if (idFields.size() != 1)
            return Optional.empty();

        return Optional.of(idFields.get(0));
    }

    public static <T> DatasourceQueryInterpreter<T> of(Class<T> type, DatasourceQuery query)
    {
        return DatasourceQueryInterpreter.of(type, query, Arc.container().select(EntityManager.class).get());
    }
}
