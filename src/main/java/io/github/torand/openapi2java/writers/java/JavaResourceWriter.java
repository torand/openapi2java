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
package io.github.torand.openapi2java.writers.java;

import io.github.torand.openapi2java.generators.Options;
import io.github.torand.openapi2java.model.AnnotationInfo;
import io.github.torand.openapi2java.model.MethodParamInfo;
import io.github.torand.openapi2java.model.ResourceInfo;
import io.github.torand.openapi2java.writers.BaseWriter;
import io.github.torand.openapi2java.writers.ResourceWriter;

import java.io.Writer;
import java.util.List;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.collection.CollectionHelper.streamSafely;
import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;

/**
 * Writes Java code for a resource.
 */
public class JavaResourceWriter extends BaseWriter implements ResourceWriter {

    public JavaResourceWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(ResourceInfo resourceInfo) {
        writeLine("package %s;", opts.rootPackage());
        writeNewLine();

        writeNonJavaImports(resourceInfo);
        writeJavaImports(resourceInfo);
        writeStaticImports(resourceInfo);

        resourceInfo.annotations().forEach(a -> writeLine(a.annotation()));
        writeLine("public interface %s {".formatted(resourceInfo.name()));
        writeNewLine();

        writeIndent(1);
        writeLine("String ROOT_PATH = \"%s\";", opts.rootUrlPath());

        resourceInfo.methods().forEach(m -> {
            writeNewLine();
            if (m.isDeprecated()) {
                writeIndent(1);
                writeLine("/// @deprecated %s".formatted(m.deprecationMessage()));
                writeIndent(1);
                writeLine("@Deprecated");
            }

            m.annotations().forEach(a -> {
                writeIndent(1);
                writeLine(a.annotation());
            });

            writeIndent(1);
            if (opts.useResteasyResponse()) {
                writeLine("RestResponse<%s> %s(".formatted(nonNull(m.returnType()) ? m.returnType() : "Void", m.name()));
            } else {
                writeLine("Response %s(".formatted(m.name()));
            }

            for (int i=0; i<m.parameters().size(); i++) {
                MethodParamInfo paramInfo = m.parameters().get(i);
                writeIndent(2);
                if (paramInfo.isDeprecated()) {
                    write("@Deprecated ");
                }

                if (nonEmpty(paramInfo.annotations())) {
                    write(String.join(" ", streamSafely(paramInfo.annotations()).map(AnnotationInfo::annotation).toList()) + " ");
                }
                write(paramInfo.type().getFullName() + " ");
                write(paramInfo.name());
                if (i < (m.parameters().size()-1)) {
                    write(",");
                }
                if (nonBlank(paramInfo.comment())) {
                    write(" // %s", paramInfo.comment());
                }
                writeNewLine();
            }

            writeIndent(1);
            writeLine(");");
        });

        if (nonNull(resourceInfo.authMethod())) {
            writeNewLine();

            resourceInfo.authMethod().annotations().forEach(a -> {
                writeIndent(1);
                writeLine(a.annotation());
            });

            writeIndent(1);
            writeLine("default String %s() {".formatted(resourceInfo.authMethod().name()));
            writeIndent(2);
            writeLine("return \"TODO\";");
            writeIndent(1);
            writeLine("}");
        }

        writeLine("}");
    }

    private void writeJavaImports(ResourceInfo resourceInfo) {
        List<String> imports = resourceInfo.aggregatedNormalImports().stream()
            .filter(this::isJavaPackage)
            .map("import %s;"::formatted)
            .toList();

        if (nonEmpty(imports)) {
            imports.forEach(this::writeLine);
            writeNewLine();
        }
    }

    private void writeNonJavaImports(ResourceInfo resourceInfo) {
        List<String> imports = resourceInfo.aggregatedNormalImports().stream()
            .filter(not(this::isJavaPackage))
            .map("import %s;"::formatted)
            .toList();

        if (nonEmpty(imports)) {
            imports.forEach(this::writeLine);
            writeNewLine();
        }
    }

    private void writeStaticImports(ResourceInfo resourceInfo) {
        List<String> imports = resourceInfo.aggregatedStaticImports().stream()
            .map("import static %s;"::formatted)
            .toList();

        if (nonEmpty(imports)) {
            imports.forEach(this::writeLine);
            writeNewLine();
        }
    }

    private boolean isJavaPackage(String qualifiedType) {
        return qualifiedType.startsWith("java.");
    }
}
