package de.cybine.quarkus.data.customer;

import de.cybine.quarkus.data.rental.*;
import de.cybine.quarkus.util.datasource.*;
import jakarta.persistence.metamodel.*;
import lombok.experimental.*;

@UtilityClass
@StaticMetamodel(CustomerEntity.class)
public class CustomerEntity_
{
    public static final String TABLE  = "customer";
    public static final String ENTITY = "Customer";

    public static final String ID_COLUMN        = "id";
    public static final String FIRSTNAME_COLUMN = "firstname";
    public static final String LASTNAME_COLUMN  = "lastname";

    // @formatter:off
    public static final DatasourceField ID        =
            DatasourceField.property(CustomerEntity.class, "id", Long.class);
    public static final DatasourceField FIRSTNAME =
            DatasourceField.property(CustomerEntity.class, "firstname", String.class);
    public static final DatasourceField LASTNAME  =
            DatasourceField.property(CustomerEntity.class, "lastname", String.class);
    public static final DatasourceField RENTALS   =
            DatasourceField.property(CustomerEntity.class, "rentals", RentalEntity.class);
    // @formatter:on

    public static volatile SingularAttribute<CustomerEntity, Long>     id;
    public static volatile SingularAttribute<CustomerEntity, String>   firstname;
    public static volatile SingularAttribute<CustomerEntity, String>   lastname;
    public static volatile ListAttribute<CustomerEntity, RentalEntity> rentals;
}
