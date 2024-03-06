package de.cybine.quarkus.data.mail.tls;

import de.cybine.quarkus.data.mail.domain.*;
import de.cybine.quarkus.util.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailTLSPolicyEntity implements Serializable, WithId<Long>
{
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long domainId;

    private MailDomainEntity  domain;
    private MailTLSPolicyType type;

    private String params;

    public Optional<MailDomainEntity> getDomain( )
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
