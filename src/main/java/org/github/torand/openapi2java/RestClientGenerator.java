package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.github.torand.openapi2java.collectors.ParameterResolver;
import org.github.torand.openapi2java.collectors.ResponseResolver;
import org.github.torand.openapi2java.collectors.SchemaResolver;
import org.github.torand.openapi2java.writers.ResourceWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class RestClientGenerator {

    public void generate(OpenAPI openApiDoc, Options opts) {
        Path outputPath = Path.of(opts.outputDir);
        File f = outputPath.toFile();
        if (!f.exists()) {
            f.mkdirs();
        }

        SchemaResolver schemaResolver = new SchemaResolver(openApiDoc.getComponents().getSchemas());
        ParameterResolver parameterResolver = new ParameterResolver(openApiDoc.getComponents().getParameters());
        ResponseResolver responseResolver = new ResponseResolver(openApiDoc.getComponents().getResponses());

        openApiDoc.getTags().forEach(tag -> {
            String resourceName = getResourceClassName(tag);
            System.out.println("Generating REST client for tag \"%s\": %s".formatted(tag.getName(), resourceName + opts.resourceNameSuffix));
            String resourceFileName = resourceName + opts.resourceNameSuffix + ".java";
            File resourceFile = new File(f, resourceFileName);
            try (Writer writer = new FileWriter(resourceFile)) {
                ResourceWriter resourceWriter = new ResourceWriter(writer, schemaResolver, parameterResolver, responseResolver, opts);
                resourceWriter.writeResource(resourceName, tag.getDescription(), openApiDoc.getPaths(), tag.getName());
            } catch (IOException e) {
                System.out.println("Failed to write file %s: %s".formatted(resourceFileName, e.toString()));
            }
        });
    }

    private String getResourceClassName(Tag tag) {
        String tagName = tag.getName().trim();
        String[] tagSubNames = tagName.split(" ");
        return Stream.of(tagSubNames).map(this::capitalize).collect(joining());
    }

    private String capitalize(String name) {
        return name.substring(0,1).toUpperCase() + name.substring(1);
    }
}
