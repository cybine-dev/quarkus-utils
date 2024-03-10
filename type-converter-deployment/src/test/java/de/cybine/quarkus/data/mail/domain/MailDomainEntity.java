package de.cybine.quarkus.data.mail.domain;

import de.cybine.quarkus.data.mail.address.*;
import de.cybine.quarkus.data.mail.tls.*;
import de.cybine.quarkus.data.mail.user.*;
import de.cybine.quarkus.util.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailDomainEntity implements Serializable, WithId<Long>
{
    @Serial
    private static final long serialVersionUID = 1L;

    private Long   id;
    private String domain;

    private MailDomainAction    action;
    private MailTLSPolicyEntity tlsPolicy;

    private Set<MailUserEntity>    users;
    private Set<MailAddressEntity> addresses;

    public Optional<MailTLSPolicyEntity> getTlsPolicy( )
    {
        return Optional.ofNullable(this.tlsPolicy);
    }

    public Optional<Set<MailUserEntity>> getUsers( )
    {
        return Optional.ofNullable(this.users);
    }

    public Optional<Set<MailAddressEntity>> getAddresses( )
    {
        return Optional.ofNullable(this.addresses);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;

        if (this.getClass() != other.getClass())
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