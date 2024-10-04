package de.cybine.quarkus.util.api;

import de.cybine.quarkus.util.api.secret.*;
import io.quarkus.arc.*;
import jakarta.enterprise.context.*;
import lombok.*;

import java.util.*;
import java.util.concurrent.atomic.*;

@Unremovable
@RequestScoped
public class ApiQueryContext
{
    @Getter
    private final ApiPaginationInfo paginationInfo = new ApiPaginationInfo();

    private final AtomicReference<String>     rawSecretData = new AtomicReference<>();
    private final AtomicReference<SecretData> secretDataRef = new AtomicReference<>();

    private final List<ApiFieldNameTranslation> translations = new ArrayList<>();

    public Optional<String> getRawSecretData( )
    {
        return Optional.ofNullable(this.rawSecretData.get());
    }

    public void setRawSecretData(final String rawSecretData)
    {
        this.rawSecretData.set(rawSecretData);
    }

    public SecretData getSecretData( )
    {
        if (this.secretDataRef.get() == null)
            this.secretDataRef.set(SecretData.builder().build());

        return this.secretDataRef.get();
    }

    public void setSecretData(final SecretData secretData)
    {
        this.secretDataRef.set(secretData);
    }

    public List<ApiFieldNameTranslation> getTranslations()
    {
        return new ArrayList<>(this.translations);
    }

    public void addTranslation(final ApiFieldNameTranslation translation)
    {
        this.translations.add(translation);
    }
}
