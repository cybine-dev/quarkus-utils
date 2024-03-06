package de.cybine.quarkus.data.mail.forwarding;

import de.cybine.quarkus.data.mail.address.*;
import de.cybine.quarkus.data.util.primitive.*;
import de.cybine.quarkus.util.converter.*;

public class MailForwardingMapper implements EntityMapper<MailForwardingEntity, MailForwarding>
{
    @Override
    public Class<MailForwardingEntity> getEntityType( )
    {
        return MailForwardingEntity.class;
    }

    @Override
    public Class<MailForwarding> getDataType( )
    {
        return MailForwarding.class;
    }

    @Override
    public ConverterMetadataBuilder getToEntityMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(MailAddress.class, MailAddressEntity.class);
    }

    @Override
    public ConverterMetadataBuilder getToDataMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(MailAddressEntity.class, MailAddress.class);
    }

    @Override
    public MailForwardingEntity toEntity(MailForwarding data, ConversionHelper helper)
    {
        return MailForwardingEntity.builder()
                                   .forwardingAddressId(
                                           helper.optional(data::getForwardingAddressId).map(Id::getValue).orElse(null))
                                   .forwardingAddress(helper.toItem(MailAddress.class, MailAddressEntity.class)
                                                            .map(data::getForwardingAddress))
                                   .receiverAddressId(
                                           helper.optional(data::getReceiverAddressId).map(Id::getValue).orElse(null))
                                   .receiverAddress(helper.toItem(MailAddress.class, MailAddressEntity.class)
                                                          .map(data::getReceiverAddress))
                                   .startsAt(data.getStartsAt().orElse(null))
                                   .endsAt(data.getEndsAt().orElse(null))
                                   .build();
    }

    @Override
    public MailForwarding toData(MailForwardingEntity entity, ConversionHelper helper)
    {
        return MailForwarding.builder()
                             .forwardingAddressId(helper.optional(entity::getForwardingAddressId)
                                                        .map(MailAddressId::of)
                                                        .orElse(null))
                             .forwardingAddress(helper.toItem(MailAddressEntity.class, MailAddress.class)
                                                      .map(entity::getForwardingAddress))
                             .receiverAddressId(
                                     helper.optional(entity::getReceiverAddressId).map(MailAddressId::of).orElse(null))
                             .receiverAddress(helper.toItem(MailAddressEntity.class, MailAddress.class)
                                                    .map(entity::getReceiverAddress))
                             .startsAt(entity.getStartsAt().orElse(null))
                             .endsAt(entity.getEndsAt().orElse(null))
                             .build();
    }
}
