package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import org.github.torand.openapi2java.collectors.ComponentResolver;
import org.github.torand.openapi2java.writers.OpenApiDefinitionWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public class OpenApiDefinitionGenerator {

    private final Options opts;

    public OpenApiDefinitionGenerator(Options opts) {
        this.opts = opts;
    }

    public void generate(OpenAPI openApiDoc) {
        Path outputPath = Path.of(opts.outputDir);
        File f = outputPath.toFile();
        if (!f.exists()) {
            f.mkdirs();
        }

        ComponentResolver componentResolver = new ComponentResolver(openApiDoc);

        String openApiDefClassName = "OpenApiDefinition";
        String openApiDefFileName = openApiDefClassName + ".java";
        File openApiDefFile = new File(f, openApiDefFileName);
        try (Writer writer = new FileWriter(openApiDefFile)) {
            if (opts.verbose) {
                System.out.println("Generating Open-API definition class: %s".formatted(openApiDefClassName));
            }
            OpenApiDefinitionWriter openApiDefWriter = new OpenApiDefinitionWriter(writer, componentResolver, opts);
            openApiDefWriter.write(openApiDefClassName, openApiDoc.getSecurity());
        } catch (IOException e) {
            System.out.println("Failed to write file %s: %s".formatted(openApiDefFileName, e.toString()));
        }

        System.out.println("Generated Open-API definition class in directory %s".formatted(outputPath));
    }
}
