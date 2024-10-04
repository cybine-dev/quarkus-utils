package de.cybine.quarkus.data.customer;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.data.rental.*;
import de.cybine.quarkus.util.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.io.*;
import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Customer implements Serializable, WithId<CustomerId>
{
    @Serial
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("id")
    private final CustomerId id;
    
    @JsonProperty("firstname")
    private final String firstname;
    
    @JsonProperty("lastname")
    private final String lastname;

    @JsonProperty("rentals")
    private final List<Rental> rentals;

    public Optional<List<Rental>> getRentals( )
    {
        return Optional.ofNullable(this.rentals);
    }

    @JsonProperty("rental_ids")
    public Optional<List<RentalId>> getRentalIds( )
    {
        return this.getRentals().map(item -> item.stream().map(WithId::getId).toList());
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
