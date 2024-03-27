package org.github.torand.openapi2java.writers.kotlin;

import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.PojoInfo;
import org.github.torand.openapi2java.model.PropertyInfo;
import org.github.torand.openapi2java.writers.BaseWriter;
import org.github.torand.openapi2java.writers.PojoWriter;

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
import static org.github.torand.openapi2java.utils.CollectionHelper.streamSafely;

public class KotlinPojoWriter extends BaseWriter implements PojoWriter {

    public KotlinPojoWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(PojoInfo pojoInfo) {
        writeLine("package %s", opts.getModelPackage());
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

        imports.removeIf(i -> i.equals("java.util.List"));

        if (nonEmpty(imports)) {
            imports.forEach(ti -> writeLine("import %s".formatted(ti)));
            writeNewLine();
        }

        pojoInfo.annotations.forEach(this::writeLine);
        writeLine("@JvmRecord");

        writeLine("data class %s (".formatted(pojoInfo.name));

        AtomicInteger propNo = new AtomicInteger(1);
        pojoInfo.properties.forEach(propInfo -> {
            writeNewLine();
            writePropertyAnnotationLines(propInfo);

            writeIndent(1);
            write("val %s: ", propInfo.name);

            if (nonNull(propInfo.type.itemType)) {
                String itemTypeWithAnnotations = streamConcat(propInfo.type.itemType.annotations, List.of(propInfo.type.itemType.name))
                    .collect(joining(" "));

                write("%s<%s>".formatted(propInfo.type.name, itemTypeWithAnnotations));
            } else {
                write("%s".formatted(propInfo.type.name));
            }

            if (propInfo.type.nullable) {
                write("?");
            }

            if (propNo.getAndIncrement() < pojoInfo.properties.size()) {
                writeLine(",");
            } else {
                writeNewLine();
            }
        });

        writeLine(")");
    }

    private void writePropertyAnnotationLines(PropertyInfo propInfo) {
        streamSafely(propInfo.annotations)
            .map(a -> "@field:"+a.substring(1))
            .forEach(a -> {
                writeIndent(1);
                writeLine(a);
            });
        streamSafely(propInfo.type.annotations)
            .map(a -> "@field:"+a.substring(1))
            .forEach(a -> {
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
