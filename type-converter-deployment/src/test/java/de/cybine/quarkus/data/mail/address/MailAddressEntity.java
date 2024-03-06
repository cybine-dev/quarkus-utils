package de.cybine.quarkus.data.mail.address;

import de.cybine.quarkus.data.mail.domain.*;
import de.cybine.quarkus.data.mail.forwarding.*;
import de.cybine.quarkus.data.mail.mailbox.*;
import de.cybine.quarkus.data.mail.user.*;
import de.cybine.quarkus.util.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailAddressEntity implements Serializable, WithId<Long>
{
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long domainId;

    private MailDomainEntity domain;

    private String name;

    private MailAddressAction action;

    private Set<MailForwardingEntity> forwardsTo;
    private Set<MailForwardingEntity> receivesFrom;
    private Set<MailboxEntity>        mailboxes;
    private Set<MailUserEntity>       senders;

    public Optional<MailDomainEntity> getDomain( )
    {
        return Optional.ofNullable(this.domain);
    }

    public Optional<Set<MailForwardingEntity>> getForwardsTo( )
    {
        return Optional.ofNullable(this.forwardsTo);
    }

    public Optional<Set<MailForwardingEntity>> getReceivesFrom( )
    {
        return Optional.ofNullable(this.receivesFrom);
    }

    public Optional<Set<MailboxEntity>> getMailboxes( )
    {
        return Optional.ofNullable(this.mailboxes);
    }

    public Optional<Set<MailUserEntity>> getSenders( )
    {
        return Optional.ofNullable(this.senders);
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
