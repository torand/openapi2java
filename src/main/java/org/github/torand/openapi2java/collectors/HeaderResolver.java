package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.headers.Header;

import java.util.Map;
import java.util.Optional;

import static org.github.torand.openapi2java.utils.Exceptions.illegalStateException;

public class HeaderResolver {
    private final Map<String, Header> headers;

    HeaderResolver(Map<String, Header> headers) {
        this.headers = headers;
    }

    public String getHeaderName(String $ref) {
        return $ref.replace("#/components/headers/", "");
    }

    public Optional<Header> get(String $ref) {
        return Optional.ofNullable(headers.get(getHeaderName($ref)));
    }

    public Header getOrThrow(String $ref) {
        return get($ref).orElseThrow(illegalStateException("Header %s not found", $ref));
    }
}
