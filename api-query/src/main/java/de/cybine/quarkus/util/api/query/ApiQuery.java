package de.cybine.quarkus.util.api.query;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiQuery
{
    public static final String SEARCH_CAPABILITY = "query:search";
    public static final String GROUP_CAPABILITY = "query:group_by";
    public static final String ORDER_CAPABILITY = "query:order";

    @Valid
    @JsonProperty("pagination")
    private final ApiPaginationInfo pagination;

    @Valid
    @JsonProperty("condition")
    private final ApiConditionInfo condition;

    @Valid
    @Singular("order")
    @JsonProperty("order")
    private final List<ApiOrderInfo> order;

    @Valid
    @Singular
    @JsonProperty("relations")
    private final List<ApiRelationInfo> relations;

    public Optional<ApiPaginationInfo> getPagination( )
    {
        return Optional.ofNullable(this.pagination);
    }

    public Optional<ApiConditionInfo> getCondition( )
    {
        return Optional.ofNullable(this.condition);
    }
}
