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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static io.github.torand.javacommons.collection.CollectionHelper.concatStream;
import static io.github.torand.javacommons.collection.CollectionHelper.streamSafely;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Describes a type.
 */
public record TypeInfo (
    String name,
    String description,
    boolean nullable,
    TypeInfo keyType,
    boolean primitive,
    TypeInfo itemType,
    String schemaFormat,
    String schemaPattern,
    List<AnnotationInfo> annotations,
    ImportInfo imports
) implements ImportsSupplier {

    public TypeInfo() {
        this(null, null, false, null, false, null, null, null, new LinkedList(), new ImportInfo());
    }

    public TypeInfo withName(String name) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    public TypeInfo withDescription(String description) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    public TypeInfo withNullable(boolean nullable) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    public TypeInfo withKeyType(TypeInfo keyType) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    public TypeInfo withPrimitive(boolean primitive) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    public TypeInfo withItemType(TypeInfo itemType) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    public TypeInfo withSchemaFormat(String schemaFormat) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    public TypeInfo withSchemaPattern(String schemaPattern) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports);
    }

    public TypeInfo withAddedAnnotation(AnnotationInfo annotation) {
        List<AnnotationInfo> newAnnotations = new LinkedList<>(annotations);
        newAnnotations.add(annotation);
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, newAnnotations, imports);
    }

    public TypeInfo withNoAnnotations() {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, new LinkedList<>(), imports);
    }

    public TypeInfo withAddedImport(String normalImport) {
        return new TypeInfo(name, description, nullable, keyType, primitive, itemType, schemaFormat, schemaPattern, annotations, imports.withAddedNormalImport(normalImport));
    }

    public boolean isArray() {
        return nonNull(itemType);
    }

    public String getFullName() {
        if (nonNull(keyType) && nonNull(itemType)) {
            return "%s<%s,%s>".formatted(name, keyType.getFullName(), itemType.getFullName());
        } else if (nonNull(itemType)) {
            return "%s<%s>".formatted(name, itemType.getFullName());
        } else {
            return name;
        }
    }

    public Stream<String> typeImports() {
        // No static imports expected here
        return isNull(itemType) ? imports.normalImports().stream() : concatStream(imports().normalImports(), itemType.imports().normalImports());
    }

    public Stream<String> annotationImports() {
        return isNull(itemType) ?
            ImportInfo.streamNormalImports(annotations) :
            Stream.concat(ImportInfo.streamNormalImports(annotations), itemType.annotationImports());
    }
}