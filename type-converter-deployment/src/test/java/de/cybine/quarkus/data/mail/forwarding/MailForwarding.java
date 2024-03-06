package de.cybine.quarkus.data.mail.forwarding;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.data.mail.address.*;
import de.cybine.quarkus.util.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailForwarding implements Serializable, WithId<MailForwarding.Id>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("forwarding_address_id")
    private final MailAddressId forwardingAddressId;

    @JsonProperty("forwarding_address")
    private final MailAddress forwardingAddress;

    @JsonProperty("receiver_address_id")
    private final MailAddressId receiverAddressId;

    @JsonProperty("receiver_address")
    private final MailAddress receiverAddress;

    @JsonProperty("starts_at")
    private final ZonedDateTime startsAt;

    @JsonProperty("ends_at")
    private final ZonedDateTime endsAt;

    @Override
    @JsonIgnore
    public Id getId( )
    {
        return Id.of(this.forwardingAddressId, this.receiverAddressId);
    }

    public Optional<MailAddress> getForwardingAddress( )
    {
        return Optional.ofNullable(this.forwardingAddress);
    }

    public Optional<MailAddress> getReceiverAddress( )
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

    @Data
    @AllArgsConstructor(staticName = "of")
    public static class Id implements Serializable
    {
        @Serial
        private static final long serialVersionUID = 1L;

        private final MailAddressId forwardingAddressId;

        private final MailAddressId receiverAddressId;
    }
}
