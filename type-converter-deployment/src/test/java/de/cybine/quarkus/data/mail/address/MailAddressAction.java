package de.cybine.quarkus.data.mail.address;

import lombok.*;

@Getter
@RequiredArgsConstructor
public enum MailAddressAction
{
    NONE("none"), SEND("send");

    private final String action;
}
