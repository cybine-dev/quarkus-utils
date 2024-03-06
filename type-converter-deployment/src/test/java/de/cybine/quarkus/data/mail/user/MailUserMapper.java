package de.cybine.quarkus.data.mail.user;

import de.cybine.quarkus.data.mail.address.*;
import de.cybine.quarkus.data.mail.domain.*;
import de.cybine.quarkus.data.mail.mailbox.*;
import de.cybine.quarkus.data.util.primitive.*;
import de.cybine.quarkus.util.converter.*;

public class MailUserMapper implements EntityMapper<MailUserEntity, MailUser>
{
    @Override
    public Class<MailUserEntity> getEntityType( )
    {
        return MailUserEntity.class;
    }

    @Override
    public Class<MailUser> getDataType( )
    {
        return MailUser.class;
    }

    @Override
    public ConverterMetadataBuilder getToEntityMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(MailDomain.class, MailDomainEntity.class)
                       .withRelation(Mailbox.class, MailboxEntity.class)
                       .withRelation(MailAddress.class, MailAddressEntity.class);
    }

    @Override
    public ConverterMetadataBuilder getToDataMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(MailDomainEntity.class, MailDomain.class)
                       .withRelation(MailboxEntity.class, Mailbox.class)
                       .withRelation(MailAddressEntity.class, MailAddress.class);
    }

    @Override
    public MailUserEntity toEntity(MailUser data, ConversionHelper helper)
    {
        return MailUserEntity.builder()
                             .id(data.findId().map(Id::getValue).orElse(null))
                             .domainId(helper.optional(data::getDomainId).map(Id::getValue).orElse(null))
                             .domain(helper.toItem(MailDomain.class, MailDomainEntity.class).map(data::getDomain))
                             .username(data.getUsername())
                             .passwordHash(data.getPasswordHash())
                             .isEnabled(data.isEnabled())
                             .mailboxes(helper.toSet(Mailbox.class, MailboxEntity.class).map(data::getMailboxes))
                             .permittedAddresses(helper.toSet(MailAddress.class, MailAddressEntity.class)
                                                       .map(data::getPermittedAddresses))
                             .build();
    }

    @Override
    public MailUser toData(MailUserEntity entity, ConversionHelper helper)
    {
        return MailUser.builder()
                       .id(helper.optional(entity::getId).map(MailUserId::of).orElse(null))
                       .domainId(helper.optional(entity::getDomainId).map(MailDomainId::of).orElse(null))
                       .domain(helper.toItem(MailDomainEntity.class, MailDomain.class).map(entity::getDomain))
                       .username(entity.getUsername())
                       .passwordHash(entity.getPasswordHash())
                       .isEnabled(entity.getIsEnabled())
                       .mailboxes(helper.toSet(MailboxEntity.class, Mailbox.class).map(entity::getMailboxes))
                       .permittedAddresses(helper.toSet(MailAddressEntity.class, MailAddress.class)
                                                 .map(entity::getPermittedAddresses))
                       .build();
    }
}
