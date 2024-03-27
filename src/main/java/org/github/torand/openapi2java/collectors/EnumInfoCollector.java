package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.media.Schema;
import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.EnumInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.Boolean.TRUE;

public class EnumInfoCollector extends BaseCollector {

    public EnumInfoCollector(Options opts) {
        super(opts);
    }

    public EnumInfo getEnumInfo(String name, Schema<?> schema) {
        EnumInfo enumInfo = new EnumInfo();
        enumInfo.name = name;

        enumInfo.annotations.add(getSchemaAnnotation(name, schema, enumInfo.imports));

        if (TRUE.equals(schema.getDeprecated())) {
            enumInfo.annotations.add("@Deprecated");
        }

        enumInfo.constants.addAll((List<String>)schema.getEnum());

        return enumInfo;
    }

    private String getSchemaAnnotation(String name, Schema<?> pojo, Set<String> imports) {
        String description = pojo.getDescription();

        imports.add("org.eclipse.microprofile.openapi.annotations.media.Schema");
        List<String> schemaParams = new ArrayList<>();
        schemaParams.add("name = \"%s\"".formatted(name));
        schemaParams.add("description=\"%s\"".formatted(normalizeDescription(description)));
        if (TRUE.equals(pojo.getDeprecated())) {
            schemaParams.add("deprecated = true");
        }
        return "@Schema(%s)".formatted(String.join(", ", schemaParams));
    }
}
