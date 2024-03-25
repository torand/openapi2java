package org.github.torand.openapi2java.writers;

import io.swagger.v3.oas.models.media.Schema;
import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.collectors.EnumInfoCollector;
import org.github.torand.openapi2java.model.EnumInfo;

import java.io.Writer;

import static org.github.torand.openapi2java.utils.CollectionHelper.nonEmpty;

public class EnumWriter extends BaseWriter {
    private final EnumInfoCollector enumInfoCollector;

    public EnumWriter(Writer writer, Options opts) {
        super(writer, opts);
        this.enumInfoCollector = new EnumInfoCollector(opts);
    }

    public void write(String name, Schema<?> schema) {
        EnumInfo enumInfo = enumInfoCollector.getEnumInfo(name, schema);

        writeLine("package %s;", opts.getModelPackage());
        writeNewLine();

        if (nonEmpty(enumInfo.imports)) {
            enumInfo.imports.forEach(ti -> writeLine("import %s;".formatted(ti)));
            writeNewLine();
        }

        enumInfo.annotations.forEach(a -> writeLine(a));

        writeLine("public enum %s {".formatted(name));
        writeIndent(1);
        writeLine(String.join(", ", enumInfo.constants));
        writeLine("}");
    }
}
