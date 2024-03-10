package de.cybine.quarkus.data.mail.user;

import de.cybine.quarkus.data.mail.address.*;
import de.cybine.quarkus.data.mail.domain.*;
import de.cybine.quarkus.data.mail.mailbox.*;
import de.cybine.quarkus.util.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailUserEntity implements Serializable, WithId<Long>
{
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long domainId;

    private MailDomainEntity domain;

    private String username;
    private String passwordHash;

    private Boolean isEnabled;

    private Set<MailboxEntity>     mailboxes;
    private Set<MailAddressEntity> permittedAddresses;

    public Optional<MailDomainEntity> getDomain( )
    {
        return Optional.ofNullable(this.domain);
    }

    public Optional<Set<MailboxEntity>> getMailboxes( )
    {
        return Optional.ofNullable(this.mailboxes);
    }

    public Optional<Set<MailAddressEntity>> getPermittedAddresses( )
    {
        return Optional.ofNullable(this.permittedAddresses);
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