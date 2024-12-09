package io.github.torand.openapi2java.writers.java;

import io.github.torand.openapi2java.Options;
import io.github.torand.openapi2java.model.EnumInfo;
import io.github.torand.openapi2java.writers.BaseWriter;
import io.github.torand.openapi2java.writers.EnumWriter;

import java.io.Writer;

import static io.github.torand.openapi2java.utils.CollectionHelper.nonEmpty;

public class JavaEnumWriter extends BaseWriter implements EnumWriter {

    public JavaEnumWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(EnumInfo enumInfo) {
        writeLine("package %s;", opts.getModelPackage());
        writeNewLine();

        if (nonEmpty(enumInfo.imports)) {
            enumInfo.imports.forEach(ti -> writeLine("import %s;".formatted(ti)));
            writeNewLine();
        }

        enumInfo.annotations.forEach(this::writeLine);

        writeLine("public enum %s {".formatted(enumInfo.name));
        writeIndent(1);
        writeLine(String.join(", ", enumInfo.constants));
        writeLine("}");
    }
}
