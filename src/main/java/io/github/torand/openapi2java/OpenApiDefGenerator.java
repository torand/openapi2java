package io.github.torand.openapi2java;

import io.github.torand.openapi2java.collectors.ComponentResolver;
import io.github.torand.openapi2java.collectors.OpenApiDefInfoCollector;
import io.github.torand.openapi2java.model.OpenApiDefInfo;
import io.github.torand.openapi2java.writers.OpenApiDefWriter;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.github.torand.openapi2java.writers.WriterFactory.createOpenApiDefWriter;

public class OpenApiDefGenerator {
    private static final Logger logger = LoggerFactory.getLogger(OpenApiDefGenerator.class);
    private final Options opts;

    public OpenApiDefGenerator(Options opts) {
        this.opts = opts;
    }

    public void generate(OpenAPI openApiDoc) {
        ComponentResolver componentResolver = new ComponentResolver(openApiDoc);
        OpenApiDefInfoCollector openApiDefInfoCollector = new OpenApiDefInfoCollector(componentResolver, opts);

        String openApiDefClassName = "OpenApiDefinition";
        if (opts.verbose) {
            logger.info("Generating Open-API definition class: {}", openApiDefClassName);
        }

        OpenApiDefInfo openApiDefInfo = openApiDefInfoCollector.getOpenApiDefInfo(openApiDefClassName, openApiDoc.getSecurity());

        String openApiDefFilename = openApiDefClassName + opts.getFileExtension();
        try (OpenApiDefWriter openApiDefWriter = createOpenApiDefWriter(openApiDefFilename, opts)) {
            openApiDefWriter.write(openApiDefInfo);
        } catch (IOException e) {
            logger.error("Failed to write file {}", openApiDefFilename, e);
        }

        logger.info("Generated Open-API definition class in directory {}", opts.outputDir);
    }
}
