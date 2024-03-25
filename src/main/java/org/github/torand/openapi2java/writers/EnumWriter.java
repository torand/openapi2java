package org.github.torand.openapi2java.writers;

import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.EnumInfo;

import java.io.Writer;

import static org.github.torand.openapi2java.utils.CollectionHelper.nonEmpty;

public class EnumWriter extends BaseWriter {

    public EnumWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

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
