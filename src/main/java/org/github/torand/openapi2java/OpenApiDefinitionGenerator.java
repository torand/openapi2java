package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import org.github.torand.openapi2java.collectors.SchemaResolver;
import org.github.torand.openapi2java.collectors.SecuritySchemeResolver;
import org.github.torand.openapi2java.writers.OpenApiDefinitionWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public class OpenApiDefinitionGenerator {

    public void generate(OpenAPI openApiDoc, Options opts) {
        Path outputPath = Path.of(opts.outputDir);
        File f = outputPath.toFile();
        if (!f.exists()) {
            f.mkdirs();
        }

        SecuritySchemeResolver securitySchemeResolver = new SecuritySchemeResolver(openApiDoc.getComponents().getSecuritySchemes());

        String openApiDefClassName = "OpenApiDefinition";
        String openApiDefFileName = openApiDefClassName + ".java";
        File openApiDefFile = new File(f, openApiDefFileName);
        try (Writer writer = new FileWriter(openApiDefFile)) {
            if (opts.verbose) {
                System.out.println("Generating Open-API definition class: %s".formatted(openApiDefClassName));
            }
            OpenApiDefinitionWriter openApiDefWriter = new OpenApiDefinitionWriter(writer, securitySchemeResolver, opts);
            openApiDefWriter.writeOpenApiDefinition(openApiDefClassName, openApiDoc.getSecurity());
        } catch (IOException e) {
            System.out.println("Failed to write file %s: %s".formatted(openApiDefFileName, e.toString()));
        }

        System.out.println("Generated Open-API definition class in directory %s".formatted(outputPath));
    }
}
