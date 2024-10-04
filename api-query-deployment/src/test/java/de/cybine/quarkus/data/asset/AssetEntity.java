package de.cybine.quarkus.data.asset;

import de.cybine.quarkus.data.library.*;
import de.cybine.quarkus.data.rental.*;
import de.cybine.quarkus.util.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@Table(name = AssetEntity_.TABLE)
@Entity(name = AssetEntity_.ENTITY)
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetEntity implements Serializable, WithId<UUID>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = AssetEntity_.ID_COLUMN)
    private UUID id;

    @Column(name = AssetEntity_.EXTERNAL_ID_COLUMN)
    private String externalId;

    @Column(name = AssetEntity_.LIBRARY_ID_COLUMN, nullable = false, insertable = false, updatable = false)
    private Long libraryId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = AssetEntity_.LIBRARY_ID_COLUMN, nullable = false)
    private LibraryEntity library;

    @Column(name = AssetEntity_.NAME_COLUMN, nullable = false)
    private String name;

    @Column(name = AssetEntity_.DESCRIPTION_COLUMN)
    private String description;

    @OneToMany(mappedBy = RentalEntity_.ASSET_RELATION)
    private List<RentalEntity> rentals;

    public Optional<String> getExternalId( )
    {
        return Optional.ofNullable(this.externalId);
    }

    public Optional<LibraryEntity> getLibrary( )
    {
        return Optional.ofNullable(this.library);
    }

    public Optional<String> getDescription( )
    {
        return Optional.ofNullable(this.description);
    }

    public Optional<List<RentalEntity>> getRentals( )
    {
        return Optional.ofNullable(this.rentals);
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
