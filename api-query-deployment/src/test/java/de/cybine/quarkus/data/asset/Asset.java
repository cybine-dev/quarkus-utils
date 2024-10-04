package de.cybine.quarkus.data.asset;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.data.library.*;
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
public class Asset implements Serializable, WithId<AssetId>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private final AssetId id;

    @JsonProperty("external_id")
    private final String externalId;

    @JsonProperty("library_id")
    private final LibraryId libraryId;

    @JsonProperty("library")
    private final Library library;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("rentals")
    private final List<Rental> rentals;

    public Optional<String> getExternalId( )
    {
        return Optional.ofNullable(this.externalId);
    }

    public Optional<Library> getLibrary( )
    {
        return Optional.ofNullable(this.library);
    }

    public Optional<String> getDescription( )
    {
        return Optional.ofNullable(this.description);
    }

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
