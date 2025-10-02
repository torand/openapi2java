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

/**
 * Describes an annotation.
 * @param annotation the annotation.
 * @param imports the imports required by the annotation.
 */
public record AnnotationInfo(
    String annotation,
    ImportInfo imports
) implements ImportsSupplier {
    /**
     * Constructs an {@link AnnotationInfo} object.
     */
    public AnnotationInfo() {
        this(null, ImportInfo.empty());
    }

    /**
     * Constructs an {@link AnnotationInfo} object.
     * @param annotation the annoation.
     */
    public AnnotationInfo(String annotation) {
        this(annotation, ImportInfo.empty());
    }

    /**
     * Constructs an {@link AnnotationInfo} object.
     * @param annotation the annotation.
     * @param annotationImport the import required by the annotation.
     */
    public AnnotationInfo(String annotation, String annotationImport) {
        this(annotation, ImportInfo.empty().withAddedNormalImport(annotationImport));
    }

    /**
     * Returns a new {@link AnnotationInfo} object with annotation.
     * @param annotation the annotation.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAnnotation(String annotation) {
        return new AnnotationInfo(annotation, imports);
    }

    /**
     * Returns a new {@link AnnotationInfo} object with specified normal import added.
     * @param normalImport the import to add.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAddedNormalImport(String normalImport) {
        return new AnnotationInfo(annotation, imports.withAddedNormalImport(normalImport));
    }

    /**
     * Returns a new {@link AnnotationInfo} object with specified static import added.
     * @param staticImport the static import to add.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAddedStaticImport(String staticImport) {
        return new AnnotationInfo(annotation, imports.withAddedStaticImport(staticImport));
    }

    /**
     * Returns a new {@link AnnotationInfo} object with specified imports added.
     * @param importSupplier the imports to add.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAddedImports(ImportsSupplier importSupplier) {
        return new AnnotationInfo(annotation, imports.withAddedImports(importSupplier));
    }

    /**
     * Returns a new {@link AnnotationInfo} object with specified imports added.
     * @param importSuppliers the imports to add.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAddedImports(Collection<? extends ImportsSupplier> importSuppliers) {
        return new AnnotationInfo(annotation, imports.withAddedImports(importSuppliers));
    }
}
