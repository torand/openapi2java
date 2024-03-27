package org.github.torand.openapi2java.writers.kotlin;

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

public class KotlinResourceWriter extends BaseWriter implements ResourceWriter {

    public KotlinResourceWriter(Writer writer, Options opts) {
        super(writer, opts);
    }

    @Override
    public void write(ResourceInfo resourceInfo) {
        writeLine("package %s", opts.rootPackage);
        writeNewLine();

        Set<String> imports = new TreeSet<>();
        imports.addAll(resourceInfo.imports);
        imports.addAll(resourceInfo.staticImports);
        resourceInfo.methods.forEach(m -> {
            imports.addAll(m.imports);
            imports.addAll(m.staticImports);
            m.parameters.forEach(p -> {
                imports.addAll(p.imports);
                imports.addAll(p.staticImports);
                imports.addAll(p.type.typeImports);
            });
        });

        imports.removeIf(i -> i.equals("java.util.List") || i.contains("ROOT_PATH"));
        imports.forEach(i -> writeLine("import %s".formatted(i)));
        writeNewLine();

        writeLine("const val ROOT_PATH: String = \"%s\"", opts.rootUrlPath);
        writeNewLine();

        resourceInfo.annotations.forEach(a -> writeLine(a));
        writeLine("interface %s {".formatted(resourceInfo.name));

        resourceInfo.methods.forEach(m -> {
            writeNewLine();

            m.annotations.forEach(a -> {
                writeIndent(1);
                writeLine(a);
            });

            writeIndent(1);
            writeLine("fun %s(".formatted(m.name));
            for (int i=0; i<m.parameters.size(); i++) {
                MethodParamInfo paramInfo = m.parameters.get(i);
                writeIndent(2);
                if (nonEmpty(paramInfo.annotations)) {
                    write(String.join(" ", paramInfo.annotations) + " ");
                }
                write(paramInfo.name + ": ");
                write(paramInfo.type.getFullName());
                if (paramInfo.nullable) {
                    write("?");
                }
                if (i < (m.parameters.size()-1)) {
                    write(",");
                }
                if (nonBlank(paramInfo.comment)) {
                    write(" // %s", paramInfo.comment);
                }
                writeNewLine();
            }

            writeIndent(1);
            writeLine("): Response");
        });

        writeLine("}");
    }
}
