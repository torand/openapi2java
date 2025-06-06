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
import io.github.torand.openapi2java.model.TypeInfo;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.torand.javacommons.collection.CollectionHelper.isEmpty;
import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.collection.CollectionHelper.streamSafely;
import static io.github.torand.javacommons.lang.Exceptions.illegalStateException;
import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static io.github.torand.openapi2java.collectors.Extensions.EXT_JSON_SERIALIZER;
import static io.github.torand.openapi2java.collectors.Extensions.EXT_NULLABLE;
import static io.github.torand.openapi2java.collectors.Extensions.EXT_VALIDATION_CONSTRAINT;
import static io.github.torand.openapi2java.collectors.Extensions.extensions;
import static io.github.torand.openapi2java.collectors.TypeInfoCollector.NullabilityResolution.FORCE_NOT_NULLABLE;
import static io.github.torand.openapi2java.collectors.TypeInfoCollector.NullabilityResolution.FORCE_NULLABLE;
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
                Schema<?> subSchema = getNonNullableSubSchema(schema.getOneOf())
                    .orElseThrow(illegalStateException("Schema 'oneOf' must contain a non-nullable sub-schema"));

                return getTypeInfo(subSchema, nullable ? FORCE_NULLABLE : FORCE_NOT_NULLABLE);
            }

            if (nonNull(schema.getAllOf()) && schema.getAllOf().size() == 1) {
                // 'allOf' only supported if it contains single type
                return getTypeInfo(schema.getAllOf().get(0), nullable ? FORCE_NULLABLE : FORCE_NOT_NULLABLE);
            }

            String $ref = schema.get$ref();
            if (nonBlank($ref)) {
                TypeInfo typeInfo;

                if (schemaResolver.isPrimitiveType($ref)) {
                    Schema<?> $refSchema = schemaResolver.getOrThrow($ref);
                    typeInfo = getTypeInfo($refSchema, nullable ? FORCE_NULLABLE : FORCE_NOT_NULLABLE);
                } else {
                    typeInfo = new TypeInfo();
                    typeInfo.nullable = nullable;

                    typeInfo.name = schemaResolver.getTypeName($ref) + opts.pojoNameSuffix;
                    String modelSubpackage = schemaResolver.getModelSubpackage($ref).orElse(null);
                    typeInfo.typeImports.add(opts.getModelPackage(modelSubpackage) + "." + typeInfo.name);
                    if (!schemaResolver.isEnumType(schema.get$ref())) {
                        String validAnnotation = getValidAnnotation(typeInfo.annotationImports);
                        typeInfo.annotations.add(validAnnotation);
                    }
                    if (!nullable) {
                        String notNullAnnotation = getNotNullAnnotation(typeInfo.annotationImports);
                        typeInfo.annotations.add(notNullAnnotation);
                    }
                }

                if (nonBlank(schema.getDescription())) {
                    typeInfo.description = schema.getDescription();
                }

                return typeInfo;
            } else if (isEmpty(schema.getAllOf())) {
                throw new IllegalStateException("No types, no $ref: %s".formatted(schema.toString()));
            }
        }

        return getJsonType(schema, nullabilityResolution);
    }

    public Optional<Schema> getNonNullableSubSchema(List<Schema> subSchemas) {
        return subSchemas.stream()
            .filter(subSchema -> !isNullable(subSchema))
            .findFirst();
    }

    private TypeInfo getJsonType(Schema<?> schema, NullabilityResolution nullabilityResolution) {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.description = schema.getDescription();
        typeInfo.primitive = true;

        boolean nullable = isNullable(schema, nullabilityResolution);
        typeInfo.nullable = nullable;

        String jsonType = streamSafely(schema.getTypes())
            .filter(t -> !"null".equals(t))
            .findFirst()
            .orElseThrow(illegalStateException("Unexpected types: %s", schema.toString()));

        if ("string".equals(jsonType)) {
            populateJsonStringType(typeInfo, schema);
        } else if ("number".equals(jsonType)) {
            populateJsonNumberType(typeInfo, schema);
        } else if ("integer".equals(jsonType)) {
            populateJsonIntegerType(typeInfo, schema);
        } else if ("boolean".equals(jsonType)) {
            populateJsonBooleanType(typeInfo);
        } else if ("array".equals(jsonType)) {
            populateJsonArrayType(typeInfo, schema);
        } else if ("object".equals(jsonType) && schema.getAdditionalProperties() instanceof Schema) {
            populateJsonMapType(typeInfo, schema);
        } else {
            // Schema not expected to be defined "inline" using type 'object'
            throw new IllegalStateException("Unexpected schema: %s".formatted(schema.toString()));
        }

        extensions(schema.getExtensions()).getString(EXT_JSON_SERIALIZER)
            .ifPresent(jsonSerializer -> {
                String jsonSerializeAnnotation = getJsonSerializeAnnotation(jsonSerializer, typeInfo.annotationImports);
                typeInfo.annotations.add(jsonSerializeAnnotation);
            });

        extensions(schema.getExtensions()).getString(EXT_VALIDATION_CONSTRAINT)
            .ifPresent(validationConstraint -> {
                typeInfo.annotations.add("@%s".formatted(getClassNameFromFqn(validationConstraint)));
                typeInfo.annotationImports.add(validationConstraint);
            });

        return typeInfo;
    }

    private void populateJsonStringType(TypeInfo typeInfo, Schema<?> schema) {
        if ("uri".equals(schema.getFormat())) {
            typeInfo.name = "URI";
            typeInfo.schemaFormat = schema.getFormat();
            typeInfo.typeImports.add("java.net.URI");
            if (!typeInfo.nullable) {
                String notNullAnnotation = getNotNullAnnotation(typeInfo.annotationImports);
                typeInfo.annotations.add(notNullAnnotation);
            }
        } else if ("uuid".equals(schema.getFormat())) {
            typeInfo.name = "UUID";
            typeInfo.schemaFormat = schema.getFormat();
            typeInfo.typeImports.add("java.util.UUID");
            if (!typeInfo.nullable) {
                String notNullAnnotation = getNotNullAnnotation(typeInfo.annotationImports);
                typeInfo.annotations.add(notNullAnnotation);
            }
        } else if ("duration".equals(schema.getFormat())) {
            typeInfo.name = "Duration";
            typeInfo.schemaFormat = schema.getFormat();
            typeInfo.typeImports.add("java.time.Duration");
            if (!typeInfo.nullable && opts.addJakartaBeanValidationAnnotations) {
                String notNullAnnotation = getNotNullAnnotation(typeInfo.annotationImports);
                typeInfo.annotations.add(notNullAnnotation);
            }
        } else if ("date".equals(schema.getFormat())) {
            typeInfo.name = "LocalDate";
            typeInfo.schemaFormat = schema.getFormat();
            typeInfo.typeImports.add("java.time.LocalDate");
            if (!typeInfo.nullable && opts.addJakartaBeanValidationAnnotations) {
                String notNullAnnotation = getNotNullAnnotation(typeInfo.annotationImports);
                typeInfo.annotations.add(notNullAnnotation);
            }
            String jsonFormatAnnotation = getJsonFormatAnnotation("yyyy-MM-dd", typeInfo.annotationImports);
            typeInfo.annotations.add(jsonFormatAnnotation);
        } else if ("date-time".equals(schema.getFormat())) {
            typeInfo.name = "LocalDateTime";
            typeInfo.schemaFormat = schema.getFormat();
            typeInfo.typeImports.add("java.time.LocalDateTime");
            if (!typeInfo.nullable && opts.addJakartaBeanValidationAnnotations) {
                String notNullAnnotation = getNotNullAnnotation(typeInfo.annotationImports);
                typeInfo.annotations.add(notNullAnnotation);
            }
            String jsonFormatAnnotation = getJsonFormatAnnotation("yyyy-MM-dd'T'HH:mm:ss", typeInfo.annotationImports);
            typeInfo.annotations.add(jsonFormatAnnotation);
        } else if ("email".equals(schema.getFormat())) {
            typeInfo.name = "String";
            typeInfo.schemaFormat = schema.getFormat();
            if (opts.addJakartaBeanValidationAnnotations) {
                if (!typeInfo.nullable) {
                    String notBlankAnnotation = getNotBlankAnnotation(typeInfo.annotationImports);
                    typeInfo.annotations.add(notBlankAnnotation);
                }
                String emailAnnotation = getEmailAnnotation(typeInfo.annotationImports);
                typeInfo.annotations.add(emailAnnotation);
            }
        } else if ("binary".equals(schema.getFormat())) {
            typeInfo.name = "byte[]";
            typeInfo.schemaFormat = schema.getFormat();
            if (opts.addJakartaBeanValidationAnnotations) {
                if (!typeInfo.nullable) {
                    String notEmptyAnnotation = getNotEmptyAnnotation(typeInfo.annotationImports);
                    typeInfo.annotations.add(notEmptyAnnotation);
                }
                if (nonNull(schema.getMinItems()) || nonNull(schema.getMaxItems())) {
                    String sizeAnnotaion = getArraySizeAnnotation(schema, typeInfo.annotationImports);
                    typeInfo.annotations.add(sizeAnnotaion);
                }
            }
        } else {
            typeInfo.name = "String";
            typeInfo.schemaFormat = schema.getFormat();
            if (opts.addJakartaBeanValidationAnnotations) {
                if (!typeInfo.nullable) {
                    String notBlankAnnotation = getNotBlankAnnotation(typeInfo.annotationImports);
                    typeInfo.annotations.add(notBlankAnnotation);
                }
                if (nonBlank(schema.getPattern())) {
                    typeInfo.schemaPattern = schema.getPattern();
                    String patternAnnotation = getPatternAnnotation(schema, typeInfo.annotationImports);
                    typeInfo.annotations.add(patternAnnotation);
                }
                if (nonNull(schema.getMinLength()) || nonNull(schema.getMaxLength())) {
                    String sizeAnnotation = getStringSizeAnnotation(schema, typeInfo.annotationImports);
                    typeInfo.annotations.add(sizeAnnotation);
                }
            }
        }
    }

    private void populateJsonNumberType(TypeInfo typeInfo, Schema<?> schema) {
        if ("double".equals(schema.getFormat())) {
            typeInfo.name = "Double";
        } else if ("float".equals(schema.getFormat())) {
            typeInfo.name = "Float";
        } else {
            typeInfo.name = "BigDecimal";
            typeInfo.typeImports.add("java.math.BigDecimal");
        }
        typeInfo.schemaFormat = schema.getFormat();
        if (opts.addJakartaBeanValidationAnnotations) {
            if (!typeInfo.nullable) {
                String notNullAnnotation = getNotNullAnnotation(typeInfo.annotationImports);
                typeInfo.annotations.add(notNullAnnotation);
            }
            if ("BigDecimal".equals(typeInfo.name)) {
                if (nonNull(schema.getMinimum())) {
                    String minAnnotation = getMinAnnotation(schema, typeInfo.annotationImports);
                    typeInfo.annotations.add(minAnnotation);
                }
                if (nonNull(schema.getMaximum())) {
                    String maxAnnotation = getMaxAnnotation(schema, typeInfo.annotationImports);
                    typeInfo.annotations.add(maxAnnotation);
                }
            }
        }
    }

    private void populateJsonIntegerType(TypeInfo typeInfo, Schema<?> schema) {
        if ("int64".equals(schema.getFormat())) {
            typeInfo.name = "Long";
        } else {
            typeInfo.name = "Integer";
        }
        typeInfo.schemaFormat = schema.getFormat();
        if (opts.addJakartaBeanValidationAnnotations) {
            if (!typeInfo.nullable) {
                String notNullAnnotation = getNotNullAnnotation(typeInfo.annotationImports);
                typeInfo.annotations.add(notNullAnnotation);
            }
            if (nonNull(schema.getMinimum())) {
                String minAnnotation = getMinAnnotation(schema, typeInfo.annotationImports);
                typeInfo.annotations.add(minAnnotation);
            }
            if (nonNull(schema.getMaximum())) {
                String maxAnnotation = getMaxAnnotation(schema, typeInfo.annotationImports);
                typeInfo.annotations.add(maxAnnotation);
            }
        }
    }

    private void populateJsonBooleanType(TypeInfo typeInfo) {
        typeInfo.name = "Boolean";
        if (!typeInfo.nullable && opts.addJakartaBeanValidationAnnotations) {
            String notNullAnnotation = getNotNullAnnotation(typeInfo.annotationImports);
            typeInfo.annotations.add(notNullAnnotation);
        }
    }

    private void populateJsonArrayType(TypeInfo typeInfo, Schema<?> schema) {
        typeInfo.primitive = false;
        if (TRUE.equals(schema.getUniqueItems())) {
            typeInfo.name = "Set";
            typeInfo.typeImports.add("java.util.Set");
        } else {
            typeInfo.name = "List";
            typeInfo.typeImports.add("java.util.List");
        }

        if (opts.addJakartaBeanValidationAnnotations) {
            String validAnnotation = getValidAnnotation(typeInfo.annotationImports);
            typeInfo.annotations.add(validAnnotation);
        }

        typeInfo.itemType = getTypeInfo(schema.getItems());
        typeInfo.itemType.annotations.clear();
        typeInfo.itemType.annotationImports.clear();

        if (opts.addJakartaBeanValidationAnnotations) {
            String itemNotNullAnnotation = getNotNullAnnotation(typeInfo.itemType.annotationImports);
            typeInfo.itemType.annotations.add(itemNotNullAnnotation);

            if (!typeInfo.nullable) {
                String notNullAnnotation = getNotNullAnnotation(typeInfo.annotationImports);
                typeInfo.annotations.add(notNullAnnotation);
            }
            if (nonNull(schema.getMinItems()) || nonNull(schema.getMaxItems())) {
                String sizeAnnotation = getArraySizeAnnotation(schema, typeInfo.annotationImports);
                typeInfo.annotations.add(sizeAnnotation);
            }
        }
    }

    private void populateJsonMapType(TypeInfo typeInfo, Schema<?> schema) {
        typeInfo.name = "Map";
        typeInfo.typeImports.add("java.util.Map");

        if (opts.addJakartaBeanValidationAnnotations) {
            String validAnnotation = getValidAnnotation(typeInfo.annotationImports);
            typeInfo.annotations.add(validAnnotation);
        }

        typeInfo.keyType = getTypeInfo(new StringSchema());
        typeInfo.itemType = getTypeInfo((Schema<?>)schema.getAdditionalProperties());

        if (opts.addJakartaBeanValidationAnnotations) {
            if (!typeInfo.nullable) {
                String notNullAnnotation = getNotNullAnnotation(typeInfo.annotationImports);
                typeInfo.annotations.add(notNullAnnotation);
            }
            if (nonNull(schema.getMinItems()) || nonNull(schema.getMaxItems())) {
                String sizeAnnotation = getArraySizeAnnotation(schema, typeInfo.annotationImports);
                typeInfo.annotations.add(sizeAnnotation);
            }
        }
    }

    private String getClassNameFromFqn(String fqn) {
        int lastDot = fqn.lastIndexOf(".");
        if (lastDot == -1) {
            throw new IllegalStateException("Unexpected fully qualified class name: %s".formatted(fqn));
        }
        return fqn.substring(lastDot+1);
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
                throw new IllegalStateException("No types, no $ref: %s".formatted(schema.toString()));
            }
        }

        // schema.getNullable() populated by OpenAPI 3.0.x only
        return schema.getTypes().contains("null") || TRUE.equals(schema.getNullable()) || isNullableByExtension(schema);
    }

    private boolean isNullableByExtension(Schema<?> schema) {
        return extensions(schema.getExtensions()).getBoolean(EXT_NULLABLE).orElse(false);
    }

    private String getJsonSerializeAnnotation(String jsonSerializer, List<String> imports) {
        imports.add("com.fasterxml.jackson.databind.annotation.JsonSerialize");
        imports.add(jsonSerializer);
        return "@JsonSerialize(using = %s)".formatted(getJsonSerializerClass(jsonSerializer));
    }

    private String getJsonFormatAnnotation(String pattern, List<String> imports) {
        imports.add("com.fasterxml.jackson.annotation.JsonFormat");
        return "@JsonFormat(pattern = \"%s\")".formatted(pattern);
    }

    private String getValidAnnotation(List<String> imports) {
        imports.add("jakarta.validation.Valid");
        return "@Valid";
    }

    private String getNotNullAnnotation(List<String> imports) {
        imports.add("jakarta.validation.constraints.NotNull");
        return "@NotNull";
    }

    private String getNotBlankAnnotation(List<String> imports) {
        imports.add("jakarta.validation.constraints.NotBlank");
        return "@NotBlank";
    }

    private String getNotEmptyAnnotation(List<String> imports) {
        imports.add("jakarta.validation.constraints.NotEmpty");
        return "@NotEmpty";
    }

    private String getMinAnnotation(Schema<?> schema, List<String> imports) {
        imports.add("jakarta.validation.constraints.Min");
        return "@Min(%d)".formatted(schema.getMinimum().longValue());
    }

    private String getMaxAnnotation(Schema<?> schema, List<String> imports) {
        imports.add("jakarta.validation.constraints.Max");
        return "@Max(%d)".formatted(schema.getMaximum().longValue());
    }

    private String getPatternAnnotation(Schema<?> schema, List<String> imports) {
        imports.add("jakarta.validation.constraints.Pattern");
        return "@Pattern(regexp = \"%s\")".formatted(schema.getPattern());
    }

    private String getEmailAnnotation(List<String> imports) {
        imports.add("jakarta.validation.constraints.Email");
        return "@Email";
    }

    private String getArraySizeAnnotation(Schema<?> schema, List<String> imports) {
        List<String> sizeParams = new ArrayList<>();
        if (nonNull(schema.getMinItems())) {
            sizeParams.add("min = %d".formatted(schema.getMinItems()));
        }
        if (nonNull(schema.getMaxItems())) {
            sizeParams.add("max = %d".formatted(schema.getMaxItems()));
        }
        imports.add("jakarta.validation.constraints.Size");
        return "@Size(%s)".formatted(joinCsv(sizeParams));
    }

    private String getStringSizeAnnotation(Schema<?> schema, List<String> imports) {
        List<String> sizeParams = new ArrayList<>();
        if (nonNull(schema.getMinLength())) {
            sizeParams.add("min = %d".formatted(schema.getMinLength()));
        }
        if (nonNull(schema.getMaxLength())) {
            sizeParams.add("max = %d".formatted(schema.getMaxLength()));
        }
        imports.add("jakarta.validation.constraints.Size");
        return "@Size(%s)".formatted(joinCsv(sizeParams));
    }

    private String getJsonSerializerClass(String jsonSerializerFqn) {
        String className = getClassNameFromFqn(jsonSerializerFqn);
        return opts.useKotlinSyntax ? className+"::class" : className+".class";
    }
}
