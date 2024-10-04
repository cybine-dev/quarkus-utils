package de.cybine.quarkus.data.rental;

import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.data.customer.*;
import de.cybine.quarkus.util.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Data
@NoArgsConstructor
@Table(name = RentalEntity_.TABLE)
@Entity(name = RentalEntity_.ENTITY)
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RentalEntity implements Serializable, WithId<UUID>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = RentalEntity_.ID_COLUMN)
    private UUID id;

    @Column(name = RentalEntity_.ASSET_ID_COLUMN, nullable = false, insertable = false, updatable = false)
    private UUID assetId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = RentalEntity_.ASSET_ID_COLUMN, nullable = false)
    private AssetEntity asset;

    @Column(name = RentalEntity_.CUSTOMER_ID_COLUMN, nullable = false, insertable = false, updatable = false)
    private Long customerId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = RentalEntity_.CUSTOMER_ID_COLUMN, nullable = false)
    private CustomerEntity customer;

    @Column(name = RentalEntity_.BEGINS_AT_COLUMN, nullable = false)
    private LocalDate beginsAt;

    @Column(name = RentalEntity_.ENDS_AT_COLUMN, nullable = false)
    private LocalDate endsAt;

    @Column(name = RentalEntity_.RETURNED_AT_COLUMN)
    private ZonedDateTime returnedAt;

    public Optional<AssetEntity> getAsset( )
    {
        return Optional.ofNullable(this.asset);
    }

    public Optional<CustomerEntity> getCustomer( )
    {
        return Optional.ofNullable(this.customer);
    }

    public Optional<ZonedDateTime> getReturnedAt( )
    {
        return Optional.ofNullable(this.returnedAt);
    }

    @Override
    public boolean equals(Object other)
    {
        // @formatter:off
        if (other == null)
            return false;

        if (this.getClass() != other.getClass())
            return false;

        WithId<?> that = (WithId<?>) other;
        if (this.findId().isEmpty() || that.findId().isEmpty())
            return false;

        return Objects.equals(this.getId(), that.getId());
        // @formatter:on
    }

    @Override
    public int hashCode( )
    {
        return this.findId().map(Object::hashCode).orElse(0);
    }
}
