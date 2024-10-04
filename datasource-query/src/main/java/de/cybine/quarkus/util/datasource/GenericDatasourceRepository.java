package de.cybine.quarkus.util.datasource;

import lombok.*;

import java.util.*;

import static de.cybine.quarkus.util.datasource.DatasourceQueryInterpreter.*;

@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "forType")
public class GenericDatasourceRepository<T>
{
    private final Class<T> type;

    public List<T> fetch(DatasourceQuery query)
    {
        return DatasourceQueryInterpreter.of(this.type, query).prepareDataQuery().getResultList();
    }

    public Optional<T> fetchSingle(DatasourceQuery query)
    {
        return DatasourceQueryInterpreter.of(this.type, query).prepareDataQuery().getResultStream().findAny();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchOptions(DatasourceQuery query)
    {
        return DatasourceQueryInterpreter.of(this.type, query)
                                         .prepareOptionQuery()
                                         .getResultStream()
                                         .map(item -> interconnectOptions(query.getFields(), item))
                                         .map(item -> (Map<String, Object>) item)
                                         .toList();
    }

    public List<DatasourceCountInfo> fetchTotal(DatasourceQuery query)
    {
        return DatasourceQueryInterpreter.of(this.type, query).executeCountQuery();
    }
}
