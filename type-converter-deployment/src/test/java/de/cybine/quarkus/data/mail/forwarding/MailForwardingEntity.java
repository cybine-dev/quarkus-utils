package de.cybine.quarkus.data.mail.forwarding;

import de.cybine.quarkus.data.mail.address.*;
import de.cybine.quarkus.util.*;
import lombok.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Data
@NoArgsConstructor
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailForwardingEntity implements Serializable, WithId<MailForwarding.Id>
{
    @Serial
    private static final long serialVersionUID = 1L;

    private Long forwardingAddressId;
    private Long receiverAddressId;

    private MailAddressEntity forwardingAddress;
    private MailAddressEntity receiverAddress;

    private ZonedDateTime startsAt;

    private ZonedDateTime endsAt;

    public Optional<MailAddressEntity> getForwardingAddress( )
    {
        return Optional.ofNullable(this.forwardingAddress);
    }

    public Optional<MailAddressEntity> getReceiverAddress( )
    {
        return Optional.ofNullable(this.receiverAddress);
    }

    public Optional<ZonedDateTime> getStartsAt( )
    {
        return Optional.ofNullable(this.startsAt);
    }

    public Optional<ZonedDateTime> getEndsAt( )
    {
        return Optional.ofNullable(this.endsAt);
    }

    @Override
    public MailForwarding.Id getId( )
    {
        return MailForwarding.Id.of(MailAddressId.of(this.forwardingAddressId),
                MailAddressId.of(this.receiverAddressId));
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
