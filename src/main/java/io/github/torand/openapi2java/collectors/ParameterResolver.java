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

import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.Map;
import java.util.Optional;

import static io.github.torand.javacommons.lang.Exceptions.illegalStateException;

/**
 * Resolves parameter components referenced in an OpenAPI specification.
 */
public class ParameterResolver {
    private final Map<String, Parameter> parameters;

    ParameterResolver(Map<String, Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getParameterName(String $ref) {
        return $ref.replace("#/components/parameters/", "");
    }

    public Optional<Parameter> get(String $ref) {
        return Optional.ofNullable(parameters.get(getParameterName($ref)));
    }

    public Parameter getOrThrow(String $ref) {
        return get($ref).orElseThrow(illegalStateException("Parameter %s not found", $ref));
    }

    public boolean isHeaderParameter(String $ref) {
        return get($ref).map(p -> "header".equals(p.getIn())).orElse(false);
    }

    public boolean isPathParameter(String $ref) {
        return get($ref).map(p -> "path".equals(p.getIn())).orElse(false);
    }

    public boolean isQueryParameter(String $ref) {
        return get($ref).map(p -> "query".equals(p.getIn())).orElse(false);
    }
}
