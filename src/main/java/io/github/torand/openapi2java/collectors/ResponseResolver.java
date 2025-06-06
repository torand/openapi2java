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

import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Map;
import java.util.Optional;

import static io.github.torand.javacommons.lang.Exceptions.illegalStateException;

/**
 * Resolves response components referenced in an OpenAPI specification.
 */
public class ResponseResolver {
    private final Map<String, ApiResponse> responses;

    ResponseResolver(Map<String, ApiResponse> responses) {
        this.responses = responses;
    }

    public String getResponseName(String $ref) {
        return $ref.replace("#/components/responses/", "");
    }

    public Optional<ApiResponse> get(String $ref) {
        return Optional.ofNullable(responses.get(getResponseName($ref)));
    }

    public ApiResponse getOrThrow(String $ref) {
        return get($ref).orElseThrow(illegalStateException("Response %s not found", $ref));
    }
}
