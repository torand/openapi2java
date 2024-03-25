package org.github.torand.openapi2java.writers;

import io.swagger.v3.oas.models.media.Schema;
import org.github.torand.openapi2java.Options;

import java.io.Writer;

import static org.github.torand.openapi2java.utils.StringHelper.normalizeDescription;

public class EnumWriter extends BaseWriter {

    public EnumWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    public void write(String name, Schema schema) {
        writeLine("package %s;", opts.getModelPackage());
        writeNewLine();
        writeLine("import org.eclipse.microprofile.openapi.annotations.media.Schema;");
        writeNewLine();
        writeLine("@Schema(name = \"%s\", description=\"%s\")".formatted(name, normalizeDescription(schema.getDescription())));
        writeLine("public enum %s {".formatted(name));
        write("    ");
        writeLine(String.join(", ", schema.getEnum()));
        writeLine("}");
    }
}
