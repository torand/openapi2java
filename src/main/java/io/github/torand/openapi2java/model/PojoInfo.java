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

import static java.util.Objects.nonNull;

/**
 * Describes a pojo.
 */
public record PojoInfo (
    String name,
    String modelSubdir,
    String modelSubpackage,
    List<AnnotationInfo> annotations,
    List<PropertyInfo> properties,
    String deprecationMessage
) implements ImportsSupplier {

    public PojoInfo(String name) {
        this(name, null, null, new LinkedList<>(), new LinkedList<>(), null);
    }

    @Override
    public ImportInfo imports() {
        return ImportInfo.concatImports(annotations());
    }

    public PojoInfo withModelSubdir(String modelSubdir) {
        return new PojoInfo(name, modelSubdir, modelSubpackage, annotations, properties, deprecationMessage);
    }

    public PojoInfo withModelSubpackage(String modelSubpackage) {
        return new PojoInfo(name, modelSubpackage, modelSubpackage, annotations, properties, deprecationMessage);
    }

    public PojoInfo withAddedAnnotation(AnnotationInfo annotation) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(annotations);
        newAnnotations.add(annotation);
        return new PojoInfo(name, modelSubpackage, modelSubpackage, newAnnotations, properties, deprecationMessage);
    }

    public PojoInfo withAddedProperties(Collection<PropertyInfo> properties) {
        List<PropertyInfo> newProperties = new LinkedList<>(this.properties);
        newProperties.addAll(properties);
        return new PojoInfo(name, modelSubpackage, modelSubpackage, annotations, newProperties, deprecationMessage);
    }

    public PojoInfo withDeprecationMessage(String deprecationMessage) {
        return new PojoInfo(name, modelSubpackage, modelSubpackage, annotations, properties, deprecationMessage);
    }

    public boolean isDeprecated() {
        return nonNull(deprecationMessage);
    }

    public boolean isEmpty() {
        return CollectionHelper.isEmpty(properties);
    }
}
