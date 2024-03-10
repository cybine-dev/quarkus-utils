package de.cybine.quarkus.data.mail.address;

import de.cybine.quarkus.data.mail.domain.*;
import de.cybine.quarkus.data.mail.forwarding.*;
import de.cybine.quarkus.data.mail.mailbox.*;
import de.cybine.quarkus.data.mail.user.*;
import de.cybine.quarkus.data.util.primitive.*;
import de.cybine.quarkus.util.converter.*;

public class MailAddressMapper implements EntityMapper<MailAddressEntity, MailAddress>
{
    @Override
    public Class<MailAddressEntity> getEntityType( )
    {
        return MailAddressEntity.class;
    }

    @Override
    public Class<MailAddress> getDataType( )
    {
        return MailAddress.class;
    }

    @Override
    public ConverterMetadataBuilder getToEntityMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(MailDomain.class, MailDomainEntity.class)
                       .withRelation(MailForwarding.class, MailForwardingEntity.class)
                       .withRelation(Mailbox.class, MailboxEntity.class)
                       .withRelation(MailUser.class, MailUserEntity.class);
    }

    @Override
    public ConverterMetadataBuilder getToDataMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(MailDomainEntity.class, MailDomain.class)
                       .withRelation(MailForwardingEntity.class, MailForwarding.class)
                       .withRelation(MailboxEntity.class, Mailbox.class)
                       .withRelation(MailUserEntity.class, MailUser.class);
    }

    @Override
    public MailAddressEntity toEntity(MailAddress data, ConversionHelper helper)
    {
        return MailAddressEntity.builder()
                                .id(data.findId().map(Id::getValue).orElse(null))
                                .domainId(helper.optional(data::getDomainId).map(Id::getValue).orElse(null))
                                .domain(helper.toItem(MailDomain.class, MailDomainEntity.class).map(data::getDomain))
                                .name(data.getName())
                                .action(data.getAction())
                                .forwardsTo(helper.toSet(MailForwarding.class, MailForwardingEntity.class)
                                                  .map(data::getForwardsTo))
                                .receivesFrom(helper.toSet(MailForwarding.class, MailForwardingEntity.class)
                                                    .map(data.getReceivesFrom()))
                                .mailboxes(helper.toSet(Mailbox.class, MailboxEntity.class).map(data::getMailboxes))
                                .senders(helper.toSet(MailUser.class, MailUserEntity.class).map(data::getSenders))
                                .build();
    }

    @Override
    public MailAddress toData(MailAddressEntity entity, ConversionHelper helper)
    {
        return MailAddress.builder()
                          .id(MailAddressId.of(entity.getId()))
                          .domainId(helper.optional(entity::getDomainId).map(MailDomainId::of).orElse(null))
                          .domain(helper.toItem(MailDomainEntity.class, MailDomain.class).map(entity::getDomain))
                          .name(entity.getName())
                          .action(entity.getAction())
                          .forwardsTo(helper.toSet(MailForwardingEntity.class, MailForwarding.class)
                                            .map(entity::getForwardsTo))
                          .receivesFrom(helper.toSet(MailForwardingEntity.class, MailForwarding.class)
                                              .map(entity::getReceivesFrom))
                          .mailboxes(helper.toSet(MailboxEntity.class, Mailbox.class).map(entity::getMailboxes))
                          .senders(helper.toSet(MailUserEntity.class, MailUser.class).map(entity::getSenders))
                          .build();
    }
}
