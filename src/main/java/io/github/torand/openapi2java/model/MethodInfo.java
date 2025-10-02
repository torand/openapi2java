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

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

/**
 * Describes a method.
 * @param name the method name.
 * @param parameters the method parameters.
 * @param returnType the method return type.
 * @param deprecationMessage the deprecation message, if any.
 * @param annotations the annotations decorating this method.
 */
public record MethodInfo (
    String name,
    List<MethodParamInfo> parameters,
    String returnType,
    String deprecationMessage,
    List<AnnotationInfo> annotations
) implements EntityInfo {

    /**
     * Constructs a {@link MethodInfo} object.
     * @param name the method name.
     */
    public MethodInfo(String name) {
        this(name, emptyList(), null, null, emptyList());
    }

    /**
     * Returns a new {@link MethodInfo} object with specified parameters added.
     * @param params the parameters to add.
     * @return the new and updated {@link MethodInfo} object.
     */
    public MethodInfo withAddedParameters(Collection<MethodParamInfo> params) {
        List<MethodParamInfo> newParameters = new LinkedList<>(parameters);
        newParameters.addAll(params);
        return new MethodInfo(name, newParameters, returnType, deprecationMessage, annotations);
    }

    /**
     * Returns a new {@link MethodInfo} object with specified return type.
     * @param returnType the return type.
     * @return the new and updated {@link MethodInfo} object.
     */
    public MethodInfo withReturnType(String returnType) {
        return new MethodInfo(name, parameters, returnType, deprecationMessage, annotations);
    }

    /**
     * Returns a new {@link MethodInfo} object with specified deprecation message.
     * @param deprecationMessage the deprecation message.
     * @return the new and updated {@link MethodInfo} object.
     */
    public MethodInfo withDeprecationMessage(String deprecationMessage) {
        return new MethodInfo(name, parameters, returnType, deprecationMessage, annotations);
    }

    /**
     * Returns a new {@link MethodInfo} object with specified annotation added.
     * @param annotation the annotation to add.
     * @return the new and updated {@link MethodInfo} object.
     */
    public MethodInfo withAddedAnnotation(AnnotationInfo annotation) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(this.annotations);
        newAnnotations.add(annotation);
        return new MethodInfo(name, parameters, returnType, deprecationMessage, newAnnotations);
    }

    /**
     * Returns a new {@link MethodInfo} object with specified annotations added.
     * @param annotations the annotations to add.
     * @return the new and updated {@link MethodInfo} object.
     */
    public MethodInfo withAddedAnnotations(Collection<AnnotationInfo> annotations) {
        MethodInfo merged = this;
        for (AnnotationInfo annotation : annotations) {
            merged = merged.withAddedAnnotation(annotation);
        }
        return merged;
    }

    /**
     * Indicates whether the method is deprecated.
     * @return true if method is deprecated; else false.
     */
    public boolean isDeprecated() {
        return nonNull(deprecationMessage);
    }

    @Override
    public Set<String> aggregatedNormalImports() {
        Set<String> aggregated = new TreeSet<>();
        parameters.stream().map(p -> p.aggregatedNormalImports()).forEach(aggregated::addAll);
        annotations.stream().map(a -> a.imports().normalImports()).forEach(aggregated::addAll);
        return aggregated;
    }

    @Override
    public Set<String> aggregatedStaticImports() {
        Set<String> aggregated = new TreeSet<>();
        parameters.stream().map(p -> p.aggregatedStaticImports()).forEach(aggregated::addAll);
        annotations.stream().map(a -> a.imports().staticImports()).forEach(aggregated::addAll);
        return aggregated;
    }
}
