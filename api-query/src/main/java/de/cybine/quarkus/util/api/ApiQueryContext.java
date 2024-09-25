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

    private final AtomicReference<SecretData> secretDataRef = new AtomicReference<>();

    public Optional<SecretData> getSecretData( )
    {
        return Optional.ofNullable(this.secretDataRef.get());
    }

    public void setSecretData(final SecretData secretData)
    {
        this.secretDataRef.set(secretData);
    }
}
