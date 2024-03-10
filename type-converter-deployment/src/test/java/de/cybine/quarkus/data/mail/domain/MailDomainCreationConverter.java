package de.cybine.quarkus.data.mail.domain;

import de.cybine.quarkus.util.converter.*;
import io.quarkus.arc.*;

@Unremovable
public class MailDomainCreationConverter implements Converter<MailDomainCreationParams, MailDomain>
{
    @Override
    public Class<MailDomainCreationParams> getInputType( )
    {
        return MailDomainCreationParams.class;
    }

    @Override
    public Class<MailDomain> getOutputType( )
    {
        return MailDomain.class;
    }

    @Override
    public MailDomain convert(MailDomainCreationParams input, ConversionHelper helper)
    {
        return MailDomain.builder()
                         .domain(input.getDomain())
                         .action(input.getAction())
                         .build();
    }
}
