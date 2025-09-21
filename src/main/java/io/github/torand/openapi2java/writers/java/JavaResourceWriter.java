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
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.collection.CollectionHelper.streamSafely;
import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static java.util.Objects.nonNull;

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

        Set<String> nonJavaImports = new TreeSet<>();
        Set<String> javaImports = new TreeSet<>();

        Consumer<String> importConsumer = qt -> { if (isJavaPackage(qt)) javaImports.add(qt); else nonJavaImports.add(qt);};

        resourceInfo.imports().normalImports().forEach(importConsumer);
        resourceInfo.annotations().forEach(a -> a.imports().normalImports().forEach(importConsumer));
        resourceInfo.methods().forEach(m -> {
            m.imports().normalImports().forEach(importConsumer);
            m.annotations().forEach(a -> a.imports().normalImports().forEach(importConsumer));
            m.parameters().forEach(p -> {
                p.imports().normalImports().forEach(importConsumer);
                p.annotations().forEach(a -> a.imports().normalImports().forEach(importConsumer));
                p.type().imports().normalImports().forEach(importConsumer);
                p.type().annotations().forEach(a -> a.imports().normalImports().forEach(importConsumer));
                if (nonNull(p.type().keyType())) {
                    p.type().keyType().imports().normalImports().forEach(importConsumer);
                    p.type().keyType().annotations().forEach(a -> a.imports().normalImports().forEach(importConsumer));
                }
                if (nonNull(p.type().itemType())) {
                    p.type().itemType().imports().normalImports().forEach(importConsumer);
                    p.type().itemType().annotations().forEach(a -> a.imports().normalImports().forEach(importConsumer));
                }
            });
        });

        if (nonEmpty(nonJavaImports)) {
            nonJavaImports.forEach(ti -> writeLine("import %s;".formatted(ti)));
            writeNewLine();
        }
        if (nonEmpty(javaImports)) {
            javaImports.forEach(ti -> writeLine("import %s;".formatted(ti)));
            writeNewLine();
        }

        Set<String> staticImports = new TreeSet<>();
        staticImports.addAll(resourceInfo.imports().staticImports());
        resourceInfo.annotations().forEach(a -> staticImports.addAll(a.imports().staticImports()));
        resourceInfo.methods().forEach(m -> {
            staticImports.addAll(m.imports().staticImports());
            m.annotations().forEach(a -> staticImports.addAll(a.imports().staticImports()));
            m.parameters().forEach(p -> {
                staticImports.addAll(p.imports().staticImports());
                p.annotations().forEach(a -> staticImports.addAll(a.imports().staticImports()));
            });
        });

        staticImports.forEach(si -> writeLine("import static %s;".formatted(si)));
        writeNewLine();

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

    private boolean isJavaPackage(String qualifiedType) {
        return qualifiedType.startsWith("java.");
    }
}
