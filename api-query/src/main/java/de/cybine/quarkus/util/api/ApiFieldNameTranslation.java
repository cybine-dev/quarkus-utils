package de.cybine.quarkus.util.api;

import lombok.*;

import java.util.*;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class ApiFieldNameTranslation
{
    private final String fieldName;
    private final String translation;

    public boolean hasTranslation( )
    {
        return this.translation != null;
    }

    public Optional<String> getTranslation( )
    {
        return Optional.ofNullable(this.translation);
    }

    public String getTranslationOrDefault( )
    {
        return Optional.ofNullable(this.translation).orElse(this.fieldName);
    }

    public List<String> getFieldNameSegments()
    {
        return Arrays.asList(this.fieldName.split("\\."));
    }

    public List<String> getTranslationSegments()
    {
        return Arrays.asList(this.getTranslationOrDefault().split("\\."));
    }

    public static ApiFieldNameTranslation of(String fieldName)
    {
        return new ApiFieldNameTranslation(fieldName, null);
    }
}
