package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.media.JsonSchema;
import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.PropertyInfo;
import org.github.torand.openapi2java.model.TypeInfo;

import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;

public class PropertyInfoCollector {
    private final TypeInfoCollector typeInfoCollector;

    public PropertyInfoCollector(SchemaResolver schemaResolver, Options opts) {
        this.typeInfoCollector = new TypeInfoCollector(schemaResolver, opts);
    }

    public PropertyInfo getPropertyInfo(String name, JsonSchema property) {
        PropertyInfo propInfo = new PropertyInfo();
        propInfo.name = name;
        propInfo.type = typeInfoCollector.getTypeInfo(property);

        String schemaAnnotation = getSchemaAnnotation(property, propInfo.type, propInfo.imports);
        propInfo.annotations.add(schemaAnnotation);

        if (TRUE.equals(property.getDeprecated())) {
            propInfo.annotations.add("@Deprecated");
        }

        return propInfo;
    }

    private String getSchemaAnnotation(JsonSchema property, TypeInfo typeInfo, Set<String> imports) {
        String description = property.getDescription();
        boolean required = !typeInfoCollector.isNullable(property);

        imports.add("org.eclipse.microprofile.openapi.annotations.media.Schema");
        StringBuilder schemaParams = new StringBuilder("description=\"%s\"".formatted(nonNull(description) ? description.replaceAll("%", "%%") : "TBD"));
        if (required) {
            schemaParams.append(", required = true");
        }
        if (nonNull(property.getDefault())) {
            schemaParams.append(", defaultValue = \"%s\"".formatted(property.getDefault().toString()));
        }
        if (nonNull(typeInfo.schemaFormat)) {
            schemaParams.append(", format = \"%s\"".formatted(typeInfo.schemaFormat));
        }
        if (nonNull(typeInfo.schemaPattern)) {
            schemaParams.append(", pattern = \"%s\"".formatted(typeInfo.schemaPattern));
        }
        if (TRUE.equals(property.getDeprecated())) {
            schemaParams.append(", deprecated = true");
        }
        return "@Schema(%s)".formatted(schemaParams);
    }
}
