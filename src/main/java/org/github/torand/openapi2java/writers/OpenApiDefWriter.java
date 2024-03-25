package org.github.torand.openapi2java.writers;

import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.OpenApiDefInfo;

import java.io.Writer;

import static org.github.torand.openapi2java.utils.CollectionHelper.nonEmpty;

public class OpenApiDefWriter extends BaseWriter {

    public OpenApiDefWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    public void write(OpenApiDefInfo openApiDefInfo) {
        writeLine("package %s;", opts.rootPackage);
        writeNewLine();

        if (nonEmpty(openApiDefInfo.imports)) {
            openApiDefInfo.imports.forEach(i -> writeLine("import %s;".formatted(i)));
            writeNewLine();
        }

        openApiDefInfo.annotations.forEach(this::writeLine);

        writeLine("public class %s extends Application {", openApiDefInfo.name);
        writeLine("}");
    }
}
