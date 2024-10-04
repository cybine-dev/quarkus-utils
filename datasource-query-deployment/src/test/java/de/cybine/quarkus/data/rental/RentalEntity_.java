package de.cybine.quarkus.data.rental;

import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.data.customer.*;
import de.cybine.quarkus.util.datasource.*;
import jakarta.persistence.metamodel.*;
import lombok.experimental.*;

import java.time.*;
import java.util.*;

@UtilityClass
@StaticMetamodel(RentalEntity.class)
public class RentalEntity_
{
    public static final String TABLE  = "asset_rental";
    public static final String ENTITY = "Rental";

    public static final String ID_COLUMN          = "id";
    public static final String ASSET_ID_COLUMN    = "asset_id";
    public static final String CUSTOMER_ID_COLUMN = "customer_id";
    public static final String BEGINS_AT_COLUMN   = "begins_at";
    public static final String ENDS_AT_COLUMN     = "ends_at";
    public static final String RETURNED_AT_COLUMN = "returned_at";

    // @formatter:off
    public static final DatasourceField ID          =
            DatasourceField.property(RentalEntity.class, "id", UUID.class);
    public static final DatasourceField ASSET_ID    =
            DatasourceField.property(RentalEntity.class, "assetId", UUID.class);
    public static final DatasourceField ASSET       =
            DatasourceField.property(RentalEntity.class, "asset", AssetEntity.class);
    public static final DatasourceField CUSTOMER_ID =
            DatasourceField.property(RentalEntity.class, "customerId", Long.class);
    public static final DatasourceField CUSTOMER    =
            DatasourceField.property(RentalEntity.class, "customer", CustomerEntity.class);
    public static final DatasourceField BEGINS_AT   =
            DatasourceField.property(RentalEntity.class, "beginsAt", LocalDate.class);
    public static final DatasourceField ENDS_AT     =
            DatasourceField.property(RentalEntity.class, "endsAt", LocalDate.class);
    public static final DatasourceField RETURNED_AT =
            DatasourceField.property(RentalEntity.class, "returnedAt", ZonedDateTime.class);
    // @formatter:on

    public static final String ASSET_RELATION    = "asset";
    public static final String CUSTOMER_RELATION = "customer";

    public static volatile SingularAttribute<RentalEntity, UUID>           id;
    public static volatile SingularAttribute<RentalEntity, UUID>           assetId;
    public static volatile SingularAttribute<RentalEntity, AssetEntity>    asset;
    public static volatile SingularAttribute<RentalEntity, Long>           customerId;
    public static volatile SingularAttribute<RentalEntity, CustomerEntity> customer;
    public static volatile SingularAttribute<RentalEntity, LocalDate>      beginsAt;
    public static volatile SingularAttribute<RentalEntity, LocalDate>      endsAt;
    public static volatile SingularAttribute<RentalEntity, ZonedDateTime>  returnedAt;
}
