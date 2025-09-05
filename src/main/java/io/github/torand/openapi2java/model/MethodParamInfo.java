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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Objects.nonNull;

/**
 * Describes a method parameter.
 */
public record MethodParamInfo (
    String name,
    Set<String> imports,
    Set<String> staticImports,
    Set<String> annotations,
    TypeInfo type,
    String comment,
    boolean nullable,
    String deprecationMessage
) {
    public MethodParamInfo() {
        this(null, new TreeSet<>(), new TreeSet<>(), new LinkedHashSet<>(), null, null, false, null);
    }

    public MethodParamInfo(String name) {
        this(name, new TreeSet<>(), new TreeSet<>(), new LinkedHashSet<>(), null, null, false, null);
    }

    public MethodParamInfo withName(String name) {
        return new MethodParamInfo(name, imports, staticImports, annotations, type, comment, nullable, deprecationMessage);
    }

    public MethodParamInfo withType(TypeInfo type) {
        return new MethodParamInfo(name, imports, staticImports, annotations, type, comment, nullable, deprecationMessage);
    }

    public MethodParamInfo withComment(String comment) {
        return new MethodParamInfo(name, imports, staticImports, annotations, type, comment, nullable, deprecationMessage);
    }

    public MethodParamInfo withNullable(boolean nullable) {
        return new MethodParamInfo(name, imports, staticImports, annotations, type, comment, nullable, deprecationMessage);
    }

    public MethodParamInfo withDeprecationMessage(String deprecationMessage) {
        return new MethodParamInfo(name, imports, staticImports, annotations, type, comment, nullable, deprecationMessage);
    }

    /**
     * Returns a new {@link MethodParamInfo} object with specified imports added.
     * @param imports the imports to add.
     * @return the new and updated {@link MethodParamInfo} object.
     */
    public MethodParamInfo withAddedImports(Collection<String> imports) {
        Set<String> newImports = new TreeSet<>(this.imports);
        newImports.addAll(imports);
        return new MethodParamInfo(name, newImports, staticImports, annotations, type, comment, nullable, deprecationMessage);
    }

    /**
     * Returns a new {@link MethodParamInfo} object with specified static import added.
     * @param staticImport the static import to add.
     * @return the new and updated {@link MethodParamInfo} object.
     */
    public MethodParamInfo withAddedStaticImport(String staticImport) {
        Set<String> newStaticImports = new TreeSet<>(this.staticImports);
        newStaticImports.add(staticImport);
        return new MethodParamInfo(name, imports, newStaticImports, annotations, type, comment, nullable, deprecationMessage);
    }

    /**
     * Returns a new {@link MethodParamInfo} object with specified annotation added.
     * @param annotation the annotation to add.
     * @return the new and updated {@link MethodParamInfo} object.
     */
    public MethodParamInfo withAddedAnnotation(AnnotationInfo annotation) {
        Set<String> newImports = new TreeSet<>(this.imports);
        newImports.addAll(annotation.imports());
        Set<String> newStaticImports = new TreeSet<>(this.staticImports);
        newStaticImports.addAll(annotation.staticImports());
        Set<String> newAnnotations = new LinkedHashSet<>(this.annotations);
        newAnnotations.add(annotation.annotation());
        return new MethodParamInfo(name, newImports, newStaticImports, newAnnotations, type, comment, nullable, deprecationMessage);
    }

    /**
     * Returns a new {@link MethodParamInfo} object with specified annotations added.
     * @param annotations the annotations to add.
     * @return the new and updated {@link MethodParamInfo} object.
     */
    public MethodParamInfo withAddedAnnotations(Collection<AnnotationInfo> annotations) {
        MethodParamInfo merged = this;
        for (AnnotationInfo annotation : annotations) {
            merged = merged.withAddedAnnotation(annotation);
        }
        return merged;
    }

    public boolean isDeprecated() {
        return nonNull(deprecationMessage);
    }
}
