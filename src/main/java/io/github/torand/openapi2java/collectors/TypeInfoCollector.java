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
import io.github.torand.openapi2java.model.TypeInfo;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.torand.javacommons.collection.CollectionHelper.isEmpty;
import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.lang.Exceptions.illegalStateException;
import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static io.github.torand.javacommons.stream.StreamHelper.streamSafely;
import static io.github.torand.openapi2java.collectors.Extensions.EXT_JSON_SERIALIZER;
import static io.github.torand.openapi2java.collectors.Extensions.EXT_NULLABLE;
import static io.github.torand.openapi2java.collectors.Extensions.EXT_VALIDATION_CONSTRAINT;
import static io.github.torand.openapi2java.collectors.Extensions.extensions;
import static io.github.torand.openapi2java.collectors.TypeInfoCollector.NullabilityResolution.FORCE_NOT_NULLABLE;
import static io.github.torand.openapi2java.collectors.TypeInfoCollector.NullabilityResolution.FORCE_NULLABLE;
import static io.github.torand.openapi2java.utils.StringUtils.getClassNameFromFqn;
import static io.github.torand.openapi2java.utils.StringUtils.joinCsv;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;

/**
 * Collects information about a type from a schema.
 */
public class TypeInfoCollector extends BaseCollector {
    public enum NullabilityResolution {FROM_SCHEMA, FORCE_NULLABLE, FORCE_NOT_NULLABLE}

    private final SchemaResolver schemaResolver;

    public TypeInfoCollector(SchemaResolver schemaResolver, Options opts) {
        super(opts);
        this.schemaResolver = schemaResolver;
    }

    public <T> TypeInfo getTypeInfo(Schema<T> schema) {
        return getTypeInfo(schema, NullabilityResolution.FROM_SCHEMA);
    }

    public TypeInfo getTypeInfo(Schema<?> schema, NullabilityResolution nullabilityResolution) {
        if (isEmpty(schema.getTypes())) {

            boolean nullable = isNullable(schema, nullabilityResolution);

            if (nonNull(schema.getAnyOf())) {
                throw new IllegalStateException("Schema 'anyOf' not supported");
            }

            if (nonNull(schema.getOneOf())) {
                // Limited support for 'oneOf' in properties: use the first non-nullable subschema
                List<Schema<?>> oneOfSchemas = (List<Schema<?>>)(Object)schema.getOneOf();
                Schema<?> subSchema = getNonNullableSubSchema(oneOfSchemas)
                    .orElseThrow(illegalStateException("Schema 'oneOf' must contain a non-nullable sub-schema"));

                return getTypeInfo(subSchema, nullable ? FORCE_NULLABLE : FORCE_NOT_NULLABLE);
            }

            if (nonNull(schema.getAllOf()) && schema.getAllOf().size() == 1) {
                // 'allOf' only supported if it contains single type
                return getTypeInfo(schema.getAllOf().get(0), nullable ? FORCE_NULLABLE : FORCE_NOT_NULLABLE);
            }

            String ref = schema.get$ref();
            if (nonBlank(ref)) {
                TypeInfo typeInfo;

                if (schemaResolver.isPrimitiveType(ref)) {
                    Schema<?> refSchema = schemaResolver.getOrThrow(ref);
                    typeInfo = getTypeInfo(refSchema, nullable ? FORCE_NULLABLE : FORCE_NOT_NULLABLE);
                } else {
                    typeInfo = new TypeInfo()
                        .withName(schemaResolver.getTypeName(ref) + opts.pojoNameSuffix())
                        .withNullable(nullable);

                    String modelSubpackage = schemaResolver.getModelSubpackage(ref).orElse(null);
                    typeInfo = typeInfo.withAddedNormalImport(opts.getModelPackage(modelSubpackage) + "." + typeInfo.name());
                    if (!schemaResolver.isEnumType(schema.get$ref())) {
                        AnnotationInfo validAnnotation = getValidAnnotation();
                        typeInfo = typeInfo.withAddedAnnotation(validAnnotation);
                    }
                    if (!nullable) {
                        AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                        typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
                    }
                }

                if (nonBlank(schema.getDescription())) {
                    typeInfo = typeInfo.withDescription(schema.getDescription());
                }

                return typeInfo;
            } else if (isEmpty(schema.getAllOf())) {
                throw new IllegalStateException("No types, no ref: %s".formatted(schema.toString()));
            }
        }

        return getJsonType(schema, nullabilityResolution);
    }

    public Optional<Schema<?>> getNonNullableSubSchema(List<Schema<?>> subSchemas) {
        return subSchemas.stream()
            .filter(subSchema -> !isNullable(subSchema))
            .findFirst();
    }

    private TypeInfo getJsonType(Schema<?> schema, NullabilityResolution nullabilityResolution) {
        TypeInfo typeInfo = new TypeInfo()
            .withDescription(schema.getDescription())
            .withPrimitive(true)
            .withNullable(isNullable(schema, nullabilityResolution));

        String jsonType = streamSafely(schema.getTypes())
            .filter(t -> !"null".equals(t))
            .findFirst()
            .orElseThrow(illegalStateException("Unexpected types: %s", schema.toString()));

        if ("string".equals(jsonType)) {
            typeInfo = populateJsonStringType(typeInfo, schema);
        } else if ("number".equals(jsonType)) {
            typeInfo = populateJsonNumberType(typeInfo, schema);
        } else if ("integer".equals(jsonType)) {
            typeInfo = populateJsonIntegerType(typeInfo, schema);
        } else if ("boolean".equals(jsonType)) {
            typeInfo = populateJsonBooleanType(typeInfo);
        } else if ("array".equals(jsonType)) {
            typeInfo = populateJsonArrayType(typeInfo, schema);
        } else if ("object".equals(jsonType) && schema.getAdditionalProperties() instanceof Schema) {
            typeInfo = populateJsonMapType(typeInfo, schema);
        } else {
            // Schema not expected to be defined "inline" using type 'object'
            throw new IllegalStateException("Unexpected schema: %s".formatted(schema.toString()));
        }

        Optional<String> maybeJsonSerializer = extensions(schema.getExtensions()).getString(EXT_JSON_SERIALIZER);
        if (maybeJsonSerializer.isPresent()) {
            AnnotationInfo jsonSerializeAnnotation = getJsonSerializeAnnotation(maybeJsonSerializer.get());
            typeInfo = typeInfo.withAddedAnnotation(jsonSerializeAnnotation);
        }

        Optional<String> maybeValidationConstraint = extensions(schema.getExtensions()).getString(EXT_VALIDATION_CONSTRAINT);
        if (maybeValidationConstraint.isPresent()) {
            AnnotationInfo validationConstraintAnnotation = new AnnotationInfo(
                "@%s".formatted(getClassNameFromFqn(maybeValidationConstraint.get())),
                maybeValidationConstraint.get()
            );
            typeInfo = typeInfo.withAddedAnnotation(validationConstraintAnnotation);
        }

        return typeInfo;
    }

    private TypeInfo populateJsonStringType(TypeInfo typeInfo, Schema<?> schema) {
        if ("uri".equals(schema.getFormat())) {
            typeInfo = typeInfo.withName("URI")
                .withSchemaFormat(schema.getFormat())
                .withAddedNormalImport("java.net.URI");
            if (!typeInfo.nullable()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
        } else if ("uuid".equals(schema.getFormat())) {
            typeInfo = typeInfo.withName("UUID")
                .withSchemaFormat(schema.getFormat())
                .withAddedNormalImport("java.util.UUID");
            if (!typeInfo.nullable()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
        } else if ("duration".equals(schema.getFormat())) {
            typeInfo = typeInfo.withName("Duration")
                .withSchemaFormat(schema.getFormat())
                .withAddedNormalImport("java.time.Duration");
            if (!typeInfo.nullable() && opts.addJakartaBeanValidationAnnotations()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
        } else if ("date".equals(schema.getFormat())) {
            typeInfo = typeInfo.withName("LocalDate")
                .withSchemaFormat(schema.getFormat())
                .withAddedNormalImport("java.time.LocalDate");
            if (!typeInfo.nullable() && opts.addJakartaBeanValidationAnnotations()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
            AnnotationInfo jsonFormatAnnotation = getJsonFormatAnnotation("yyyy-MM-dd");
            typeInfo = typeInfo.withAddedAnnotation(jsonFormatAnnotation);
        } else if ("date-time".equals(schema.getFormat())) {
            typeInfo = typeInfo.withName("LocalDateTime")
                .withSchemaFormat(schema.getFormat())
                .withAddedNormalImport("java.time.LocalDateTime");
            if (!typeInfo.nullable() && opts.addJakartaBeanValidationAnnotations()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
            AnnotationInfo jsonFormatAnnotation = getJsonFormatAnnotation("yyyy-MM-dd'T'HH:mm:ss");
            typeInfo = typeInfo.withAddedAnnotation(jsonFormatAnnotation);
        } else if ("email".equals(schema.getFormat())) {
            typeInfo = typeInfo.withName("String")
                .withSchemaFormat(schema.getFormat());
            if (opts.addJakartaBeanValidationAnnotations()) {
                if (!typeInfo.nullable()) {
                    AnnotationInfo notBlankAnnotation = getNotBlankAnnotation();
                    typeInfo = typeInfo.withAddedAnnotation(notBlankAnnotation);
                }
                AnnotationInfo emailAnnotation = getEmailAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(emailAnnotation);
            }
        } else if ("binary".equals(schema.getFormat())) {
            typeInfo = typeInfo.withName("byte[]")
                .withSchemaFormat(schema.getFormat());
            if (opts.addJakartaBeanValidationAnnotations()) {
                if (!typeInfo.nullable()) {
                    AnnotationInfo notEmptyAnnotation = getNotEmptyAnnotation();
                    typeInfo = typeInfo.withAddedAnnotation(notEmptyAnnotation);
                }
                if (nonNull(schema.getMinItems()) || nonNull(schema.getMaxItems())) {
                    AnnotationInfo sizeAnnotaion = getArraySizeAnnotation(schema);
                    typeInfo = typeInfo.withAddedAnnotation(sizeAnnotaion);
                }
            }
        } else {
            typeInfo = typeInfo.withName("String")
                .withSchemaFormat(schema.getFormat());
            if (opts.addJakartaBeanValidationAnnotations()) {
                if (!typeInfo.nullable()) {
                    AnnotationInfo notBlankAnnotation = getNotBlankAnnotation();
                    typeInfo = typeInfo.withAddedAnnotation(notBlankAnnotation);
                }
                if (nonBlank(schema.getPattern())) {
                    typeInfo = typeInfo.withSchemaPattern(schema.getPattern())
                        .withAddedAnnotation(getPatternAnnotation(schema));
                }
                if (nonNull(schema.getMinLength()) || nonNull(schema.getMaxLength())) {
                    AnnotationInfo sizeAnnotation = getStringSizeAnnotation(schema);
                    typeInfo = typeInfo.withAddedAnnotation(sizeAnnotation);
                }
            }
        }

        return typeInfo;
    }

    private TypeInfo populateJsonNumberType(TypeInfo typeInfo, Schema<?> schema) {
        if ("double".equals(schema.getFormat())) {
            typeInfo = typeInfo.withName("Double");
        } else if ("float".equals(schema.getFormat())) {
            typeInfo = typeInfo.withName("Float");
        } else {
            typeInfo = typeInfo.withName("BigDecimal")
                .withAddedNormalImport("java.math.BigDecimal");
        }
        typeInfo = typeInfo.withSchemaFormat(schema.getFormat());
        if (opts.addJakartaBeanValidationAnnotations()) {
            if (!typeInfo.nullable()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
            if ("BigDecimal".equals(typeInfo.name())) {
                if (nonNull(schema.getMinimum())) {
                    AnnotationInfo minAnnotation = getMinAnnotation(schema);
                    typeInfo = typeInfo.withAddedAnnotation(minAnnotation);
                }
                if (nonNull(schema.getMaximum())) {
                    AnnotationInfo maxAnnotation = getMaxAnnotation(schema);
                    typeInfo = typeInfo.withAddedAnnotation(maxAnnotation);
                }
            }
        }

        return typeInfo;
    }

    private TypeInfo populateJsonIntegerType(TypeInfo typeInfo, Schema<?> schema) {
        typeInfo = typeInfo.withName("int64".equals(schema.getFormat()) ? "Long" :"Integer")
            .withSchemaFormat(schema.getFormat());

        if (opts.addJakartaBeanValidationAnnotations()) {
            if (!typeInfo.nullable()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
            if (nonNull(schema.getMinimum())) {
                AnnotationInfo minAnnotation = getMinAnnotation(schema);
                typeInfo = typeInfo.withAddedAnnotation(minAnnotation);
            }
            if (nonNull(schema.getMaximum())) {
                AnnotationInfo maxAnnotation = getMaxAnnotation(schema);
                typeInfo = typeInfo.withAddedAnnotation(maxAnnotation);
            }
        }

        return typeInfo;
    }

    private TypeInfo populateJsonBooleanType(TypeInfo typeInfo) {
        typeInfo = typeInfo.withName("Boolean");
        if (!typeInfo.nullable() && opts.addJakartaBeanValidationAnnotations()) {
            AnnotationInfo notNullAnnotation = getNotNullAnnotation();
            typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
        }

        return typeInfo;
    }

    private TypeInfo populateJsonArrayType(TypeInfo typeInfo, Schema<?> schema) {
        typeInfo = typeInfo.withPrimitive(false);
        if (TRUE.equals(schema.getUniqueItems())) {
            typeInfo = typeInfo.withName("Set")
                .withAddedNormalImport("java.util.Set");
        } else {
            typeInfo = typeInfo.withName("List")
                .withAddedNormalImport("java.util.List");
        }

        if (opts.addJakartaBeanValidationAnnotations()) {
            AnnotationInfo validAnnotation = getValidAnnotation();
            typeInfo = typeInfo.withAddedAnnotation(validAnnotation);
        }

        TypeInfo itemType = getTypeInfo(schema.getItems());

        if (opts.addJakartaBeanValidationAnnotations()) {
            if (!typeInfo.nullable()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
            if (nonNull(schema.getMinItems()) || nonNull(schema.getMaxItems())) {
                AnnotationInfo sizeAnnotation = getArraySizeAnnotation(schema);
                typeInfo = typeInfo.withAddedAnnotation(sizeAnnotation);
            }
        }

        return typeInfo.withItemType(itemType);
    }

    private TypeInfo populateJsonMapType(TypeInfo typeInfo, Schema<?> schema) {
        typeInfo = typeInfo.withName("Map")
            .withAddedNormalImport("java.util.Map");

        if (opts.addJakartaBeanValidationAnnotations()) {
            AnnotationInfo validAnnotation = getValidAnnotation();
            typeInfo = typeInfo.withAddedAnnotation(validAnnotation);
        }

        typeInfo = typeInfo.withKeyType(getTypeInfo(new StringSchema()))
            .withItemType(getTypeInfo((Schema<?>)schema.getAdditionalProperties()));

        if (opts.addJakartaBeanValidationAnnotations()) {
            if (!typeInfo.nullable()) {
                AnnotationInfo notNullAnnotation = getNotNullAnnotation();
                typeInfo = typeInfo.withAddedAnnotation(notNullAnnotation);
            }
            if (nonNull(schema.getMinItems()) || nonNull(schema.getMaxItems())) {
                AnnotationInfo sizeAnnotation = getArraySizeAnnotation(schema);
                typeInfo = typeInfo.withAddedAnnotation(sizeAnnotation);
            }
        }

        return typeInfo;
    }

    private boolean isNullable(Schema<?> schema, NullabilityResolution resolution) {
        return switch(resolution) {
            case FROM_SCHEMA -> isNullable(schema);
            case FORCE_NULLABLE -> true;
            case FORCE_NOT_NULLABLE -> false;
        };
    }

    public boolean isNullable(Schema<?> schema) {
        if (isEmpty(schema.getTypes())) {
            if (nonEmpty(schema.getAllOf())) {
                return schema.getAllOf().stream().allMatch(this::isNullable);
            } else if (nonEmpty(schema.getOneOf())) {
                return schema.getOneOf().stream().anyMatch(this::isNullable);
            } else if (nonBlank(schema.get$ref())) {
                return isNullableByExtension(schema);
            } else {
                throw new IllegalStateException("No types, no ref: %s".formatted(schema.toString()));
            }
        }

        // schema.getNullable() populated by OpenAPI 3.0.x only
        return schema.getTypes().contains("null") || TRUE.equals(schema.getNullable()) || isNullableByExtension(schema);
    }

    private boolean isNullableByExtension(Schema<?> schema) {
        return extensions(schema.getExtensions()).getBoolean(EXT_NULLABLE).orElse(false);
    }

    private AnnotationInfo getJsonSerializeAnnotation(String jsonSerializer) {
        return new AnnotationInfo("@JsonSerialize(using = %s)".formatted(getJsonSerializerClass(jsonSerializer)))
            .withAddedNormalImport("com.fasterxml.jackson.databind.annotation.JsonSerialize")
            .withAddedNormalImport(jsonSerializer);
    }

    private AnnotationInfo getJsonFormatAnnotation(String pattern) {
        return new AnnotationInfo(
            "@JsonFormat(pattern = \"%s\")".formatted(pattern),
            "com.fasterxml.jackson.annotation.JsonFormat");
    }

    private AnnotationInfo getValidAnnotation() {
        return new AnnotationInfo(
            "@Valid",
            "jakarta.validation.Valid");
    }

    private AnnotationInfo getNotNullAnnotation() {
        return new AnnotationInfo(
            "@NotNull",
            "jakarta.validation.constraints.NotNull");
    }

    private AnnotationInfo getNotBlankAnnotation() {
        return new AnnotationInfo(
            "@NotBlank",
            "jakarta.validation.constraints.NotBlank");
    }

    private AnnotationInfo getNotEmptyAnnotation() {
        return new AnnotationInfo(
            "@NotEmpty",
            "jakarta.validation.constraints.NotEmpty");
    }

    private AnnotationInfo getMinAnnotation(Schema<?> schema) {
        return new AnnotationInfo(
            "@Min(%d)".formatted(schema.getMinimum().longValue()),
            "jakarta.validation.constraints.Min");
    }

    private AnnotationInfo getMaxAnnotation(Schema<?> schema) {
        return new AnnotationInfo(
            "@Max(%d)".formatted(schema.getMaximum().longValue()),
            "jakarta.validation.constraints.Max");
    }

    private AnnotationInfo getPatternAnnotation(Schema<?> schema) {
        return new AnnotationInfo(
            "@Pattern(regexp = \"%s\")".formatted(schema.getPattern()),
            "jakarta.validation.constraints.Pattern");
    }

    private AnnotationInfo getEmailAnnotation() {
        return new AnnotationInfo(
            "@Email",
            "jakarta.validation.constraints.Email");
    }

    private AnnotationInfo getArraySizeAnnotation(Schema<?> schema) {
        List<String> sizeParams = new ArrayList<>();
        if (nonNull(schema.getMinItems())) {
            sizeParams.add("min = %d".formatted(schema.getMinItems()));
        }
        if (nonNull(schema.getMaxItems())) {
            sizeParams.add("max = %d".formatted(schema.getMaxItems()));
        }
        return new AnnotationInfo(
            "@Size(%s)".formatted(joinCsv(sizeParams)),
            "jakarta.validation.constraints.Size");
    }

    private AnnotationInfo getStringSizeAnnotation(Schema<?> schema) {
        List<String> sizeParams = new ArrayList<>();
        if (nonNull(schema.getMinLength())) {
            sizeParams.add("min = %d".formatted(schema.getMinLength()));
        }
        if (nonNull(schema.getMaxLength())) {
            sizeParams.add("max = %d".formatted(schema.getMaxLength()));
        }
        return new AnnotationInfo(
            "@Size(%s)".formatted(joinCsv(sizeParams)),
            "jakarta.validation.constraints.Size");
    }

    private String getJsonSerializerClass(String jsonSerializerFqn) {
        String className = getClassNameFromFqn(jsonSerializerFqn);
        return opts.useKotlinSyntax() ? className+"::class" : className+".class";
    }
}
