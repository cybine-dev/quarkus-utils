package de.cybine.quarkus.util.api;

import de.cybine.quarkus.util.datasource.*;
import lombok.*;

import java.util.*;
import java.util.stream.*;

@Data
public class ApiFieldPath
{
    private final List<ApiField> path;

    @Builder(builderClassName = "Generator")
    public ApiFieldPath(@Singular("field") List<ApiField> path)
    {
        if (path.isEmpty())
            throw new IllegalArgumentException("Path cannot be emtpy");

        this.path = path;
    }

    public ApiField getLast( )
    {
        return this.path.get(this.getLength() - 1);
    }

    public int getLength( )
    {
        return this.path.size();
    }

    public String asString( )
    {
        return this.path.stream().map(ApiField::getName).collect(Collectors.joining("."));
    }

    public DatasourceFieldPath toDatasourceFieldPath( )
    {
        return this.toDatasourceFieldPath(1);
    }

    public DatasourceFieldPath toDatasourceFieldPath(int steps)
    {
        int start = this.path.size() - steps;
        if(start < 0)
            start = 0;

        List<ApiField> path = this.path.subList(start, this.path.size());
        DatasourceFieldPath.Generator builder = DatasourceFieldPath.builder();
        for (ApiField field : path)
            builder.field(field.getDatasourceField());

        return builder.build();
    }
}
