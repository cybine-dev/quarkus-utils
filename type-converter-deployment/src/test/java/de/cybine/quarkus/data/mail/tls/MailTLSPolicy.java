package de.cybine.quarkus.data.mail.tls;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.data.mail.domain.*;
import de.cybine.quarkus.util.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.io.*;
import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailTLSPolicy implements Serializable, WithId<MailTLSPolicyId>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private final MailTLSPolicyId id;

    @JsonProperty("domain_id")
    private final MailDomainId domainId;

    @JsonProperty("domain")
    private final MailDomain domain;

    @JsonProperty("type")
    private final MailTLSPolicyType type;

    @JsonProperty("params")
    private final String params;

    public Optional<MailDomain> getDomain( )
    {
        return Optional.ofNullable(this.domain);
    }

    public Optional<String> getParams( )
    {
        return Optional.ofNullable(this.params);
    }

    @Override
    public boolean equals(Object other)
    {
        if(other == null)
            return false;

        if(this.getClass() != other.getClass())
            return false;

        WithId<?> that = ((WithId<?>) other);
        if (this.findId().isEmpty() || that.findId().isEmpty())
            return false;

        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode( )
    {
        return this.findId().map(Object::hashCode).orElse(0);
    }
}
