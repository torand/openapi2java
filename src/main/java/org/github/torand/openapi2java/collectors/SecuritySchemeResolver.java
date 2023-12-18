package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.Map;
import java.util.Optional;

public class SecuritySchemeResolver {
    private Map<String, SecurityScheme> securityShemes;

    public SecuritySchemeResolver(Map<String, SecurityScheme> securityShemes) {
        this.securityShemes = securityShemes;
    }

    public Optional<SecurityScheme> get(String name) {
        return Optional.ofNullable(securityShemes.get(name));
    }
}
