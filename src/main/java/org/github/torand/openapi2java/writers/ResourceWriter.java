package org.github.torand.openapi2java.writers;

import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.ResourceInfo;

import java.io.Writer;
import java.util.Set;
import java.util.TreeSet;

import static org.github.torand.openapi2java.utils.StringHelper.nonBlank;

public class ResourceWriter extends BaseWriter {

    public ResourceWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    public void write(ResourceInfo resourceInfo) {
        writeLine("package %s;", opts.rootPackage);
        writeNewLine();

        Set<String> imports = new TreeSet<>();
        imports.addAll(resourceInfo.imports);
        resourceInfo.methods.forEach(m -> imports.addAll(m.imports));
        imports.forEach(i -> writeLine("import %s;".formatted(i)));
        writeNewLine();

        Set<String> staticImports = new TreeSet<>();
        staticImports.addAll(resourceInfo.staticImports);
        resourceInfo.methods.forEach(m -> staticImports.addAll(m.staticImports));
        staticImports.forEach(si -> writeLine("import static %s;".formatted(si)));
        writeNewLine();

        resourceInfo.annotations.forEach(a -> writeLine(a));
        writeLine("public interface %s {".formatted(resourceInfo.name));
        writeNewLine();

        writeIndent(1);
        writeLine("String ROOT_PATH = \"%s\";", opts.rootUrlPath);

        resourceInfo.methods.forEach(m -> {
            writeNewLine();

            m.annotations.forEach(a -> {
                writeIndent(1);
                writeLine(a);
            });

            writeIndent(1);
            writeLine("Response %s(".formatted(m.name));
            for (int i=0; i<m.parameters.size(); i++) {
                writeIndent(2);
                write(m.parameters.get(i));
                if (i < (m.parameters.size()-1)) {
                    write(",");
                }
                if (nonBlank(m.parameterComments.get(i))) {
                    write(" // %s", m.parameterComments.get(i));
                }
                writeNewLine();
            };

            writeIndent(1);
            writeLine(");");
        });

        writeLine("}");
    }
}
