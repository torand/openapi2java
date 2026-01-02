/*
 * Copyright (c) 2024-2026 Tore Eide Andersen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.Map;
import java.util.Optional;

import static io.github.torand.javacommons.lang.Exceptions.illegalStateException;

/**
 * Resolves header security schemes referenced in an OpenAPI specification.
 */
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
