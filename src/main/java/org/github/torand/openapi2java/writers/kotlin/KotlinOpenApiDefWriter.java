package org.github.torand.openapi2java.writers.kotlin;

import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.OpenApiDefInfo;
import org.github.torand.openapi2java.writers.BaseWriter;
import org.github.torand.openapi2java.writers.OpenApiDefWriter;

import java.io.Writer;

import static org.github.torand.openapi2java.utils.CollectionHelper.nonEmpty;

public class KotlinOpenApiDefWriter extends BaseWriter implements OpenApiDefWriter {

    public KotlinOpenApiDefWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(OpenApiDefInfo openApiDefInfo) {
        writeLine("package %s", opts.rootPackage);
        writeNewLine();

        if (nonEmpty(openApiDefInfo.imports)) {
            openApiDefInfo.imports.forEach(i -> writeLine("import %s".formatted(i)));
            writeNewLine();
        }

        openApiDefInfo.annotations.forEach(this::writeLine);

        writeLine("class %s : Application()", openApiDefInfo.name);
    }
}
