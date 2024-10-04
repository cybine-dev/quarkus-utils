package de.cybine.quarkus.data.library;

import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.util.datasource.*;
import jakarta.persistence.metamodel.*;
import lombok.experimental.*;

@UtilityClass
@StaticMetamodel(LibraryEntity.class)
public class LibraryEntity_
{
    public static final String TABLE  = "library";
    public static final String ENTITY = "Library";

    public static final String ID_COLUMN      = "id";
    public static final String NAME_COLUMN    = "name";
    public static final String ADDRESS_COLUMN = "address";

    // @formatter:off
    public static final DatasourceField ID      =
            DatasourceField.property(LibraryEntity.class, "id", Long.class);
    public static final DatasourceField NAME    =
            DatasourceField.property(LibraryEntity.class, "name", String.class);
    public static final DatasourceField ADDRESS =
            DatasourceField.property(LibraryEntity.class, "address", String.class);
    public static final DatasourceField ASSETS  =
            DatasourceField.property(LibraryEntity.class, "assets", AssetEntity.class);
    // @formatter:on

    public static volatile SingularAttribute<LibraryEntity, Long>    id;
    public static volatile SingularAttribute<LibraryEntity, String>  name;
    public static volatile SingularAttribute<LibraryEntity, String>  address;
    public static volatile ListAttribute<LibraryEntity, AssetEntity> assets;
}
