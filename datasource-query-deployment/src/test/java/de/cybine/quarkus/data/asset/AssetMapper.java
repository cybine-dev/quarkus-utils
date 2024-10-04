package de.cybine.quarkus.data.asset;

import de.cybine.quarkus.data.library.*;
import de.cybine.quarkus.data.rental.*;
import de.cybine.quarkus.data.util.primitive.*;
import de.cybine.quarkus.util.converter.*;

public class AssetMapper implements EntityMapper<AssetEntity, Asset>
{
    @Override
    public Class<AssetEntity> getEntityType( )
    {
        return AssetEntity.class;
    }

    @Override
    public Class<Asset> getDataType( )
    {
        return Asset.class;
    }

    @Override
    public ConverterMetadataBuilder getToEntityMetadata(ConverterMetadataBuilder metadata)
    {
        // @formatter:off
        return metadata.withRelation(Library.class, LibraryEntity.class)
                       .withRelation(Rental.class, RentalEntity.class);
        // @formatter:on
    }

    @Override
    public ConverterMetadataBuilder getToDataMetadata(ConverterMetadataBuilder metadata)
    {
        // @formatter:off
        return metadata.withRelation(LibraryEntity.class, Library.class)
                       .withRelation(RentalEntity.class, Rental.class);
        // @formatter:on
    }

    @Override
    public AssetEntity toEntity(Asset data, ConversionHelper helper)
    {
        return AssetEntity.builder()
                          .id(data.findId().map(Id::getValue).orElse(null))
                          .externalId(data.getExternalId().orElse(null))
                          .libraryId(helper.optional(data::getLibraryId).map(Id::getValue).orElse(null))
                          .library(helper.toItem(Library.class, LibraryEntity.class).map(data::getLibrary))
                          .name(data.getName())
                          .description(data.getDescription().orElse(null))
                          .rentals(helper.toList(Rental.class, RentalEntity.class).map(data::getRentals))
                          .build();
    }

    @Override
    public Asset toData(AssetEntity entity, ConversionHelper helper)
    {
        return Asset.builder()
                    .id(entity.findId().map(AssetId::of).orElse(null))
                    .externalId(entity.getExternalId().orElse(null))
                    .libraryId(helper.optional(entity::getLibraryId).map(LibraryId::of).orElse(null))
                    .library(helper.toItem(LibraryEntity.class, Library.class).map(entity::getLibrary))
                    .name(entity.getName())
                    .description(entity.getDescription().orElse(null))
                    .rentals(helper.toList(RentalEntity.class, Rental.class).map(entity::getRentals))
                    .build();
    }
}
