package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import org.github.torand.openapi2java.collectors.ComponentResolver;
import org.github.torand.openapi2java.collectors.OpenApiDefInfoCollector;
import org.github.torand.openapi2java.model.OpenApiDefInfo;
import org.github.torand.openapi2java.writers.OpenApiDefWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public class OpenApiDefGenerator {

    private final Options opts;

    public OpenApiDefGenerator(Options opts) {
        this.opts = opts;
    }

    public void generate(OpenAPI openApiDoc) {
        Path outputPath = Path.of(opts.outputDir);
        File f = outputPath.toFile();
        if (!f.exists()) {
            f.mkdirs();
        }

        ComponentResolver componentResolver = new ComponentResolver(openApiDoc);
        OpenApiDefInfoCollector openApiDefInfoCollector = new OpenApiDefInfoCollector(componentResolver, opts);

        String openApiDefClassName = "OpenApiDefinition";
        if (opts.verbose) {
            System.out.println("Generating Open-API definition class: %s".formatted(openApiDefClassName));
        }

        OpenApiDefInfo openApiDefInfo = openApiDefInfoCollector.getOpenApiDefInfo(openApiDefClassName, openApiDoc.getSecurity());

        String openApiDefFileName = openApiDefClassName + ".java";
        File openApiDefFile = new File(f, openApiDefFileName);
        try (Writer writer = new FileWriter(openApiDefFile)) {
            OpenApiDefWriter openApiDefWriter = new OpenApiDefWriter(writer, opts);
            openApiDefWriter.write(openApiDefInfo);
        } catch (IOException e) {
            System.out.println("Failed to write file %s: %s".formatted(openApiDefFileName, e.toString()));
        }

        System.out.println("Generated Open-API definition class in directory %s".formatted(outputPath));
    }
}
