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
package io.github.torand.openapi2java.collectors;

import io.github.torand.openapi2java.generators.Options;
import io.github.torand.openapi2java.model.AnnotationInfo;
import io.github.torand.openapi2java.model.PropertyInfo;
import io.github.torand.openapi2java.model.TypeInfo;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;

import static io.github.torand.openapi2java.utils.StringUtils.joinCsv;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;

/**
 * Collects information about a property from a schema.
 */
public class PropertyInfoCollector extends BaseCollector {
    private final TypeInfoCollector typeInfoCollector;

    public PropertyInfoCollector(SchemaResolver schemaResolver, Options opts) {
        super(opts);
        this.typeInfoCollector = new TypeInfoCollector(schemaResolver, opts);
    }

    public PropertyInfo getPropertyInfo(String name, Schema<?> property, boolean required) {
        PropertyInfo propInfo = new PropertyInfo(name)
            .withRequired(required);

        var nullabilityResolution = required
            ? TypeInfoCollector.NullabilityResolution.FROM_SCHEMA
            : TypeInfoCollector.NullabilityResolution.FORCE_NULLABLE;
        propInfo = propInfo.withType(typeInfoCollector.getTypeInfo(property, nullabilityResolution));

        if (opts.addMpOpenApiAnnotations()) {
            AnnotationInfo schemaAnnotation = getSchemaAnnotation(property, propInfo.type());
            propInfo = propInfo.withAddedAnnotation(schemaAnnotation);
        }

        if (opts.addJsonPropertyAnnotations()) {
            AnnotationInfo jsonPropAnnotation = getJsonPropertyAnnotation(name);
            propInfo = propInfo.withAddedAnnotation(jsonPropAnnotation);
        }

        if (TRUE.equals(property.getDeprecated())) {
            propInfo = propInfo.withDeprecationMessage(formatDeprecationMessage(property.getExtensions()));
        }

        return propInfo;
    }

    private AnnotationInfo getSchemaAnnotation(Schema<?> property, TypeInfo typeInfo) {
        boolean required = !typeInfoCollector.isNullable(property) && !typeInfo.nullable();

        List<String> schemaParams = new ArrayList<>();
        schemaParams.add("description = \"%s\"".formatted(normalizeDescription(property.getDescription())));
        if (required) {
            schemaParams.add("required = true");
        }
        if (nonNull(property.getDefault())) {
            schemaParams.add("defaultValue = \"%s\"".formatted(property.getDefault().toString()));
        }
        if (nonNull(typeInfo.schemaFormat())) {
            schemaParams.add("format = \"%s\"".formatted(typeInfo.schemaFormat()));
        }
        if (nonNull(typeInfo.schemaPattern())) {
            schemaParams.add("pattern = \"%s\"".formatted(typeInfo.schemaPattern()));
        }
        if (TRUE.equals(property.getDeprecated())) {
            schemaParams.add("deprecated = true");
        }

        return new AnnotationInfo(
            "@Schema(%s)".formatted(joinCsv(schemaParams)),
            "org.eclipse.microprofile.openapi.annotations.media.Schema"
        );
    }

    private AnnotationInfo getJsonPropertyAnnotation(String name) {
        return new AnnotationInfo(
            "@JsonProperty(\"%s\")".formatted(name),
            "com.fasterxml.jackson.annotation.JsonProperty"
        );
    }
}
