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
import io.github.torand.openapi2java.model.PojoInfo;
import io.github.torand.openapi2java.model.PropertyInfo;
import io.github.torand.openapi2java.writers.BaseWriter;
import io.github.torand.openapi2java.writers.PojoWriter;

import java.io.Writer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.collection.CollectionHelper.streamSafely;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

/**
 * Writes Java code for a pojo.
 */
public class JavaPojoWriter extends BaseWriter implements PojoWriter {

    public JavaPojoWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(PojoInfo pojoInfo) {
        writeLine("package %s;", opts.getModelPackage(pojoInfo.modelSubpackage()));
        writeNewLine();

        writeNonJavaImports(pojoInfo);
        writeJavaImports(pojoInfo);

        if (pojoInfo.isDeprecated()) {
            writeLine("/// @deprecated %s".formatted(pojoInfo.deprecationMessage()));
            writeLine("@Deprecated");
        }

        pojoInfo.annotations().forEach(a -> writeLine(a.annotation()));

        if (opts.pojosAsRecords()) {
            writeLine("public record %s (".formatted(pojoInfo.name()));
        } else {
            writeLine("public class %s {".formatted(pojoInfo.name()));
        }

        AtomicInteger propNo = new AtomicInteger(1);
        pojoInfo.properties().forEach(propInfo -> {
            writeNewLine();
            writePropertyAnnotationLines(propInfo);

            writeIndent(1);
            if (nonNull(propInfo.type().itemType())) {
                String itemTypeWithAnnotations = Stream.concat(streamSafely(propInfo.type().itemType().annotations()).map(AnnotationInfo::annotation), Stream.of(propInfo.type().itemType().name()))
                    .collect(joining(" "));

                if (!opts.pojosAsRecords()) {
                    write("public ");
                }

                if (nonNull(propInfo.type().keyType())) {
                    String keyTypeWithAnnotations = Stream.concat(streamSafely(propInfo.type().keyType().annotations()).map(AnnotationInfo::annotation), Stream.of(propInfo.type().keyType().name()))
                        .collect(joining(" "));

                    write("%s<%s, %s> %s".formatted(propInfo.type().name(), keyTypeWithAnnotations, itemTypeWithAnnotations, propInfo.name()));
                } else {
                    write("%s<%s> %s".formatted(propInfo.type().name(), itemTypeWithAnnotations, propInfo.name()));
                }
            } else {
                if (opts.pojosAsRecords()) {
                    write("%s %s".formatted(propInfo.type().name(), propInfo.name()));
                } else {
                    write("public %s %s".formatted(propInfo.type().name(), propInfo.name()));
                }
            }

            if (opts.pojosAsRecords()) {
                if (propNo.getAndIncrement() < pojoInfo.properties().size()) {
                    writeLine(",");
                } else {
                    writeNewLine();
                }
            } else {
                writeLine(";");
            }
        });

        if (opts.pojosAsRecords()) {
            writeLine(") {");
            writeNewLine();
            writeLine("}");
        } else {
            writeNewLine();
            writeNoArgConstructor(pojoInfo.name());
            writeNewLine();
            writeParameterizedConstructor(pojoInfo.name(), pojoInfo.properties());
            writeLine("}");
        }
    }

    private void writeJavaImports(PojoInfo pojoInfo) {
        List<String> imports = pojoInfo.aggregatedNormalImports().stream()
            .filter(this::isJavaPackage)
            .map("import %s;"::formatted)
            .toList();

        if (nonEmpty(imports)) {
            imports.forEach(this::writeLine);
            writeNewLine();
        }
    }

    private void writeNonJavaImports(PojoInfo pojoInfo) {
        List<String> imports = pojoInfo.aggregatedNormalImports().stream()
            .filter(not(this::isJavaPackage))
            .filter(ni -> !isInPackage(ni, pojoInfo.modelSubpackage()))
            .map("import %s;"::formatted)
            .toList();

        if (nonEmpty(imports)) {
            imports.forEach(this::writeLine);
            writeNewLine();
        }
    }

    private void writeNoArgConstructor(String name) {
        writeIndent(1);
        writeLine("public %s() {", name);
        writeIndent(1);
        writeLine("}");
    }

    private void writeParameterizedConstructor(String name, List<PropertyInfo> props) {
        writeIndent(1);
        writeLine("public %s(%s) {", name, props.stream().map(p -> p.type().getFullName() + " " + p.name()).collect(joining(", ")));
        props.forEach(p -> {
            writeIndent(2);
            writeLine("this.%s = %s;", p.name(), p.name());
        });
        writeIndent(1);
        writeLine("}");
    }

    private void writePropertyAnnotationLines(PropertyInfo propInfo) {
        if (propInfo.isDeprecated()) {
            writeIndent(1);
            writeLine("/// @deprecated %s".formatted(propInfo.deprecationMessage()));
            writeIndent(1);
            writeLine("@Deprecated");
        }
        propInfo.annotations().forEach(a -> {
            writeIndent(1);
            writeLine(a.annotation());
        });
        propInfo.type().annotations().forEach(a -> {
            writeIndent(1);
            writeLine(a.annotation());
        });
    }

    private boolean isInPackage(String qualifiedType, String pojoModelSubpackage) {
        // Remove class name from qualifiedType value
        int lastDotIdx = qualifiedType.lastIndexOf(".");
        String typePackage = qualifiedType.substring(0, lastDotIdx);

        return opts.getModelPackage(pojoModelSubpackage).equals(typePackage);
    }

    private boolean isJavaPackage(String qualifiedType) {
        return qualifiedType.startsWith("java.");
    }
}
