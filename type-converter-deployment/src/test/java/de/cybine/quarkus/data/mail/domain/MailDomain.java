package de.cybine.quarkus.data.mail.domain;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.data.mail.address.*;
import de.cybine.quarkus.data.mail.tls.*;
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
public class MailDomain implements Serializable, WithId<MailDomainId>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private final MailDomainId id;

    @JsonProperty("domain")
    private final String domain;

    @JsonProperty("action")
    private final MailDomainAction action;

    @JsonProperty("tls_policy")
    private final MailTLSPolicy tlsPolicy;

    @JsonProperty("users")
    private final Set<MailUser> users;

    @JsonProperty("addresses")
    private final Set<MailAddress> addresses;

    public Optional<MailTLSPolicy> getTlsPolicy( )
    {
        return Optional.ofNullable(this.tlsPolicy);
    }

    @JsonProperty("tls_policy_id")
    public Optional<MailTLSPolicyId> getTlsPolicyId( )
    {
        return this.getTlsPolicy().map(WithId::getId);
    }

    public Optional<Set<MailUser>> getUsers( )
    {
        return Optional.ofNullable(this.users);
    }

    @JsonProperty("user_ids")
    public Optional<Set<MailUserId>> getUserIds( )
    {
        return this.getUsers().map(items -> items.stream().map(WithId::getId).collect(Collectors.toSet()));
    }

    public Optional<Set<MailAddress>> getAddresses( )
    {
        return Optional.ofNullable(this.addresses);
    }

    @JsonProperty("address_ids")
    public Optional<Set<MailAddressId>> getAddressIds( )
    {
        return this.getAddresses().map(items -> items.stream().map(WithId::getId).collect(Collectors.toSet()));
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
