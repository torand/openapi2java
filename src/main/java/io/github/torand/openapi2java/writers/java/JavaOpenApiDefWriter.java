package io.github.torand.openapi2java.writers.java;

import io.github.torand.openapi2java.Options;
import io.github.torand.openapi2java.model.OpenApiDefInfo;
import io.github.torand.openapi2java.writers.BaseWriter;
import io.github.torand.openapi2java.writers.OpenApiDefWriter;

import java.io.Writer;

import static io.github.torand.openapi2java.utils.CollectionHelper.nonEmpty;

public class JavaOpenApiDefWriter extends BaseWriter implements OpenApiDefWriter {

    public JavaOpenApiDefWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
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
