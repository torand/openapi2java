package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.media.JsonSchema;
import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.TypeInfo;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.github.torand.openapi2java.utils.CollectionHelper.isEmpty;
import static org.github.torand.openapi2java.utils.CollectionHelper.nonEmpty;
import static org.github.torand.openapi2java.utils.CollectionHelper.streamSafely;
import static org.github.torand.openapi2java.utils.Exceptions.illegalStateException;
import static org.github.torand.openapi2java.utils.StringHelper.nonBlank;

public class TypeInfoCollector extends BaseCollector {
    public enum NullabilityResolution {FROM_SCHEMA, FORCE_NULLABLE, FORCE_NOT_NULLABLE};

    private final SchemaResolver schemaResolver;

    public TypeInfoCollector(SchemaResolver schemaResolver, Options opts) {
        super(opts);
        this.schemaResolver = schemaResolver;
    }

    public TypeInfo getTypeInfo(JsonSchema schema) {
        return getTypeInfo(schema, NullabilityResolution.FROM_SCHEMA);
    }

    public TypeInfo getTypeInfo(JsonSchema schema, NullabilityResolution nullabilityResolution) {
        if (isEmpty(schema.getTypes())) {
            String $ref = schema.get$ref();
            if (nonBlank($ref)) {
                TypeInfo typeInfo = new TypeInfo();
                boolean nullable = isNullable(schema, nullabilityResolution);

                if (schemaResolver.isPrimitiveType($ref)) {
                    JsonSchema $refSchema = schemaResolver.getOrThrow($ref);
                    typeInfo = getTypeInfo($refSchema);
                } else {
                    typeInfo.name = schemaResolver.getTypeName($ref) + opts.pojoNameSuffix;
                    typeInfo.typeImports.add(opts.getModelPackage() + "." + typeInfo.name);
                    if (!schemaResolver.isEnumType(schema.get$ref())) {
                        typeInfo.annotations.add("@Valid");
                        typeInfo.annotationImports.add("jakarta.validation.Valid");
                    }
                    if (!nullable) {
                        typeInfo.annotations.add("@NotNull");
                        typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
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

    private TypeInfo getJsonType(JsonSchema schema, NullabilityResolution nullabilityResolution) {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.description = schema.getDescription();

        boolean nullable = isNullable(schema, nullabilityResolution);
        typeInfo.nullable = nullable;

        String jsonType = streamSafely(schema.getTypes())
            .filter(t -> !"null".equals(t))
            .findFirst()
            .orElseThrow(illegalStateException("Unexpected types: %s", schema.toString()));

        if ("string".equals(jsonType)) {
            if ("uri".equals(schema.getFormat())) {
                typeInfo.name = "URI";
                typeInfo.schemaFormat = schema.getFormat();
                typeInfo.typeImports.add("java.net.URI");
                if (!nullable) {
                    typeInfo.annotations.add("@NotNull");
                    typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
                }
            } else if ("uuid".equals(schema.getFormat())) {
                typeInfo.name = "UUID";
                typeInfo.schemaFormat = schema.getFormat();
                typeInfo.typeImports.add("java.util.UUID");
                if (!nullable) {
                    typeInfo.annotations.add("@NotNull");
                    typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
                }
            } else if ("date".equals(schema.getFormat())) {
                typeInfo.name = "LocalDate";
                typeInfo.schemaFormat = schema.getFormat();
                typeInfo.typeImports.add("java.time.LocalDate");
                if (!nullable) {
                    typeInfo.annotations.add("@NotNull");
                    typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
                }
                typeInfo.annotations.add("@JsonFormat(pattern = \"yyyy-MM-dd\")");
                typeInfo.annotationImports.add("com.fasterxml.jackson.annotation.JsonFormat");
            } else if ("date-time".equals(schema.getFormat())) {
                typeInfo.name = "LocalDateTime";
                typeInfo.schemaFormat = schema.getFormat();
                typeInfo.typeImports.add("java.time.LocalDateTime");
                if (!nullable) {
                    typeInfo.annotations.add("@NotNull");
                    typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
                }
                typeInfo.annotations.add("@JsonFormat(pattern = \"yyyy-MM-dd'T'HH:mm:ss\")");
                typeInfo.annotationImports.add("com.fasterxml.jackson.annotation.JsonFormat");
            } else if ("email".equals(schema.getFormat())) {
                typeInfo.name = "String";
                typeInfo.schemaFormat = schema.getFormat();
                typeInfo.annotations.add("@Email");
                typeInfo.annotationImports.add("jakarta.validation.constraints.Email");
            } else if ("binary".equals(schema.getFormat())) {
                typeInfo.name = "byte[]";
                typeInfo.schemaFormat = schema.getFormat();
                if (!nullable) {
                    typeInfo.annotations.add("@NotEmpty");
                    typeInfo.annotationImports.add("jakarta.validation.constraints.NotEmpty");
                }
                if (nonNull(schema.getMinItems()) || nonNull(schema.getMaxItems())) {
                    // TODO: Use getStringSizeAnnotation ?
                    String sizeAnnotaion = getArraySizeAnnotation(schema, typeInfo.annotationImports);
                    typeInfo.annotations.add(sizeAnnotaion);
                }
            } else {
                typeInfo.name = "String";
                if (!nullable) {
                    typeInfo.annotations.add("@NotBlank");
                    typeInfo.annotationImports.add("jakarta.validation.constraints.NotBlank");
                }

                typeInfo.schemaFormat = schema.getFormat();

                if (nonBlank(schema.getPattern())) {
                    typeInfo.schemaPattern = schema.getPattern();
                    typeInfo.annotations.add("@Pattern(regexp = \"%s\")".formatted(schema.getPattern()));
                    typeInfo.annotationImports.add("jakarta.validation.constraints.Pattern");
                }

                if (nonNull(schema.getMinLength()) || nonNull(schema.getMaxLength())) {
                    String sizeAnnotation = getStringSizeAnnotation(schema, typeInfo.annotationImports);
                    typeInfo.annotations.add(sizeAnnotation);
                }
            }
        } else if ("number".equals(jsonType)) {
            typeInfo.name = "Double";
            if (!nullable) {
                typeInfo.annotations.add("@NotNull");
                typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
            }
            // TODO: float/double format
            typeInfo.schemaFormat = schema.getFormat();
        } else if ("integer".equals(jsonType)) {
            if ("int64".equals(schema.getFormat())) {
                typeInfo.name = "Long";
            } else {
                typeInfo.name = "Integer";
            }
            typeInfo.schemaFormat = schema.getFormat();
            if (!nullable && isNull(schema.getMinimum()) && isNull(schema.getMaximum())) {
                typeInfo.annotations.add("@NotNull");
                typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
            }
            if (nonNull(schema.getMinimum())) {
                typeInfo.annotations.add("@Min(%d)".formatted(schema.getMinimum().longValue()));
                typeInfo.annotationImports.add("jakarta.validation.constraints.Min");
            }
            if (nonNull(schema.getMaximum())) {
                typeInfo.annotations.add("@Max(%d)".formatted(schema.getMaximum().longValue()));
                typeInfo.annotationImports.add("jakarta.validation.constraints.Max");
            }
        } else if ("boolean".equals(jsonType)) {
            typeInfo.name = "Boolean";
            if (!nullable) {
                typeInfo.annotations.add("@NotNull");
                typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
            }
        } else if ("array".equals(jsonType)) {
            typeInfo.name = "List";
            typeInfo.typeImports.add("java.util.List");
            typeInfo.annotations.add("@Valid");
            typeInfo.annotationImports.add("jakarta.validation.Valid");

            typeInfo.itemType = getTypeInfo((JsonSchema)schema.getItems());
            typeInfo.itemType.annotations.clear();
            typeInfo.itemType.annotations.add("@NotNull");
            typeInfo.itemType.annotationImports.clear();
            typeInfo.itemType.annotationImports.add("jakarta.validation.constraints.NotNull");

            if (!nullable) {
                typeInfo.annotations.add("@NotEmpty");
                typeInfo.annotationImports.add("jakarta.validation.constraints.NotEmpty");
            }
            if (nonNull(schema.getMinItems()) || nonNull(schema.getMaxItems())) {
                String sizeAnnotaion = getArraySizeAnnotation(schema, typeInfo.annotationImports);
                typeInfo.annotations.add(sizeAnnotaion);
            }
        } else {
            throw new IllegalStateException("Unexpected schema: %s".formatted(schema.toString()));
        }

        return typeInfo;
    }

    private boolean isNullable(JsonSchema schema, NullabilityResolution resolution) {
        return switch(resolution) {
            case FROM_SCHEMA -> isNullable(schema);
            case FORCE_NULLABLE -> true;
            case FORCE_NOT_NULLABLE -> false;
        };
    }

    public boolean isNullable(JsonSchema schema) {
        if (isEmpty(schema.getTypes())) {
            if (nonBlank(schema.get$ref())) {
                return TRUE.equals(schema.getNullable());
            } else if (nonEmpty(schema.getAllOf())) {
                return schema.getAllOf().stream().allMatch(subSchema -> isNullable((JsonSchema)subSchema));
            } else {
                throw new IllegalStateException("No types, no $ref: %s".formatted(schema.toString()));
            }
        }

        return schema.getTypes().contains("null");
    }

    private String getArraySizeAnnotation(JsonSchema schema, List<String> imports) {
        List<String> sizeParams = new ArrayList<>();
        if (nonNull(schema.getMinItems())) {
            sizeParams.add("min = %d".formatted(schema.getMinItems()));
        }
        if (nonNull(schema.getMaxItems())) {
            sizeParams.add("max = %d".formatted(schema.getMaxItems()));
        }
        imports.add("jakarta.validation.constraints.Size");
        return "@Size(%s)".formatted(String.join(", ", sizeParams));
    }

    private String getStringSizeAnnotation(JsonSchema schema, List<String> imports) {
        List<String> sizeParams = new ArrayList<>();
        if (nonNull(schema.getMinLength())) {
            sizeParams.add("min = %d".formatted(schema.getMinLength()));
        }
        if (nonNull(schema.getMaxLength())) {
            sizeParams.add("max = %d".formatted(schema.getMaxLength()));
        }
        imports.add("jakarta.validation.constraints.Size");
        return "@Size(%s)".formatted(String.join(", ", sizeParams));
    }
}
