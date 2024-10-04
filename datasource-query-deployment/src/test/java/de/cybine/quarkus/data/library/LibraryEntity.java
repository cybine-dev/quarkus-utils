package de.cybine.quarkus.data.library;

import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.util.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@Table(name = LibraryEntity_.TABLE)
@Entity(name = LibraryEntity_.ENTITY)
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LibraryEntity implements Serializable, WithId<Long>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = LibraryEntity_.ID_COLUMN)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = LibraryEntity_.NAME_COLUMN, nullable = false)
    private String name;

    @Column(name = LibraryEntity_.ADDRESS_COLUMN, nullable = false)
    private String address;

    @OneToMany(mappedBy = AssetEntity_.LIBRARY_RELATION)
    private List<AssetEntity> assets;

    public Optional<List<AssetEntity>> getAssets( )
    {
        return Optional.ofNullable(this.assets);
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
