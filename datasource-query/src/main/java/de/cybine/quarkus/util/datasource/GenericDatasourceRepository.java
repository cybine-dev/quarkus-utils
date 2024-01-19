package de.cybine.quarkus.util.datasource;

import lombok.*;

import java.util.*;

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

    public <O> List<O> fetchOptions(DatasourceQuery query)
    {
        return DatasourceQueryInterpreter.of(this.type, query).<O>prepareOptionQuery().getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<List<Object>> fetchMultiOptions(DatasourceQuery query)
    {
        return DatasourceQueryInterpreter.of(this.type, query)
                                         .prepareMultiOptionQuery()
                                         .getResultStream()
                                         .map(item -> (List<Object>) item)
                                         .toList();
    }

    public List<DatasourceCountInfo> fetchTotal(DatasourceQuery query)
    {
        return DatasourceQueryInterpreter.of(this.type, query).executeCountQuery();
    }
}
