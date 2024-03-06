package de.cybine.quarkus.data.mail.mailbox;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.data.util.primitive.*;
import lombok.*;

import java.io.*;

@Data
@RequiredArgsConstructor(staticName = "of")
public class MailboxId implements Serializable, Id<Long>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonValue
    private final Long value;
}
