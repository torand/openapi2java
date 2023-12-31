package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.media.JsonSchema;
import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.TypeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

public class TypeInfoCollector {

    private final SchemaResolver schemaResolver;
    private final Options opts;

    public TypeInfoCollector(SchemaResolver schemaResolver, Options opts) {
        this.schemaResolver = schemaResolver;
        this.opts = opts;
    }

    public TypeInfo getTypeInfo(JsonSchema schema) {
        if (isNull(schema.getTypes())) {
            String $ref = schema.get$ref();
            if (nonNull($ref)) {
                TypeInfo typeInfo = new TypeInfo();

                if (schemaResolver.isPrimitiveType($ref)) {
                    JsonSchema $refSchema = schemaResolver.get($ref)
                        .orElseThrow(() -> new IllegalStateException("Schema not found: " + $ref));
                    typeInfo = getTypeInfo($refSchema);
                } else {
                    typeInfo.name = schemaResolver.getTypeName($ref);
                    typeInfo.typeImports.add(opts.getModelPackage() + "." + typeInfo.name);
                    if (!schemaResolver.isEnumType(schema.get$ref())) {
                        typeInfo.annotations.add("@Valid");
                        typeInfo.annotationImports.add("jakarta.validation.Valid");
                    }
                }

                if (nonNull(schema.getDescription())) {
                    typeInfo.description = schema.getDescription();
                }

                if (!isNullable(schema)) {
                    typeInfo.annotations.add("@NotNull");
                    typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
                }

                return typeInfo;
            } else if (isNull(schema.getAllOf())) {
                throw new IllegalStateException("No types, no $ref: %s".formatted(schema.toString()));
            }
        }

        return getJsonType(schema);
    }

    private TypeInfo getJsonType(JsonSchema schema) {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.description = schema.getDescription();

        boolean nullable = isNullable(schema);

        String jsonType = schema.getTypes().stream().filter(t -> !"null".equals(t)).findFirst()
            .orElseThrow(() -> new IllegalStateException("Unexpected types: %s".formatted(schema.toString())));

        if ("string".equals(jsonType)) {
            if ("uuid".equals(schema.getFormat())) {
                typeInfo.name = "UUID";
                typeInfo.schemaFormat = "uuid";
                typeInfo.typeImports.add("java.util.UUID");
                if (!nullable) {
                    typeInfo.annotations.add("@NotNull");
                    typeInfo.annotationImports.add("jakarta.validation.constraints.NotNull");
                }
            } else if ("date".equals(schema.getFormat())) {
                typeInfo.name = "LocalDate";
                typeInfo.schemaFormat = "date";
                typeInfo.typeImports.add("java.time.LocalDate");
                typeInfo.annotations.add("@JsonFormat(pattern = \"yyyy-MM-dd\")");
                typeInfo.annotationImports.add("com.fasterxml.jackson.annotation.JsonFormat");
            } else if ("date-time".equals(schema.getFormat())) {
                typeInfo.name = "LocalDateTime";
                typeInfo.schemaFormat = "date-time";
                typeInfo.typeImports.add("java.time.LocalDateTime");
                typeInfo.annotations.add("@JsonFormat(pattern = \"yyyy-MM-dd'T'HH:mm:ss\")");
                typeInfo.annotationImports.add("com.fasterxml.jackson.annotation.JsonFormat");
            } else if ("email".equals(schema.getFormat())) {
                typeInfo.name = "String";
                typeInfo.schemaFormat = "email";
                typeInfo.annotations.add("@Email");
                typeInfo.annotationImports.add("jakarta.validation.constraints.Email");
            } else if ("binary".equals(schema.getFormat())) {
                typeInfo.name = "byte[]";
                typeInfo.schemaFormat = "binary";

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

                if (nonNull(schema.getPattern())) {
                    typeInfo.schemaPattern = schema.getPattern();
                    typeInfo.annotations.add("@Pattern(\"%s\")".formatted(schema.getPattern()));
                    typeInfo.annotationImports.add("jakarta.validation.constraints.Pattern");
                }

                if (nonNull(schema.getMinLength()) || nonNull(schema.getMaxLength())) {
                    String sizeAnnotation = getStringSizeAnnotation(schema, typeInfo.annotationImports);
                    typeInfo.annotations.add(sizeAnnotation);
                }
            }
        } else if ("number".equals(jsonType)) {
            typeInfo.name = "Double";
            // TODO: float/double format
        } else if ("integer".equals(jsonType)) {
            if ("int64".equals(schema.getFormat())) {
                typeInfo.name = "Long";
            } else {
                typeInfo.name = "Integer";
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

    public boolean isNullable(JsonSchema schema) {
        if (isNull(schema.getTypes())) {
            if (nonNull(schema.get$ref())) {
                return Boolean.TRUE.equals(schema.getNullable());
            } else if (nonNull(schema.getAllOf())) {
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
