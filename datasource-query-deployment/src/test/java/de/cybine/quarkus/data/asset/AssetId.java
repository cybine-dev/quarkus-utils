package de.cybine.quarkus.data.asset;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import de.cybine.quarkus.data.util.*;
import de.cybine.quarkus.data.util.primitive.*;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.enums.*;
import org.eclipse.microprofile.openapi.annotations.media.*;

import java.io.*;
import java.util.*;

@Data
@RequiredArgsConstructor(staticName = "of")
@JsonDeserialize(using = AssetId.Deserializer.class)
@Schema(type = SchemaType.STRING, implementation = UUID.class)
public class AssetId implements Serializable, Id<UUID>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonValue
    @Schema(hidden = true)
    private final UUID value;

    public static AssetId create( )
    {
        return AssetId.of(UUIDv7.generate());
    }

    public static class Deserializer extends JsonDeserializer<AssetId>
    {
        @Override
        public AssetId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            // @formatter:off
            String value = p.getText();
            if (value == null)
                return null;

            return AssetId.of(UUID.fromString(value));
            // @formatter:on
        }
    }
}
