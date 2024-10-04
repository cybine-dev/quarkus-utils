package de.cybine.quarkus.data.customer;

import de.cybine.quarkus.data.rental.*;
import de.cybine.quarkus.data.util.primitive.*;
import de.cybine.quarkus.util.converter.*;

public class CustomerMapper implements EntityMapper<CustomerEntity, Customer>
{
    @Override
    public Class<CustomerEntity> getEntityType( )
    {
        return CustomerEntity.class;
    }

    @Override
    public Class<Customer> getDataType( )
    {
        return Customer.class;
    }

    @Override
    public ConverterMetadataBuilder getToEntityMetadata(ConverterMetadataBuilder metadata)
    {
        // @formatter:off
        return metadata.withRelation(Rental.class, RentalEntity.class);
        // @formatter:on
    }

    @Override
    public ConverterMetadataBuilder getToDataMetadata(ConverterMetadataBuilder metadata)
    {
        // @formatter:off
        return metadata.withRelation(RentalEntity.class, Rental.class);
        // @formatter:on
    }

    @Override
    public CustomerEntity toEntity(Customer data, ConversionHelper helper)
    {
        return CustomerEntity.builder()
                             .id(data.findId().map(Id::getValue).orElse(null))
                             .firstname(data.getFirstname())
                             .lastname(data.getLastname())
                             .rentals(helper.toList(Rental.class, RentalEntity.class).map(data::getRentals))
                             .build();
    }

    @Override
    public Customer toData(CustomerEntity entity, ConversionHelper helper)
    {
        return Customer.builder()
                       .id(entity.findId().map(CustomerId::of).orElse(null))
                       .firstname(entity.getFirstname())
                       .lastname(entity.getLastname())
                       .rentals(helper.toList(RentalEntity.class, Rental.class).map(entity::getRentals))
                       .build();
    }
}
