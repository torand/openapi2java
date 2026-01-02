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
package io.github.torand.openapi2java.writers.kotlin;

import io.github.torand.openapi2java.generators.Options;
import io.github.torand.openapi2java.model.OpenApiDefInfo;
import io.github.torand.openapi2java.writers.BaseWriter;
import io.github.torand.openapi2java.writers.OpenApiDefWriter;

import java.io.Writer;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;

/**
 * Writes Kotlin code for an OpenAPI definition.
 */
public class KotlinOpenApiDefWriter extends BaseWriter implements OpenApiDefWriter {

    public KotlinOpenApiDefWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(OpenApiDefInfo openApiDefInfo) {
        writeLine("package %s", opts.rootPackage());
        writeNewLine();

        if (nonEmpty(openApiDefInfo.aggregatedImports())) {
            openApiDefInfo.aggregatedImports().forEach(i -> writeLine("import %s".formatted(i)));
            writeNewLine();
        }

        openApiDefInfo.annotationsAsStrings().forEach(this::writeLine);

        writeLine("class %s : Application()", openApiDefInfo.name());
    }
}
