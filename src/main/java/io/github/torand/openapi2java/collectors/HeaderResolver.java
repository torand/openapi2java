/*
 * Copyright (c) 2024-2025 Tore Eide Andersen
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

import io.swagger.v3.oas.models.headers.Header;

import java.util.Map;
import java.util.Optional;

import static io.github.torand.javacommons.lang.Exceptions.illegalStateException;

/**
 * Resolves header components referenced in an OpenAPI specification.
 */
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
