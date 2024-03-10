package de.cybine.quarkus.data.mail.mailbox;

import de.cybine.quarkus.data.mail.address.*;
import de.cybine.quarkus.data.mail.user.*;
import de.cybine.quarkus.util.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailboxEntity implements Serializable, WithId<Long>
{
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;

    private Boolean isEnabled;

    private long quota;

    private Set<MailAddressEntity> sourceAddresses;
    private Set<MailUserEntity>    users;

    public Optional<String> getDescription( )
    {
        return Optional.ofNullable(this.description);
    }

    public Optional<Set<MailAddressEntity>> getSourceAddresses( )
    {
        return Optional.ofNullable(this.sourceAddresses);
    }

    public Optional<Set<MailUserEntity>> getUsers( )
    {
        return Optional.ofNullable(this.users);
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
