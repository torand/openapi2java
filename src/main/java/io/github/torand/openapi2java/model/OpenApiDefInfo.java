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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * Describes an OpenAPI definition.
 * @param name the application name.
 * @param imports the imports required by the OpenAPI definition.
 * @param annotations the annotations decorating this OpenAPI definition.
 */
public record OpenApiDefInfo (
    String name,
    ImportInfo imports,
    List<AnnotationInfo> annotations
) implements EntityInfo, ImportsSupplier {

    /**
     * Constructs an {@link OpenApiDefInfo} object.
     * @param name the application name.
     */
    public OpenApiDefInfo(String name) {
        this(name, ImportInfo.empty(), emptyList());
    }

    /**
     * Returns a new {@link OpenApiDefInfo} object with specified normal import added.
     * @param normalImport the import to add.
     * @return the new and updated {@link OpenApiDefInfo} object.
     */
    public OpenApiDefInfo withAddedNormalImport(String normalImport) {
        return new OpenApiDefInfo(name, imports.withAddedNormalImport(normalImport), annotations);
    }

    /**
     * Returns a new {@link OpenApiDefInfo} object with specified annotation added.
     * @param annotation the annotation to add.
     * @return the new and updated {@link OpenApiDefInfo} object.
     */
    public OpenApiDefInfo withAddedAnnotation(AnnotationInfo annotation) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(annotations);
        newAnnotations.add(annotation);
        return new OpenApiDefInfo(name, imports, newAnnotations);
    }

    @Override
    public Set<String> aggregatedNormalImports() {
        return imports.withAddedImports(annotations).normalImports();
    }

    @Override
    public Set<String> aggregatedStaticImports() {
        return imports.withAddedImports(annotations).staticImports();
    }
}
