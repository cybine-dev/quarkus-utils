package de.cybine.quarkus.util.api.query;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.*;
import lombok.*;
import lombok.extern.jackson.*;
import org.eclipse.microprofile.openapi.annotations.media.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiConditionInfo
{
    @Builder.Default
    @JsonProperty("method")
    private final EvaluationMethod type = EvaluationMethod.AND;

    @Builder.Default
    @JsonProperty("inverted")
    private final boolean isInverted = false;

    @Valid
    @Singular
    @JsonProperty("details")
    private final List<ApiConditionDetail> details;

    @Valid
    @Singular
    @JsonProperty("sub_conditions")
    private final List<ApiConditionInfo> subConditions;

    @Schema(name = "ConditionEvaluationMethod")
    public enum EvaluationMethod
    {
        AND, OR
    }
}
