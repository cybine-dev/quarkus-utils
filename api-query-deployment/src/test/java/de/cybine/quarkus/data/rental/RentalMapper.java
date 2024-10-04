package de.cybine.quarkus.data.rental;

import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.data.customer.*;
import de.cybine.quarkus.data.util.primitive.*;
import de.cybine.quarkus.util.converter.*;

public class RentalMapper implements EntityMapper<RentalEntity, Rental>
{
    @Override
    public Class<RentalEntity> getEntityType( )
    {
        return RentalEntity.class;
    }

    @Override
    public Class<Rental> getDataType( )
    {
        return Rental.class;
    }

    @Override
    public ConverterMetadataBuilder getToEntityMetadata(ConverterMetadataBuilder metadata)
    {
        // @formatter:off
        return metadata.withRelation(Asset.class, AssetEntity.class)
                       .withRelation(Customer.class, CustomerEntity.class);
        // @formatter:on
    }

    @Override
    public ConverterMetadataBuilder getToDataMetadata(ConverterMetadataBuilder metadata)
    {
        // @formatter:off
        return metadata.withRelation(AssetEntity.class, Asset.class)
                       .withRelation(CustomerEntity.class, Customer.class);
        // @formatter:on
    }

    @Override
    public RentalEntity toEntity(Rental data, ConversionHelper helper)
    {
        return RentalEntity.builder()
                           .id(data.findId().map(Id::getValue).orElse(null))
                           .assetId(helper.optional(data::getAssetId).map(Id::getValue).orElse(null))
                           .asset(helper.toItem(Asset.class, AssetEntity.class).map(data::getAsset))
                           .customerId(helper.optional(data::getCustomerId).map(Id::getValue).orElse(null))
                           .customer(helper.toItem(Customer.class, CustomerEntity.class).map(data::getCustomer))
                           .beginsAt(data.getBeginsAt())
                           .endsAt(data.getEndsAt())
                           .returnedAt(data.getReturnedAt().orElse(null))
                           .build();
    }

    @Override
    public Rental toData(RentalEntity entity, ConversionHelper helper)
    {
        return Rental.builder()
                     .id(entity.findId().map(RentalId::of).orElse(null))
                     .assetId(helper.optional(entity::getAssetId).map(AssetId::of).orElse(null))
                     .asset(helper.toItem(AssetEntity.class, Asset.class).map(entity::getAsset))
                     .customerId(helper.optional(entity::getCustomerId).map(CustomerId::of).orElse(null))
                     .customer(helper.toItem(CustomerEntity.class, Customer.class).map(entity::getCustomer))
                     .beginsAt(entity.getBeginsAt())
                     .endsAt(entity.getEndsAt())
                     .returnedAt(entity.getReturnedAt().orElse(null))
                     .build();
    }
}
