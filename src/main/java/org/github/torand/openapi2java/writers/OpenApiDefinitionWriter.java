package org.github.torand.openapi2java.writers;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.collectors.SecuritySchemeInfoCollector;
import org.github.torand.openapi2java.collectors.SecuritySchemeResolver;
import org.github.torand.openapi2java.model.SecuritySchemeInfo;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class OpenApiDefinitionWriter extends BaseWriter {
    private final SecuritySchemeInfoCollector securitySchemeInfoCollector;

    public OpenApiDefinitionWriter(Writer writer, SecuritySchemeResolver securitySchemeResolver, Options opts) {
        super(writer, opts);
        this.securitySchemeInfoCollector = new SecuritySchemeInfoCollector(securitySchemeResolver, opts);
    }

    public void writeOpenApiDefinition(String name, List<SecurityRequirement> securityRequirements) {
        writeLine("package %s;", opts.rootPackage);
        writeNewLine();

        List<SecuritySchemeInfo> securitySchemes = new ArrayList<>();
        securityRequirements.forEach(sr -> {
            sr.keySet().forEach(schemeName -> {
                securitySchemes.add(securitySchemeInfoCollector.getSecuritySchemeInfo(schemeName));
            });
        });

        Set<String> imports = new TreeSet<>();
        imports.add("jakarta.ws.rs.core.Application");
        imports.add("org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes");
        securitySchemes.forEach(ss -> imports.addAll(ss.imports));

        imports.forEach(i -> writeLine("import %s;".formatted(i)));
        writeNewLine();

        writeLine("@SecuritySchemes({");
        AtomicInteger schemeNo = new AtomicInteger(0);
        securitySchemes.forEach(ss -> {
            writeIndent(1);
            write(ss.annotation);
            if (schemeNo.incrementAndGet() < securitySchemes.size()) {
                writeLine(",");
            } else {
                writeNewLine();
            }
        });

        writeLine("})");
        writeLine("public class %s extends Application {", name);
        writeLine("}");
    }
}
