package org.github.torand.openapi2java.writers;

import io.swagger.v3.oas.models.media.Schema;
import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.collectors.PojoInfoCollector;
import org.github.torand.openapi2java.collectors.SchemaResolver;
import org.github.torand.openapi2java.model.PojoInfo;
import org.github.torand.openapi2java.model.PropertyInfo;

import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

public class PojoWriter extends BaseWriter {
    private final PojoInfoCollector pojoInfoCollector;

    public PojoWriter(Writer writer, SchemaResolver schemaResolver, Options opts) {
        super(writer, opts);
        this.pojoInfoCollector = new PojoInfoCollector(schemaResolver, opts);
    }

    public void write(String name, Schema<?> schema) {
        PojoInfo pojoInfo = pojoInfoCollector.getPojoInfo(name, schema);

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

        if (!imports.isEmpty()) {
            imports.forEach(ti -> writeLine("import %s;".formatted(ti)));
            writeNewLine();
        }

        pojoInfo.annotations.forEach(a -> writeLine(a));

        writeLine("public class %s {".formatted(name));

        AtomicBoolean firstProp = new AtomicBoolean(true);
        pojoInfo.properties.forEach(propInfo -> {
            if (!firstProp.getAndSet(false)) {
                writeNewLine();
            }

            writePropertyAnnotationLines(propInfo);

            writeIndent(1);
            if (nonNull(propInfo.type.itemType)) {
                String itemTypeWithAnnotations = Stream.concat(propInfo.type.itemType.annotations.stream(), Stream.of(propInfo.type.itemType.name)).collect(joining(" "));
                writeLine("public %s<%s> %s;".formatted(propInfo.type.name, itemTypeWithAnnotations, propInfo.name));
            } else {
                writeLine("public %s %s;".formatted(propInfo.type.name, propInfo.name));
            }
        });

        writeNewLine();
        writeNoArgConstructor(name);
        writeNewLine();
        writeParameterizedConstructor(name, pojoInfo.properties);
        writeLine("}");
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
