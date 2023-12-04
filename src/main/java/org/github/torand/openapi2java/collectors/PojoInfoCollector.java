package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.PojoInfo;
import org.github.torand.openapi2java.model.PropertyInfo;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

public class PojoInfoCollector {
    private final PropertyInfoCollector propertyInfoCollector;
    private final SchemaResolver schemaResolver;
    private final Options opts;

    public PojoInfoCollector(SchemaResolver schemaResolver, Options opts) {
        this.propertyInfoCollector = new PropertyInfoCollector(schemaResolver, opts);
        this.schemaResolver = schemaResolver;
        this.opts = opts;
    }

    public PojoInfo getPojoInfo(String name, Schema<?> schema) {
        PojoInfo pojoInfo = new PojoInfo();

        pojoInfo.imports.add("org.eclipse.microprofile.openapi.annotations.media.Schema");
        pojoInfo.annotations.add("@Schema(name = \"%s\", description=\"%s\")".formatted(name, schema.getDescription()));

        pojoInfo.properties = getSchemaProperties(schema);

        return pojoInfo;
    }

    private List<PropertyInfo> getSchemaProperties(Schema<?> schema) {
        List<PropertyInfo> props = new ArrayList<>();

        if (nonNull(schema.getAllOf())) {
            schema.getAllOf().forEach(subSchema -> props.addAll(getSchemaProperties(subSchema)));
        } else if (nonNull(schema.get$ref())) {
            Schema<?> $refSchema = schemaResolver.get(schema.get$ref())
                .orElseThrow(() -> new IllegalStateException("Schema not found: " + schema.get$ref()));
            return getSchemaProperties($refSchema);
        } else {
            schema.getProperties().forEach((String k, Schema v) ->
                props.add(propertyInfoCollector.getPropertyInfo(k, (JsonSchema)v))
            );
        }

        return props;
    }
}
