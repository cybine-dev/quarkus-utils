package de.cybine.quarkus.data.asset;

import de.cybine.quarkus.data.library.*;
import de.cybine.quarkus.data.rental.*;
import de.cybine.quarkus.util.datasource.*;
import jakarta.persistence.metamodel.*;
import lombok.experimental.*;

import java.util.*;

@UtilityClass
@StaticMetamodel(AssetEntity.class)
public class AssetEntity_
{
    public static final String TABLE  = "asset";
    public static final String ENTITY = "Asset";

    public static final String ID_COLUMN          = "id";
    public static final String EXTERNAL_ID_COLUMN = "external_id";
    public static final String LIBRARY_ID_COLUMN  = "library_id";
    public static final String NAME_COLUMN        = "name";
    public static final String DESCRIPTION_COLUMN = "description";

    // @formatter:off
    public static final DatasourceField ID          =
            DatasourceField.property(AssetEntity.class, "id", UUID.class);
    public static final DatasourceField EXTERNAL_ID =
            DatasourceField.property(AssetEntity.class, "externalId", String.class);
    public static final DatasourceField LIBRARY_ID  =
            DatasourceField.property(AssetEntity.class, "libraryId", Long.class);
    public static final DatasourceField LIBRARY     =
            DatasourceField.property(AssetEntity.class, "library", LibraryEntity.class);
    public static final DatasourceField NAME        =
            DatasourceField.property(AssetEntity.class, "name", String.class);
    public static final DatasourceField DESCRIPTION =
            DatasourceField.property(AssetEntity.class, "description", String.class);
    public static final DatasourceField RENTALS     =
            DatasourceField.property(AssetEntity.class, "rentals", RentalEntity.class);
    // @formatter:on

    public static final String LIBRARY_RELATION = "library";

    public static volatile SingularAttribute<AssetEntity, UUID>          id;
    public static volatile SingularAttribute<AssetEntity, String>        externalId;
    public static volatile SingularAttribute<AssetEntity, Long>          libraryId;
    public static volatile SingularAttribute<AssetEntity, LibraryEntity> library;
    public static volatile SingularAttribute<AssetEntity, String>        name;
    public static volatile SingularAttribute<AssetEntity, String>        description;
    public static volatile ListAttribute<AssetEntity, RentalEntity>      rentals;
}
