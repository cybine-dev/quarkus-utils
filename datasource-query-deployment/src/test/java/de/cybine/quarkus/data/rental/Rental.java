package de.cybine.quarkus.data.rental;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.data.customer.*;
import de.cybine.quarkus.util.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Rental implements Serializable, WithId<RentalId>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private final RentalId id;

    @JsonProperty("asset_id")
    private final AssetId assetId;

    @JsonProperty("asset")
    private final Asset asset;

    @JsonProperty("customer_id")
    private final CustomerId customerId;

    @JsonProperty("customer")
    private final Customer customer;

    @JsonProperty("begins_at")
    private final LocalDate beginsAt;

    @JsonProperty("ends_at")
    private final LocalDate endsAt;

    @JsonProperty("returned_at")
    private final ZonedDateTime returnedAt;

    public Optional<Customer> getCustomer( )
    {
        return Optional.ofNullable(this.customer);
    }

    public Optional<Asset> getAsset( )
    {
        return Optional.ofNullable(this.asset);
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
