package io.github.torand.openapi2java.writers.java;

import io.github.torand.openapi2java.generators.Options;
import io.github.torand.openapi2java.model.AnnotationInfo;
import io.github.torand.openapi2java.model.EnumInfo;
import io.github.torand.openapi2java.writers.BaseWriter;
import io.github.torand.openapi2java.writers.EnumWriter;

import java.io.Writer;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.collection.CollectionHelper.streamSafely;
import static io.github.torand.openapi2java.utils.StringUtils.joinCsv;

/**
 * Writes Java code for an enum.
 */
public class JavaEnumWriter extends BaseWriter implements EnumWriter {

    public JavaEnumWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(EnumInfo enumInfo) {
        writeLine("package %s;", opts.getModelPackage(enumInfo.modelSubpackage()));
        writeNewLine();

        if (nonEmpty(enumInfo.imports().normalImports())) {
            enumInfo.imports().normalImports().forEach(i -> writeLine("import %s;".formatted(i)));
            writeNewLine();
        }

        streamSafely(enumInfo.annotations()).map(AnnotationInfo::annotation).forEach(this::writeLine);

        writeLine("public enum %s {".formatted(enumInfo.name()));
        writeIndent(1);
        writeLine(joinCsv(enumInfo.constants()));
        writeLine("}");
    }
}
