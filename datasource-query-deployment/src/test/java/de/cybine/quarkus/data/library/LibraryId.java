package de.cybine.quarkus.data.library;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import de.cybine.quarkus.data.util.primitive.*;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.enums.*;
import org.eclipse.microprofile.openapi.annotations.media.*;

import java.io.*;

@Data
@RequiredArgsConstructor(staticName = "of")
@JsonDeserialize(using = LibraryId.Deserializer.class)
@Schema(type = SchemaType.INTEGER, implementation = Long.class)
public class LibraryId implements Serializable, Id<Long>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonValue
    @Schema(hidden = true)
    private final Long value;

    public static class Deserializer extends JsonDeserializer<LibraryId>
    {
        @Override
        public LibraryId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            // @formatter:off
            String value = p.getText();
            if (value == null)
                return null;

            return LibraryId.of(Long.parseLong(value));
            // @formatter:on
        }
    }
}
