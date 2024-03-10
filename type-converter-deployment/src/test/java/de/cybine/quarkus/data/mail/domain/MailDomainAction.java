package de.cybine.quarkus.data.mail.domain;

import lombok.*;

@Getter
@RequiredArgsConstructor
public enum MailDomainAction
{
    NONE("none"), MANAGE("manage"), SEND_ONLY("send_only");

    private final String action;
}
