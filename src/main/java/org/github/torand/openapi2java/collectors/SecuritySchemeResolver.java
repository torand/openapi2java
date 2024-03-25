package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.Map;
import java.util.Optional;

import static org.github.torand.openapi2java.utils.Exceptions.illegalStateException;

public class SecuritySchemeResolver {
    private final Map<String, SecurityScheme> securityShemes;

    SecuritySchemeResolver(Map<String, SecurityScheme> securityShemes) {
        this.securityShemes = securityShemes;
    }

    public Optional<SecurityScheme> get(String name) {
        return Optional.ofNullable(securityShemes.get(name));
    }

    public SecurityScheme getOrThrow(String name) {
        return get(name).orElseThrow(illegalStateException("Security scheme %s not found", name));
    }
}
