package de.cybine.quarkus.data.mail.tls;

import de.cybine.quarkus.data.mail.domain.*;
import de.cybine.quarkus.data.util.primitive.*;
import de.cybine.quarkus.util.converter.*;

public class MailTLSPolicyMapper implements EntityMapper<MailTLSPolicyEntity, MailTLSPolicy>
{
    @Override
    public Class<MailTLSPolicyEntity> getEntityType( )
    {
        return MailTLSPolicyEntity.class;
    }

    @Override
    public Class<MailTLSPolicy> getDataType( )
    {
        return MailTLSPolicy.class;
    }

    @Override
    public ConverterMetadataBuilder getToEntityMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(MailDomain.class, MailDomainEntity.class);
    }

    @Override
    public ConverterMetadataBuilder getToDataMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(MailDomainEntity.class, MailDomain.class);
    }

    @Override
    public MailTLSPolicyEntity toEntity(MailTLSPolicy data, ConversionHelper helper)
    {
        return MailTLSPolicyEntity.builder()
                                  .id(data.findId().map(Id::getValue).orElse(null))
                                  .domainId(helper.optional(data::getDomainId).map(Id::getValue).orElse(null))
                                  .domain(helper.toItem(MailDomain.class, MailDomainEntity.class).map(data::getDomain))
                                  .type(data.getType())
                                  .params(data.getParams().orElse(null))
                                  .build();
    }

    @Override
    public MailTLSPolicy toData(MailTLSPolicyEntity entity, ConversionHelper helper)
    {
        return MailTLSPolicy.builder()
                            .id(helper.optional(entity::getId).map(MailTLSPolicyId::of).orElse(null))
                            .domainId(helper.optional(entity::getDomainId).map(MailDomainId::of).orElse(null))
                            .domain(helper.toItem(MailDomainEntity.class, MailDomain.class).map(entity::getDomain))
                            .type(entity.getType())
                            .params(entity.getParams().orElse(null))
                            .build();
    }
}
