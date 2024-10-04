package de.cybine.quarkus.util.api;

import lombok.*;

@Getter
@RequiredArgsConstructor
public enum ApiQueryTask
{
    FETCH("fetch"), FETCH_SINGLE("fetch_single"), OPTIONS("options"), COUNT("count");

    private final String action;
}
