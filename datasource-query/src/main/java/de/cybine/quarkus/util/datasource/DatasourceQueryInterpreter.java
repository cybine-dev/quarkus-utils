package de.cybine.quarkus.util.datasource;

import de.cybine.quarkus.exception.datasource.*;
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

import static de.cybine.quarkus.util.datasource.DatasourceFieldPath.*;

@Slf4j
@SuppressWarnings("unused")
@AllArgsConstructor(staticName = "of")
public class DatasourceQueryInterpreter<T>
{
    private final Class<T>        type;
    private final DatasourceQuery datasourceQuery;

    private final EntityManager entityManager;

    @SuppressWarnings("rawtypes")
    public TypedQuery<List> prepareOptionQuery( )
    {
        return this.prepareOptionQuery(this.datasourceQuery.getFields());
    }

    @SuppressWarnings("rawtypes")
    private TypedQuery<List> prepareOptionQuery(List<String> fieldNames)
    {
        if (fieldNames == null || fieldNames.isEmpty())
            throw new InvalidQueryException("At least one field must be queried");

        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<List> query = criteriaBuilder.createQuery(List.class);
        Root<T> root = query.from(this.type);

        query.multiselect(fieldNames.stream().map(item -> resolvePath(root, item)).toArray(Selection[]::new))
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
                pagination.setTotal(
                        this.executeResultCountQuery(parameters, new HashSet<>(this.datasourceQuery.getFields())));
        }

        return typedQuery;
    }

    public List<DatasourceCountInfo> executeCountQuery( )
    {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);
        Root<T> root = query.from(this.type);

        List<String> groupingNames = this.datasourceQuery.getFields();
        List<Path<?>> grouping = this.resolvePaths(root, groupingNames);
        List<Selection<?>> selection = new ArrayList<>();
        selection.add(criteriaBuilder.count(root));
        selection.addAll(grouping);

        query.multiselect(selection)
             .where(this.datasourceQuery.getConditions(criteriaBuilder, root).toArray(Predicate[]::new))
             .groupBy(new ArrayList<>(grouping))
             .orderBy(this.datasourceQuery.getSortedOrderings(criteriaBuilder, root));

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
                                                         .groupKey(grouping.isEmpty() ? Collections.emptyMap() :
                                                                 interconnectOptions(groupingNames,
                                                                         Arrays.asList(item).subList(1, item.length)))
                                                         .build())
                         .toList();
    }

    public TypedQuery<T> prepareDataQuery( )
    {
        if (this.datasourceQuery.getRelations().stream().noneMatch(DatasourceRelationInfo::isFetch))
            return this.prepareRegularDataQuery();

        List<Field> idFields = this.getIdFields();
        if (idFields.isEmpty())
            return this.prepareRegularDataQuery();

        return this.prepareIdDataQuery(idFields);
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
        List<Object> ids = this.prepareOptionQuery(List.of(idField.getName()))
                               .getResultStream()
                               .map(item -> item.get(0))
                               .toList();

        TypedQuery<T> typedQuery = this.entityManager.createQuery(query)
                                                     .setParameter(idParameter, ids)
                                                     .setHint(SpecHints.HINT_SPEC_FETCH_GRAPH, graph)
                                                     .setHint(HibernateHints.HINT_READ_ONLY, true);

        List<BiTuple<String, Object>> parameters = this.datasourceQuery.getParameters();
        parameters.forEach(parameter -> typedQuery.setParameter(parameter.first(), parameter.second()));

        return typedQuery;
    }

    @SuppressWarnings("unchecked")
    private TypedQuery<T> prepareIdDataQuery(List<Field> idFields)
    {
        if (idFields.size() == 1)
            return this.prepareIdDataQuery(idFields.get(0));

        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(this.type);
        Root<T> root = query.from(this.type);

        List<String> idFieldNames = idFields.stream().map(Field::getName).toList();
        List<Map<String, Object>> ids = this.prepareOptionQuery(idFieldNames)
                                            .getResultStream()
                                            .map(item -> interconnectOptions(idFieldNames, item))
                                            .map(item -> (Map<String, Object>) item)
                                            .toList();

        List<Predicate> idConditions = new ArrayList<>();
        List<BiTuple<String, Object>> parameters = this.datasourceQuery.getParameters();
        for (Map<String, Object> item : ids)
        {
            String rowId = UUID.randomUUID().toString();
            List<Predicate> rowConditions = new ArrayList<>();
            for (Field field : idFields)
            {
                String parameterName = field.getName() + "--" + rowId;
                ParameterExpression<?> parameter = criteriaBuilder.parameter(field.getType(), parameterName);
                rowConditions.add(criteriaBuilder.equal(root.get(field.getName()), parameter));

                parameters.add(new BiTuple<>(parameterName, item.get(field.getName())));
            }

            idConditions.add(criteriaBuilder.and(rowConditions.toArray(Predicate[]::new)));
        }

        List<Predicate> conditions = this.datasourceQuery.getConditions(criteriaBuilder, root);
        conditions.add(criteriaBuilder.or(idConditions.toArray(Predicate[]::new)));

        query.select(root)
             .where(conditions.toArray(Predicate[]::new))
             .orderBy(this.datasourceQuery.getSortedOrderings(criteriaBuilder, root));

        EntityGraph<T> graph = this.getRelationGraph();
        TypedQuery<T> typedQuery = this.entityManager.createQuery(query)
                                                     .setHint(SpecHints.HINT_SPEC_FETCH_GRAPH, graph)
                                                     .setHint(HibernateHints.HINT_READ_ONLY, true);

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
        return this.executeResultCountQuery(parameters, Collections.emptySet());
    }

    private Long executeResultCountQuery(List<BiTuple<String, Object>> parameters, Set<String> properties)
    {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<T> root = query.from(this.type);

        query.select(criteriaBuilder.countDistinct(root))
             .where(this.datasourceQuery.getConditions(criteriaBuilder, root).toArray(Predicate[]::new));

        List<String> idFieldNames = this.getIdFields().stream().map(Field::getName).toList();
        boolean propertiesAreDifferentFromIdFields =
                !properties.isEmpty() && (idFieldNames.isEmpty() || !properties.containsAll(
                idFieldNames));

        if (propertiesAreDifferentFromIdFields)
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

    private List<Field> getIdFields( )
    {
        return Arrays.stream(this.type.getDeclaredFields()).filter(item -> item.isAnnotationPresent(Id.class)).toList();
    }

    @SuppressWarnings("java:S6204")
    private List<Path<?>> resolvePaths(Root<?> root, List<String> fieldNames)
    {
        return fieldNames.stream().map(item -> resolvePath(root, item)).collect(Collectors.toList());
    }

    public static Map<String, Object> interconnectOptions(List<String> fieldNames, List<Object> row)
    {
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < fieldNames.size(); i++)
            result.put(fieldNames.get(i), row.get(i));

        return result;
    }

    public static <T> DatasourceQueryInterpreter<T> of(Class<T> type, DatasourceQuery query)
    {
        return DatasourceQueryInterpreter.of(type, query, Arc.container().select(EntityManager.class).get());
    }
}
