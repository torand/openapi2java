package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import org.github.torand.openapi2java.collectors.ComponentResolver;
import org.github.torand.openapi2java.collectors.OpenApiDefInfoCollector;
import org.github.torand.openapi2java.model.OpenApiDefInfo;
import org.github.torand.openapi2java.writers.OpenApiDefWriter;

import java.io.IOException;

import static org.github.torand.openapi2java.writers.WriterFactory.createOpenApiDefWriter;

public class OpenApiDefGenerator {

    private final Options opts;

    public OpenApiDefGenerator(Options opts) {
        this.opts = opts;
    }

    public void generate(OpenAPI openApiDoc) {
        ComponentResolver componentResolver = new ComponentResolver(openApiDoc);
        OpenApiDefInfoCollector openApiDefInfoCollector = new OpenApiDefInfoCollector(componentResolver, opts);

        String openApiDefClassName = "OpenApiDefinition";
        if (opts.verbose) {
            System.out.printf("Generating Open-API definition class: %s%n", openApiDefClassName);
        }

        OpenApiDefInfo openApiDefInfo = openApiDefInfoCollector.getOpenApiDefInfo(openApiDefClassName, openApiDoc.getSecurity());

        String openApiDefFilename = openApiDefClassName + opts.getFileExtension();
        try (OpenApiDefWriter openApiDefWriter = createOpenApiDefWriter(openApiDefFilename, opts)) {
            openApiDefWriter.write(openApiDefInfo);
        } catch (IOException e) {
            System.out.printf("Failed to write file %s: %s%n", openApiDefFilename, e);
        }

        System.out.printf("Generated Open-API definition class in directory %s%n", opts.outputDir);
    }
}
