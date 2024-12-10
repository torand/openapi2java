/*
 * Copyright (c) 2024 Tore Eide Andersen
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

import io.github.torand.openapi2java.Options;
import io.github.torand.openapi2java.model.EnumInfo;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.torand.openapi2java.utils.StringHelper.joinCsv;
import static java.lang.Boolean.TRUE;
import static io.github.torand.openapi2java.collectors.Extensions.EXT_MODEL_SUBDIR;
import static io.github.torand.openapi2java.collectors.Extensions.extensions;

public class EnumInfoCollector extends BaseCollector {

    public EnumInfoCollector(Options opts) {
        super(opts);
    }

    public EnumInfo getEnumInfo(String name, Schema<?> schema) {
        EnumInfo enumInfo = new EnumInfo();
        enumInfo.name = name;

        Optional<String> maybeModelSubdir = extensions(schema.getExtensions()).getString(EXT_MODEL_SUBDIR);
        enumInfo.modelSubdir = maybeModelSubdir.orElse(null);
        enumInfo.modelSubpackage = maybeModelSubdir.map(this::dirPath2PackagePath).orElse(null);

        if (opts.addMpOpenApiAnnotations) {
            enumInfo.annotations.add(getSchemaAnnotation(name, schema, enumInfo.imports));
        }

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
        schemaParams.add("name = \"%s\"".formatted(modelName2SchemaName(name)));
        schemaParams.add("description = \"%s\"".formatted(normalizeDescription(description)));
        if (TRUE.equals(pojo.getDeprecated())) {
            schemaParams.add("deprecated = true");
        }
        return "@Schema(%s)".formatted(joinCsv(schemaParams));
    }
}
