package org.github.torand.openapi2java.writers.java;

import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.MethodParamInfo;
import org.github.torand.openapi2java.model.ResourceInfo;
import org.github.torand.openapi2java.writers.BaseWriter;
import org.github.torand.openapi2java.writers.ResourceWriter;

import java.io.Writer;
import java.util.Set;
import java.util.TreeSet;

import static org.github.torand.openapi2java.utils.CollectionHelper.nonEmpty;
import static org.github.torand.openapi2java.utils.StringHelper.nonBlank;

public class JavaResourceWriter extends BaseWriter implements ResourceWriter {

    public JavaResourceWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(ResourceInfo resourceInfo) {
        writeLine("package %s;", opts.rootPackage);
        writeNewLine();

        Set<String> imports = new TreeSet<>();
        imports.addAll(resourceInfo.imports);
        resourceInfo.methods.forEach(m -> {
            imports.addAll(m.imports);
            m.parameters.forEach(p -> {
                imports.addAll(p.imports);
                imports.addAll(p.type.typeImports);
            });
        });
        imports.forEach(i -> writeLine("import %s;".formatted(i)));
        writeNewLine();

        Set<String> staticImports = new TreeSet<>();
        staticImports.addAll(resourceInfo.staticImports);
        resourceInfo.methods.forEach(m -> {
            staticImports.addAll(m.staticImports);
            m.parameters.forEach(p -> staticImports.addAll(p.staticImports));
        });
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
                MethodParamInfo paramInfo = m.parameters.get(i);
                writeIndent(2);
                if (nonEmpty(paramInfo.annotations)) {
                    write(String.join(" ", paramInfo.annotations) + " ");
                }
                write(paramInfo.type.getFullName() + " ");
                write(paramInfo.name);
                if (i < (m.parameters.size()-1)) {
                    write(",");
                }
                if (nonBlank(paramInfo.comment)) {
                    write(" // %s", paramInfo.comment);
                }
                writeNewLine();
            }

            writeIndent(1);
            writeLine(");");
        });

        writeLine("}");
    }
}
