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

import io.github.torand.javacommons.collection.CollectionHelper;

import java.util.*;

import static java.util.Collections.emptyList;

/**
 * Describes a resource.
 * @param name the resource name.
 * @param imports the imports required by the resource.
 * @param annotations the annotations decorating this resource.
 * @param methods the methods of this resource.
 */
public record ResourceInfo (
    String name,
    ImportInfo imports,
    List<AnnotationInfo> annotations,
    List<MethodInfo> methods
) implements EntityInfo, ImportsSupplier {

    /**
     * Constructs a {@link ResourceInfo} object.
     * @param name the resource name.
     */
    public ResourceInfo(String name) {
        this(name, ImportInfo.empty(), emptyList(), emptyList());
    }

    /**
     * Returns a new {@link ResourceInfo} object with specified normal import added.
     * @param normalImport the import to add.
     * @return the new and updated {@link ResourceInfo} object.
     */
    public ResourceInfo withAddedNormalImport(String normalImport) {
        return new ResourceInfo(name, imports.withAddedNormalImport(normalImport), annotations, methods);
    }

    /**
     * Returns a new {@link ResourceInfo} object with specified annotation added.
     * @param annotation the annotation to add.
     * @return the new and updated {@link ResourceInfo} object.
     */
    public ResourceInfo withAddedAnnotation(AnnotationInfo annotation) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(annotations);
        newAnnotations.add(annotation);
        return new ResourceInfo(name, imports, newAnnotations, methods);
    }

    /**
     * Returns a new {@link ResourceInfo} object with specified annotations added.
     * @param annotations the annotations to add.
     * @return the new and updated {@link ResourceInfo} object.
     */
    public ResourceInfo withAddedAnnotations(Collection<AnnotationInfo> annotations) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(this.annotations);
        newAnnotations.addAll(annotations);
        return new ResourceInfo(name, imports, newAnnotations, methods);
    }

    /**
     * Returns a new {@link ResourceInfo} object with specified method added.
     * @param method the method to add.
     * @return the new and updated {@link ResourceInfo} object.
     */
    public ResourceInfo withAddedMethod(MethodInfo method) {
        List<MethodInfo> newMethods = new LinkedList<>(methods);
        newMethods.add(method);
        return new ResourceInfo(name, imports, annotations, newMethods);
    }

    /**
     * Gets whether resource has no methods.
     * @return true if resource has no methods; else false.
     */
    public boolean isEmpty() {
        return CollectionHelper.isEmpty(methods);
    }

    @Override
    public Set<String> aggregatedNormalImports() {
        Set<String> aggregated = new TreeSet<>();
        aggregated.addAll(imports.normalImports());
        methods.stream().map(p -> p.aggregatedNormalImports()).forEach(aggregated::addAll);
        annotations.stream().map(a -> a.imports().normalImports()).forEach(aggregated::addAll);
        return aggregated;
    }

    @Override
    public Set<String> aggregatedStaticImports() {
        Set<String> aggregated = new TreeSet<>();
        aggregated.addAll(imports.staticImports());
        methods.stream().map(p -> p.aggregatedStaticImports()).forEach(aggregated::addAll);
        annotations.stream().map(a -> a.imports().staticImports()).forEach(aggregated::addAll);
        return aggregated;
    }
}
