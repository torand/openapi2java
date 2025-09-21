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

import io.github.torand.javacommons.collection.CollectionHelper;

import java.util.*;

/**
 * Describes a resource.
 */
public record ResourceInfo (
    String name,
    ImportInfo imports,
    List<AnnotationInfo> annotations,
    List<MethodInfo> methods,
    MethodInfo authMethod
) implements ImportsSupplier {

    public ResourceInfo(String name) {
        this(name, new ImportInfo(), new LinkedList<>(), new LinkedList<>(), null);
    }

    public ResourceInfo withAddedImport(String normalImport) {
        return new ResourceInfo(name, imports.withAddedNormalImport(normalImport), annotations, methods, authMethod);
    }

    public ResourceInfo withAddedAnnotation(AnnotationInfo annotation) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(annotations);
        newAnnotations.add(annotation);
        return new ResourceInfo(name, imports, newAnnotations, methods, authMethod);
    }

    public ResourceInfo withAddedMethod(MethodInfo method) {
        List<MethodInfo> newMethods = new LinkedList<>(methods);
        newMethods.add(method);
        return new ResourceInfo(name, imports, annotations, newMethods, authMethod);
    }

    public ResourceInfo withAuthMethod(MethodInfo authMethod) {
        return new ResourceInfo(name, imports, annotations, methods, authMethod);
    }

    public boolean isEmpty() {
        return CollectionHelper.isEmpty(methods);
    }
}
