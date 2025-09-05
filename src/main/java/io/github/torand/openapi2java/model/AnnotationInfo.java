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

import static java.util.Collections.emptySet;
import static java.util.Objects.nonNull;

/**
 * Describes an annotation.
 * @param annotation the annotation.
 * @param imports the imports required by the annotation.
 */
public record AnnotationInfo(
    String annotation,
    Set<String> imports,
    Set<String> staticImports
) {
    /**
     * Constructs an {@link AnnotationInfo} object.
     */
    public AnnotationInfo() {
        this(null, emptySet(), emptySet());
    }

    /**
     * Constructs an {@link AnnotationInfo} object.
     * @param annotation the annoation.
     */
    public AnnotationInfo(String annotation) {
        this(annotation, emptySet(), emptySet());
    }

    /**
     * Constructs an {@link AnnotationInfo} object.
     * @param annotation the annotation.
     * @param annotationImport the import required by the annotation.
     */
    public AnnotationInfo(String annotation, String annotationImport) {
        this(annotation, Set.of(annotationImport), emptySet());
    }

    /**
     * Returns a new {@link AnnotationInfo} object with annotation.
     * @param annotation the annotation.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAnnotation(String annotation) {
        return new AnnotationInfo(annotation, imports, staticImports);
    }

    /**
     * Returns a new {@link AnnotationInfo} object with specified import added.
     * @param annotationImport the import to add.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAddedImport(String annotationImport) {
        Set<String> newImports = new TreeSet<>(this.imports);
        newImports.add(annotationImport);
        return new AnnotationInfo(annotation, newImports, staticImports);
    }

    /**
     * Returns a new {@link AnnotationInfo} object with specified imports added.
     * @param annotationImports the imports to add.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAddedImports(Collection<String> annotationImports) {
        Set<String> newImports = new TreeSet<>(this.imports);
        newImports.addAll(annotationImports);
        return new AnnotationInfo(annotation, newImports, staticImports);
    }

    /**
     * Returns a new {@link AnnotationInfo} object with specified static import added.
     * @param annotationStaticImport the static import to add.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAddedStaticImport(String annotationStaticImport) {
        Set<String> newStaticImports = new TreeSet<>(this.staticImports);
        newStaticImports.add(annotationStaticImport);
        return new AnnotationInfo(annotation, imports, newStaticImports);
    }

    /**
     * Returns a new {@link AnnotationInfo} object with specified static imports added.
     * @param annotationStaticImports the static imports to add.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAddedStaticImports(Collection<String> annotationStaticImports) {
        Set<String> newStaticImports = new TreeSet<>(this.staticImports);
        newStaticImports.addAll(annotationStaticImports);
        return new AnnotationInfo(annotation, imports, newStaticImports);
    }

    /**
     * Returns a new {@link AnnotationInfo} object with imports and static imports from specified constant value added.
     * @param constant the constant value having imports and static imports to add.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAddedConstantValueImports(ConstantValue constant) {
        return nonNull(constant.staticImport()) ? withAddedStaticImport(constant.staticImport()) : this;
    }

    /**
     * Returns a new {@link AnnotationInfo} object with imports and static imports from specified constant values added.
     * @param constants the constant values having imports and static imports to add.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAddedConstantValueImports(Collection<ConstantValue> constants) {
        AnnotationInfo merged = this;
        for (ConstantValue constant : constants) {
            merged = withAddedConstantValueImports(constant);
        }

        return merged;
    }

    /**
     * Returns a new {@link AnnotationInfo} object with imports and static imports from specified annotation added.
     * @param annotation the annotation having imports and static imports to add.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAddedAllImportsFrom(AnnotationInfo annotation) {
        return withAddedImports(annotation.imports)
            .withAddedStaticImports(annotation.staticImports);
    }

    /**
     * Returns a new {@link AnnotationInfo} object with imports and static imports from specified annotations added.
     * @param annotations the annotations having imports and static imports to add.
     * @return the new and updated {@link AnnotationInfo} object.
     */
    public AnnotationInfo withAddedAllImportsFrom(Collection<AnnotationInfo> annotations) {
        AnnotationInfo merged = this;
        for (AnnotationInfo annotation : annotations) {
            merged = withAddedAllImportsFrom(annotation);
        }

        return merged;
    }
}
