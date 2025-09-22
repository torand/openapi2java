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

import java.util.*;

/**
 * Describes an OpenAPI definition.
 */
public record OpenApiDefInfo (
    String name,
    ImportInfo imports,
    List<AnnotationInfo> annotations
) implements ImportsSupplier {

    public OpenApiDefInfo(String name) {
        this(name, new ImportInfo(), new LinkedList<>());
    }

    public OpenApiDefInfo withAddedImports(String normalImport) {
        return new OpenApiDefInfo(name, imports.withAddedNormalImport(normalImport), annotations);
    }

    public OpenApiDefInfo withAddedAnnotation(AnnotationInfo annotation) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(annotations);
        newAnnotations.add(annotation);
        return new OpenApiDefInfo(name, imports, newAnnotations);
    }
}
