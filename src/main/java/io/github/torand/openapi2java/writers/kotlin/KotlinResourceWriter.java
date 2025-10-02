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
package io.github.torand.openapi2java.writers.kotlin;

import io.github.torand.openapi2java.generators.Options;
import io.github.torand.openapi2java.model.AnnotationInfo;
import io.github.torand.openapi2java.model.MethodParamInfo;
import io.github.torand.openapi2java.model.ResourceInfo;
import io.github.torand.openapi2java.writers.BaseWriter;
import io.github.torand.openapi2java.writers.ResourceWriter;

import java.io.Writer;
import java.util.Set;
import java.util.TreeSet;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.collection.CollectionHelper.streamSafely;
import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static io.github.torand.openapi2java.utils.KotlinTypeMapper.toKotlinNative;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;

/**
 * Writes Kotlin code for a resource.
 */
public class KotlinResourceWriter extends BaseWriter implements ResourceWriter {

    public KotlinResourceWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(ResourceInfo resourceInfo) {
        writeLine("package %s", opts.rootPackage());
        writeNewLine();

        writeImports(resourceInfo);

        resourceInfo.annotations().forEach(a -> writeLine(a.annotation()));
        writeLine("interface %s {".formatted(resourceInfo.name()));

        resourceInfo.methods().forEach(m -> {
            writeNewLine();
            if (m.isDeprecated()) {
                writeIndent(1);
                writeLine("@Deprecated(\"%s\")".formatted(m.deprecationMessage()));
            }

            m.annotations().forEach(a -> {
                writeIndent(1);
                writeLine(a.annotation());
            });

            writeIndent(1);
            writeLine("fun %s(".formatted(m.name()));
            for (int i=0; i<m.parameters().size(); i++) {
                MethodParamInfo paramInfo = m.parameters().get(i);
                writeIndent(2);
                if (nonEmpty(paramInfo.annotations())) {
                    write(String.join(" ", streamSafely(paramInfo.annotations()).map(AnnotationInfo::annotation).toList()) + " ");
                }
                write(paramInfo.name() + ": ");
                write(toKotlinNative(paramInfo.type().getFullName()));
                if (paramInfo.nullable()) {
                    write("? = null");
                }
                if (i < (m.parameters().size()-1)) {
                    write(",");
                }
                if (nonBlank(paramInfo.comment())) {
                    write(" // %s", paramInfo.comment());
                }
                writeNewLine();
            }

            writeIndent(1);
            if (opts.useResteasyResponse()) {
                writeLine("): RestResponse<%s>".formatted(nonNull(m.returnType()) ? m.returnType() : "Unit"));
            } else {
                writeLine("): Response");
            }
        });

        writeNewLine();

        writeIndent(1);
        writeLine("companion object {");

        writeIndent(2);
        writeLine("const val ROOT_PATH: String = \"%s\"", opts.rootUrlPath());

        if (nonNull(resourceInfo.authMethod())) {
            resourceInfo.authMethod().annotations().forEach(a -> {
                writeIndent(2);
                writeLine(a.annotation());
            });

            writeIndent(2);
            writeLine("fun %s() = \"TODO\"".formatted(resourceInfo.authMethod().name()));
        }

        writeIndent(1);
        writeLine("}");

        writeLine("}");
    }

    private void writeImports(ResourceInfo resourceInfo) {
        Set<String> imports = resourceInfo.aggregatedImports().stream()
            .filter(not("java.util.List"::equals))
            .filter(not("java.util.Map"::equals))
            .filter(not(i -> i.contains("ROOT_PATH")))
            .map("import %s"::formatted)
            .collect(toCollection(TreeSet::new));

        imports.add("import %s.%s.Companion.ROOT_PATH".formatted(opts.rootPackage(), resourceInfo.name()));

        imports.forEach(this::writeLine);
        writeNewLine();
    }
}
