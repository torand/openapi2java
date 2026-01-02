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
package io.github.torand.openapi2java.model;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

/**
 * Describes a method parameter.
 * @param name the parameter name.
 * @param imports the imports required by the parameter.
 * @param annotations the annotations decorating this parameter.
 * @param type the parameter type.
 * @param comment the parameter comment text, if any.
 * @param nullable the nullable flag.
 * @param deprecationMessage the deprecation message, if any.
 */
public record MethodParamInfo (
    String name,
    ImportInfo imports,
    List<AnnotationInfo> annotations,
    TypeInfo type,
    String comment,
    boolean nullable,
    String deprecationMessage
) implements EntityInfo, ImportsSupplier {

    /**
     * Constructs a {@link MethodParamInfo} object.
     */
    public MethodParamInfo() {
        this(null, ImportInfo.empty(), emptyList(), null, null, false, null);
    }

    /**
     * Constructs a {@link MethodParamInfo} object.
     * @param name the parameter name.
     */
    public MethodParamInfo(String name) {
        this(name, ImportInfo.empty(), emptyList(), null, null, false, null);
    }

    /**
     * Returns a new {@link MethodParamInfo} object with specified name.
     * @param name the name.
     * @return the new and updated {@link MethodParamInfo} object.
     */
    public MethodParamInfo withName(String name) {
        return new MethodParamInfo(name, imports, annotations, type, comment, nullable, deprecationMessage);
    }

    /**
     * Returns a new {@link MethodParamInfo} object with specified type.
     * @param type the name.
     * @return the new and updated {@link MethodParamInfo} object.
     */
    public MethodParamInfo withType(TypeInfo type) {
        return new MethodParamInfo(name, imports, annotations, type, comment, nullable, deprecationMessage);
    }

    /**
     * Returns a new {@link MethodParamInfo} object with specified comment.
     * @param comment the comment.
     * @return the new and updated {@link MethodParamInfo} object.
     */
    public MethodParamInfo withComment(String comment) {
        return new MethodParamInfo(name, imports, annotations, type, comment, nullable, deprecationMessage);
    }

    /**
     * Returns a new {@link MethodParamInfo} object with specified nullable flag.
     * @param nullable the nullable flag.
     * @return the new and updated {@link MethodParamInfo} object.
     */
    public MethodParamInfo withNullable(boolean nullable) {
        return new MethodParamInfo(name, imports, annotations, type, comment, nullable, deprecationMessage);
    }

    /**
     * Returns a new {@link MethodParamInfo} object with specified deprecation message.
     * @param deprecationMessage the deprecation message.
     * @return the new and updated {@link MethodParamInfo} object.
     */
    public MethodParamInfo withDeprecationMessage(String deprecationMessage) {
        return new MethodParamInfo(name, imports, annotations, type, comment, nullable, deprecationMessage);
    }

    /**
     * Returns a new {@link MethodParamInfo} object with specified imports added.
     * @param importsSupplier the imports to add.
     * @return the new and updated {@link MethodParamInfo} object.
     */
    public MethodParamInfo withAddedImports(ImportsSupplier importsSupplier) {
        return new MethodParamInfo(name, imports.withAddedImports(importsSupplier), annotations, type, comment, nullable, deprecationMessage);
    }

    /**
     * Returns a new {@link MethodParamInfo} object with specified annotation added.
     * @param annotation the annotation to add.
     * @return the new and updated {@link MethodParamInfo} object.
     */
    public MethodParamInfo withAddedAnnotation(AnnotationInfo annotation) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(this.annotations);
        newAnnotations.add(annotation);
        return new MethodParamInfo(name, imports, newAnnotations, type, comment, nullable, deprecationMessage);
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

    /**
     * Gets whether parameter is nullable.
     * @return true if parameter is nullable; else false.
     */
    public boolean isDeprecated() {
        return nonNull(deprecationMessage);
    }

    @Override
    public Set<String> aggregatedNormalImports() {
        Set<String> aggregated = new TreeSet<>();
        aggregated.addAll(imports.normalImports());
        annotations.stream().map(a -> a.imports().normalImports()).forEach(aggregated::addAll);
        aggregated.addAll(type.aggregatedNormalImports());
        return aggregated;
    }

    @Override
    public Set<String> aggregatedStaticImports() {
        Set<String> aggregated = new TreeSet<>();
        aggregated.addAll(imports.staticImports());
        annotations.stream().map(a -> a.imports().staticImports()).forEach(aggregated::addAll);
        aggregated.addAll(type.aggregatedStaticImports());
        return aggregated;
    }
}
