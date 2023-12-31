package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.github.torand.openapi2java.collectors.ParameterResolver;
import org.github.torand.openapi2java.collectors.ResponseResolver;
import org.github.torand.openapi2java.collectors.SchemaResolver;
import org.github.torand.openapi2java.utils.StringHelper;
import org.github.torand.openapi2java.writers.ResourceWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.github.torand.openapi2java.utils.StringHelper.pluralSuffix;

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

        AtomicInteger clientCount = new AtomicInteger(0);

        openApiDoc.getTags().forEach(tag -> {
            if (opts.includeTags.isEmpty() || opts.includeTags.contains(tag.getName())) {
                clientCount.incrementAndGet();
                String resourceName = getResourceClassName(tag);
                if (opts.verbose) {
                    System.out.println("Generating REST client for tag \"%s\": %s".formatted(tag.getName(), resourceName + opts.resourceNameSuffix));
                }
                String resourceFileName = resourceName + opts.resourceNameSuffix + ".java";
                File resourceFile = new File(f, resourceFileName);
                try (Writer writer = new FileWriter(resourceFile)) {
                    ResourceWriter resourceWriter = new ResourceWriter(writer, schemaResolver, parameterResolver, responseResolver, opts);
                    resourceWriter.writeResource(resourceName, tag.getDescription(), openApiDoc.getPaths(), tag.getName());
                } catch (IOException e) {
                    System.out.println("Failed to write file %s: %s".formatted(resourceFileName, e.toString()));
                }
            }
        });

        System.out.println("Generated %d REST client%s in directory %s".formatted(clientCount.get(), pluralSuffix(clientCount.get()), outputPath));
    }

    private String getResourceClassName(Tag tag) {
        String tagName = tag.getName().trim();
        String[] tagSubNames = tagName.split(" ");
        return Stream.of(tagSubNames).map(StringHelper::capitalize).collect(joining());
    }
}
