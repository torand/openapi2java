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
package io.github.torand.openapi2java.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Describes a security requirement.
 * @param scheme the security scheme.
 * @param scopes the scopes, if any.
 * @param annotation the OpenAPI annotation, if any.
 */
public record SecurityRequirementInfo (
    String scheme,
    List<String> scopes,
    AnnotationInfo annotation
) {
    /**
     * Constructs a {@link SecurityRequirementInfo} object.
     * @param scheme the security scheme.
     */
    public SecurityRequirementInfo(String scheme) {
        this(scheme, emptyList(), null);
    }

    /**
     * Returns a new {@link SecurityRequirementInfo} object with specified scopes.
     * @param scopes the scopes.
     * @return the new and updated {@link SecurityRequirementInfo} object.
     */
    public SecurityRequirementInfo withScopes(Collection<String> scopes) {
        return new SecurityRequirementInfo(scheme, new LinkedList<>(scopes), annotation);
    }

    /**
     * Returns a new {@link SecurityRequirementInfo} object with specified OpenAPI annotation.
     * @param annotation the OpenAPI annotation.
     * @return the new and updated {@link SecurityRequirementInfo} object.
     */
    public SecurityRequirementInfo withAnnotation(AnnotationInfo annotation) {
        return new SecurityRequirementInfo(scheme, scopes, annotation);
    }
}
