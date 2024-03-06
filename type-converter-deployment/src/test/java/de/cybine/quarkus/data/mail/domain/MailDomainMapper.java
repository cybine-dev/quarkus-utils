package de.cybine.quarkus.data.mail.domain;

import de.cybine.quarkus.data.mail.address.*;
import de.cybine.quarkus.data.mail.tls.*;
import de.cybine.quarkus.data.mail.user.*;
import de.cybine.quarkus.data.util.primitive.*;
import de.cybine.quarkus.util.converter.*;

public class MailDomainMapper implements EntityMapper<MailDomainEntity, MailDomain>
{
    @Override
    public Class<MailDomainEntity> getEntityType( )
    {
        return MailDomainEntity.class;
    }

    @Override
    public Class<MailDomain> getDataType( )
    {
        return MailDomain.class;
    }

    @Override
    public ConverterMetadataBuilder getToEntityMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(MailTLSPolicy.class, MailTLSPolicyEntity.class)
                       .withRelation(MailUser.class, MailUserEntity.class)
                       .withRelation(MailAddress.class, MailAddressEntity.class);
    }

    @Override
    public ConverterMetadataBuilder getToDataMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(MailTLSPolicyEntity.class, MailTLSPolicy.class)
                       .withRelation(MailUserEntity.class, MailUser.class)
                       .withRelation(MailAddressEntity.class, MailAddress.class);
    }

    @Override
    public MailDomainEntity toEntity(MailDomain data, ConversionHelper helper)
    {
        return MailDomainEntity.builder()
                               .id(data.findId().map(Id::getValue).orElse(null))
                               .domain(data.getDomain())
                               .action(data.getAction())
                               .tlsPolicy(helper.toItem(MailTLSPolicy.class, MailTLSPolicyEntity.class)
                                                .map(data::getTlsPolicy))
                               .users(helper.toSet(MailUser.class, MailUserEntity.class).map(data::getUsers))
                               .addresses(
                                       helper.toSet(MailAddress.class, MailAddressEntity.class).map(data::getAddresses))
                               .build();
    }

    @Override
    public MailDomain toData(MailDomainEntity entity, ConversionHelper helper)
    {
        return MailDomain.builder()
                         .id(helper.optional(entity::getId).map(MailDomainId::of).orElse(null))
                         .domain(entity.getDomain())
                         .action(entity.getAction())
                         .tlsPolicy(helper.toItem(MailTLSPolicyEntity.class, MailTLSPolicy.class)
                                          .map(entity::getTlsPolicy))
                         .users(helper.toSet(MailUserEntity.class, MailUser.class).map(entity::getUsers))
                         .addresses(helper.toSet(MailAddressEntity.class, MailAddress.class).map(entity::getAddresses))
                         .build();
    }
}
