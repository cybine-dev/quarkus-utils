package de.cybine.quarkus.data.rental;

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
@JsonDeserialize(using = RentalId.Deserializer.class)
@Schema(type = SchemaType.STRING, implementation = UUID.class)
public class RentalId implements Serializable, Id<UUID>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonValue
    @Schema(hidden = true)
    private final UUID value;

    public static RentalId generate( )
    {
        return RentalId.of(UUIDv7.generate());
    }

    public static class Deserializer extends JsonDeserializer<RentalId>
    {
        @Override
        public RentalId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            // @formatter:off
            String value = p.getText();
            if (value == null)
                return null;

            return RentalId.of(UUID.fromString(value));
            // @formatter:on
        }
    }
}
