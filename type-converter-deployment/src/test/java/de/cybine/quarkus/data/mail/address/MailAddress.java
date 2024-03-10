package de.cybine.quarkus.data.mail.address;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.data.mail.domain.*;
import de.cybine.quarkus.data.mail.forwarding.*;
import de.cybine.quarkus.data.mail.mailbox.*;
import de.cybine.quarkus.data.mail.user.*;
import de.cybine.quarkus.util.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailAddress implements Serializable, WithId<MailAddressId>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private final MailAddressId id;

    @JsonProperty("domain_id")
    private final MailDomainId domainId;

    @JsonProperty("domain")
    private final MailDomain domain;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("action")
    private final MailAddressAction action;

    @JsonProperty("forwards_to")
    private final Set<MailForwarding> forwardsTo;

    @JsonProperty("receives_from")
    private final Set<MailForwarding> receivesFrom;

    @JsonProperty("mailboxes")
    private final Set<Mailbox> mailboxes;

    @JsonProperty("senders")
    private final Set<MailUser> senders;

    public Optional<MailDomain> getDomain( )
    {
        return Optional.ofNullable(this.domain);
    }

    public Optional<Set<MailForwarding>> getForwardsTo( )
    {
        return Optional.ofNullable(this.forwardsTo);
    }

    @JsonProperty("forwards_to_ids")
    public Optional<Set<MailAddressId>> getForwardsToIds( )
    {
        return this.getForwardsTo()
                   .map(items -> items.stream()
                                      .map(MailForwarding::getForwardingAddressId)
                                      .collect(Collectors.toSet()));
    }

    public Optional<Set<MailForwarding>> getReceivesFrom( )
    {
        return Optional.ofNullable(this.receivesFrom);
    }

    @JsonProperty("receives_from_ids")
    public Optional<Set<MailAddressId>> getReceivesFromIds( )
    {
        return this.getReceivesFrom()
                   .map(items -> items.stream().map(MailForwarding::getReceiverAddressId).collect(Collectors.toSet()));
    }

    public Optional<Set<Mailbox>> getMailboxes( )
    {
        return Optional.ofNullable(this.mailboxes);
    }

    @JsonProperty("mailbox_ids")
    public Optional<Set<MailboxId>> getMailboxIds( )
    {
        return this.getMailboxes().map(items -> items.stream().map(WithId::getId).collect(Collectors.toSet()));
    }

    public Optional<Set<MailUser>> getSenders( )
    {
        return Optional.ofNullable(this.senders);
    }

    @JsonProperty("sender_ids")
    public Optional<Set<MailUserId>> getSenderIds( )
    {
        return this.getSenders().map(items -> items.stream().map(WithId::getId).collect(Collectors.toSet()));
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
