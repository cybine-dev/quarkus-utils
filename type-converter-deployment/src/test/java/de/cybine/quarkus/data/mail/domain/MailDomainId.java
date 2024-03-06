package de.cybine.quarkus.data.mail.domain;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.data.util.primitive.*;
import lombok.*;

import java.io.*;

@Data
@RequiredArgsConstructor(staticName = "of")
public class MailDomainId implements Serializable, Id<Long>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonValue
    private final Long value;
}
