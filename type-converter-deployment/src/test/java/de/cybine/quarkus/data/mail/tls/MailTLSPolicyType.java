package de.cybine.quarkus.data.mail.tls;

import lombok.*;

@Getter
@RequiredArgsConstructor
public enum MailTLSPolicyType
{
    NONE("none"),
    MAY("may"),
    ENCRYPT("encrypt"),
    DANE("dane"),
    DANE_ONLY("dane-only"),
    FINGERPRINT("fingerprint"),
    VERIFY("verify"),
    SECURE("secure");

    private final String type;
}
