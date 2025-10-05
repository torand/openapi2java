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
package io.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;
import java.util.Optional;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.lang.Exceptions.illegalStateException;
import static io.github.torand.openapi2java.collectors.Extensions.EXT_MODEL_SUBDIR;
import static io.github.torand.openapi2java.collectors.Extensions.extensions;
import static java.util.Objects.nonNull;

/**
 * Resolves schema components referenced in an OpenAPI specification.
 */
public class SchemaResolver {
    private final Map<String, Schema<?>> schemas;

    SchemaResolver(Map<String, Schema<?>> schemas) {
        this.schemas = schemas;
    }

    public String getTypeName(String ref) {
        return ref.replace("#/components/schemas/", "");
    }

    public Optional<String> getModelSubpackage(String ref) {
        Schema<?> schema = getOrThrow(ref);
        return extensions(schema.getExtensions())
            .getString(EXT_MODEL_SUBDIR)
            .map(subdir -> subdir.replace("/", "."));
    }

    public Optional<Schema<?>> get(String ref) {
        return Optional.ofNullable(schemas.get(getTypeName(ref)));
    }

    public Schema<?> getOrThrow(String ref) {
        return get(ref).orElseThrow(illegalStateException("Schema %s not found", ref));
    }

    public boolean isEnumType(String ref) {
        return get(ref).map(SchemaResolver::isEnumType).orElse(false);
    }

    public boolean isObjectType(String ref) {
        return get(ref).map(SchemaResolver::isObjectType).orElse(false);
    }

    public boolean isArrayType(String ref) {
        return get(ref).map(SchemaResolver::isArrayType).orElse(false);
    }

    public boolean isCompoundType(String ref) {
        return get(ref).map(SchemaResolver::isCompoundType).orElse(false);
    }

    public boolean isPrimitiveType(String ref) {
        return get(ref).map(SchemaResolver::isPrimitiveType).orElse(false);
    }

    public static boolean isEnumType(Schema<?> schema) {
        return nonNull(schema.getEnum());
    }

    public static boolean isObjectType(Schema<?> schema) {
        return nonEmpty(schema.getTypes()) && schema.getTypes().contains("object");
    }

    public static boolean isArrayType(Schema<?> schema) {
        return nonEmpty(schema.getTypes()) && schema.getTypes().contains("array");
    }

    public static boolean isCompoundType(Schema<?> schema) {
        return nonEmpty(schema.getAllOf());
    }

    /**
     * Indicates if schema represents a non-enumerated primitive JSON type, i.e. string, number, integer or boolean
     */
    public static boolean isPrimitiveType(Schema<?> schema) {
        return !isEnumType(schema) && !isObjectType(schema) && !isArrayType(schema) && !isCompoundType(schema);
    }
}
