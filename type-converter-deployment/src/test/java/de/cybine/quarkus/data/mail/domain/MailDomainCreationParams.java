package de.cybine.quarkus.data.mail.domain;

import com.fasterxml.jackson.annotation.*;
import io.smallrye.common.constraint.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.io.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailDomainCreationParams implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @JsonProperty("domain")
    private final String domain;

    @NotNull
    @JsonProperty("action")
    private final MailDomainAction action;
}
