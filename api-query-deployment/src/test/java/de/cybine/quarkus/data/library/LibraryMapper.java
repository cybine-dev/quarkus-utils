package de.cybine.quarkus.data.library;

import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.data.util.primitive.*;
import de.cybine.quarkus.util.converter.*;

public class LibraryMapper implements EntityMapper<LibraryEntity, Library>
{
    @Override
    public Class<LibraryEntity> getEntityType( )
    {
        return LibraryEntity.class;
    }

    @Override
    public Class<Library> getDataType( )
    {
        return Library.class;
    }

    @Override
    public ConverterMetadataBuilder getToEntityMetadata(ConverterMetadataBuilder metadata)
    {
        // @formatter:off
        return metadata.withRelation(Asset.class, AssetEntity.class);
        // @formatter:on
    }

    @Override
    public ConverterMetadataBuilder getToDataMetadata(ConverterMetadataBuilder metadata)
    {
        // @formatter:off
        return metadata.withRelation(AssetEntity.class, Asset.class);
        // @formatter:on
    }

    @Override
    public LibraryEntity toEntity(Library data, ConversionHelper helper)
    {
        return LibraryEntity.builder()
                            .id(data.findId().map(Id::getValue).orElse(null))
                            .name(data.getName())
                            .address(data.getAddress())
                            .assets(helper.toList(Asset.class, AssetEntity.class).map(data::getAssets))
                            .build();
    }

    @Override
    public Library toData(LibraryEntity entity, ConversionHelper helper)
    {
        return Library.builder()
                      .id(entity.findId().map(LibraryId::of).orElse(null))
                      .name(entity.getName())
                      .address(entity.getAddress())
                      .assets(helper.toList(AssetEntity.class, Asset.class).map(entity::getAssets))
                      .build();
    }
}
