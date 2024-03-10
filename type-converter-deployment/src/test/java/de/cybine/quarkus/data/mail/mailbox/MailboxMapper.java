package de.cybine.quarkus.data.mail.mailbox;

import de.cybine.quarkus.data.mail.address.*;
import de.cybine.quarkus.data.mail.user.*;
import de.cybine.quarkus.data.util.primitive.*;
import de.cybine.quarkus.util.converter.*;

public class MailboxMapper implements EntityMapper<MailboxEntity, Mailbox>
{
    @Override
    public Class<MailboxEntity> getEntityType( )
    {
        return MailboxEntity.class;
    }

    @Override
    public Class<Mailbox> getDataType( )
    {
        return Mailbox.class;
    }

    @Override
    public ConverterMetadataBuilder getToEntityMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(MailAddress.class, MailAddressEntity.class)
                       .withRelation(MailUser.class, MailUserEntity.class);
    }

    @Override
    public ConverterMetadataBuilder getToDataMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(MailAddressEntity.class, MailAddress.class)
                       .withRelation(MailUserEntity.class, MailUser.class);
    }

    @Override
    public MailboxEntity toEntity(Mailbox data, ConversionHelper helper)
    {
        return MailboxEntity.builder()
                            .id(data.findId().map(Id::getValue).orElse(null))
                            .name(data.getName())
                            .description(data.getDescription().orElse(null))
                            .isEnabled(data.isEnabled())
                            .quota(data.getQuota())
                            .sourceAddresses(helper.toSet(MailAddress.class, MailAddressEntity.class)
                                                   .map(data::getSourceAddresses))
                            .users(helper.toSet(MailUser.class, MailUserEntity.class).map(data::getUsers))
                            .build();
    }

    @Override
    public Mailbox toData(MailboxEntity entity, ConversionHelper helper)
    {
        return Mailbox.builder()
                      .id(helper.optional(entity::getId).map(MailboxId::of).orElse(null))
                      .name(entity.getName())
                      .description(entity.getDescription().orElse(null))
                      .isEnabled(entity.getIsEnabled())
                      .quota(entity.getQuota())
                      .sourceAddresses(
                              helper.toSet(MailAddressEntity.class, MailAddress.class).map(entity::getSourceAddresses))
                      .users(helper.toSet(MailUserEntity.class, MailUser.class).map(entity::getUsers))
                      .build();
    }
}
