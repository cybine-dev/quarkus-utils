package de.cybine.quarkus.data.library;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.data.asset.*;
import de.cybine.quarkus.util.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.io.*;
import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Library implements Serializable, WithId<LibraryId>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private final LibraryId id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("address")
    private final String address;

    @JsonProperty("assets")
    private final List<Asset> assets;

    public Optional<List<Asset>> getAssets( )
    {
        return Optional.ofNullable(this.assets);
    }

    @JsonProperty("asset_ids")
    public Optional<List<AssetId>> getAssetIds()
    {
        return this.getAssets().map(item -> item.stream().map(WithId::getId).toList());
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
