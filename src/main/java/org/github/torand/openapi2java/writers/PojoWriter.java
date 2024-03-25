package org.github.torand.openapi2java.writers;

import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.PojoInfo;
import org.github.torand.openapi2java.model.PropertyInfo;

import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static org.github.torand.openapi2java.utils.CollectionHelper.nonEmpty;
import static org.github.torand.openapi2java.utils.CollectionHelper.streamConcat;

public class PojoWriter extends BaseWriter {

    public PojoWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    public void write(PojoInfo pojoInfo) {
        writeLine("package %s;", opts.getModelPackage());
        writeNewLine();

        Set<String> imports = new TreeSet<>();
        imports.addAll(pojoInfo.imports);
        pojoInfo.properties.stream()
            .flatMap(p -> p.type.typeImports())
            .filter(not(this::isModelPackage))
            .forEach(imports::add);
        pojoInfo.properties.stream()
            .flatMap(p -> p.type.annotationImports())
            .filter(not(this::isModelPackage))
            .forEach(imports::add);

        if (nonEmpty(imports)) {
            imports.forEach(ti -> writeLine("import %s;".formatted(ti)));
            writeNewLine();
        }

        pojoInfo.annotations.forEach(this::writeLine);

        if (opts.pojosAsRecords) {
            writeLine("public record %s (".formatted(pojoInfo.name));
        } else {
            writeLine("public class %s {".formatted(pojoInfo.name));
        }

        AtomicInteger propNo = new AtomicInteger(1);
        pojoInfo.properties.forEach(propInfo -> {
            writeNewLine();
            writePropertyAnnotationLines(propInfo);

            writeIndent(1);
            if (nonNull(propInfo.type.itemType)) {
                String itemTypeWithAnnotations = streamConcat(propInfo.type.itemType.annotations, List.of(propInfo.type.itemType.name))
                    .collect(joining(" "));

                if (opts.pojosAsRecords) {
                    write("%s<%s> %s".formatted(propInfo.type.name, itemTypeWithAnnotations, propInfo.name));
                } else {
                    write("public %s<%s> %s".formatted(propInfo.type.name, itemTypeWithAnnotations, propInfo.name));
                }
            } else {
                if (opts.pojosAsRecords) {
                    write("%s %s".formatted(propInfo.type.name, propInfo.name));
                } else {
                    write("public %s %s".formatted(propInfo.type.name, propInfo.name));
                }
            }

            if (opts.pojosAsRecords) {
                if (propNo.getAndIncrement() < pojoInfo.properties.size()) {
                    writeLine(",");
                } else {
                    writeNewLine();
                }
            } else {
                writeLine(";");
            }
        });

        if (opts.pojosAsRecords) {
            writeLine(") {");
            writeNewLine();
            writeLine("}");
        } else {
            writeNewLine();
            writeNoArgConstructor(pojoInfo.name);
            writeNewLine();
            writeParameterizedConstructor(pojoInfo.name, pojoInfo.properties);
            writeLine("}");
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
        writeLine("public %s(%s) {", name, props.stream().map(p -> p.type.getFullName() + " " + p.name).collect(joining(", ")));
        props.forEach(p -> {
            writeIndent(2);
            writeLine("this.%s = %s;", p.name, p.name);
        });
        writeIndent(1);
        writeLine("}");
    }

    private void writePropertyAnnotationLines(PropertyInfo propInfo) {
        propInfo.annotations.forEach(a -> {
            writeIndent(1);
            writeLine(a);
        });
        propInfo.type.annotations.forEach(a -> {
            writeIndent(1);
            writeLine(a);
        });
    }

    private boolean isModelPackage(String qualifiedType) {
        int lastDotPos = qualifiedType.lastIndexOf(".");
        String typePackage = qualifiedType.substring(0, lastDotPos);
        return opts.getModelPackage().equals(typePackage);
    }
}
