package de.cybine.quarkus.util.api.query;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import lombok.experimental.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiQueryPagination
{
    @JsonProperty("size")
    private Integer size;

    @JsonProperty("offset")
    private Integer offset;

    @Accessors(fluent = true)
    @JsonProperty("include_total")
    private boolean includeTotal;

    public Optional<Integer> getSize( )
    {
        return Optional.ofNullable(this.size);
    }

    public Optional<Integer> getOffset( )
    {
        return Optional.ofNullable(this.offset);
    }
}
