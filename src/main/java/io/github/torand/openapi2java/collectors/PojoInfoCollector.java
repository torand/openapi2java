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

import io.github.torand.openapi2java.generators.Options;
import io.github.torand.openapi2java.model.AnnotationInfo;
import io.github.torand.openapi2java.model.PojoInfo;
import io.github.torand.openapi2java.model.PropertyInfo;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static io.github.torand.openapi2java.collectors.Extensions.EXT_MODEL_SUBDIR;
import static io.github.torand.openapi2java.collectors.Extensions.extensions;
import static io.github.torand.openapi2java.utils.StringUtils.joinCsv;
import static java.lang.Boolean.TRUE;

/**
 * Collects information about a pojo from a schema.
 */
public class PojoInfoCollector extends BaseCollector {
    private final PropertyInfoCollector propertyInfoCollector;
    private final SchemaResolver schemaResolver;

    public PojoInfoCollector(SchemaResolver schemaResolver, Options opts) {
        super(opts);
        this.propertyInfoCollector = new PropertyInfoCollector(schemaResolver, opts);
        this.schemaResolver = schemaResolver;
    }

    public PojoInfo getPojoInfo(String name, Schema<?> schema) {
        PojoInfo pojoInfo = new PojoInfo(name);

        Optional<String> maybeModelSubdir = extensions(schema.getExtensions()).getString(EXT_MODEL_SUBDIR);
        pojoInfo = pojoInfo.withModelSubdir(maybeModelSubdir.orElse(null))
            .withModelSubpackage(maybeModelSubdir.map(this::dirPath2PackagePath).orElse(null));

        if (opts.addMpOpenApiAnnotations()) {
            pojoInfo = pojoInfo.withAddedAnnotation(getSchemaAnnotation(name, schema));
        }

        if (TRUE.equals(schema.getDeprecated())) {
            pojoInfo = pojoInfo.withDeprecationMessage(formatDeprecationMessage(schema.getExtensions()));
        }

        pojoInfo = pojoInfo.withAddedProperties(getSchemaProperties(schema));

        if (schema.getAdditionalProperties() instanceof Schema) {
            throw new IllegalStateException("Schema-based 'additionalProperties' not supported for Pojos. Please specify this inside a property schema instead.");
        }

        return pojoInfo;
    }

    private AnnotationInfo getSchemaAnnotation(String name, Schema<?> pojo) {
        List<String> schemaParams = new ArrayList<>();

        schemaParams.add("name = \"%s\"".formatted(modelName2SchemaName(name)));
        schemaParams.add("description = \"%s\"".formatted(normalizeDescription(pojo.getDescription())));
        if (TRUE.equals(pojo.getDeprecated())) {
            schemaParams.add("deprecated = true");
        }

        return new AnnotationInfo(
            "@Schema(%s)".formatted(joinCsv(schemaParams)),
            "org.eclipse.microprofile.openapi.annotations.media.Schema"
        );
    }

    private List<PropertyInfo> getSchemaProperties(Schema<?> schema) {
        List<PropertyInfo> props = new ArrayList<>();

        if (nonEmpty(schema.getAllOf())) {
            schema.getAllOf().forEach(subSchema -> props.addAll(getSchemaProperties(subSchema)));
        } else if (nonBlank(schema.get$ref())) {
            Schema<?> refSchema = schemaResolver.getOrThrow(schema.get$ref());
            return getSchemaProperties(refSchema);
        } else {
            schema.getProperties().forEach((propName, propSchema) ->
                props.add(propertyInfoCollector.getPropertyInfo(propName, propSchema, isRequired(schema, propName)))
            );
        }

        return props;
    }

    private PropertyInfo getAdditionalProperties(Schema<?> additionalProperties) {
        if (!schemaResolver.isPrimitiveType(additionalProperties)) {
            throw new IllegalStateException("Expected primitive type, but got: %s".formatted(additionalProperties.toString()));
        }

        return propertyInfoCollector.getPropertyInfo("additionalProperties", additionalProperties, false);
    }

    private boolean isRequired(Schema<?> schema, String propName) {
        return nonEmpty(schema.getRequired()) && schema.getRequired().contains(propName);
    }
}
