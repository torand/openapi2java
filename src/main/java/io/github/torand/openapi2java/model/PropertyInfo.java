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

import static java.util.Objects.nonNull;

/**
 * Describes a property.
 */
public record PropertyInfo (
    String name,
    TypeInfo type,
    boolean required,
    List<AnnotationInfo> annotations,
    String deprecationMessage
) implements ImportsSupplier {

    public PropertyInfo(String name) {
        this(name, null, false, new LinkedList<>(), null);
    }

    @Override
    public ImportInfo imports() {
        return ImportInfo.concatImports(annotations());
    }

    public PropertyInfo withType(TypeInfo type) {
        return new PropertyInfo(name, type, required, annotations, deprecationMessage);
    }

    public PropertyInfo withRequired(boolean required) {
        return new PropertyInfo(name, type, required, annotations, deprecationMessage);
    }

    public PropertyInfo withAddedAnnotation(AnnotationInfo annotation) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(annotations);
        newAnnotations.add(annotation);
        return new PropertyInfo(name, type, required, newAnnotations, deprecationMessage);
    }

    public PropertyInfo withDeprecationMessage(String deprecationMessage) {
        return new PropertyInfo(name, type, required, annotations, deprecationMessage);
    }

    public boolean isDeprecated() {
        return nonNull(deprecationMessage);
    }
}
