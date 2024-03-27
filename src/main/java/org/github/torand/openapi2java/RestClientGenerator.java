package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.github.torand.openapi2java.collectors.ComponentResolver;
import org.github.torand.openapi2java.collectors.ResourceInfoCollector;
import org.github.torand.openapi2java.model.ResourceInfo;
import org.github.torand.openapi2java.utils.StringHelper;
import org.github.torand.openapi2java.writers.ResourceWriter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.github.torand.openapi2java.utils.CollectionHelper.isEmpty;
import static org.github.torand.openapi2java.utils.StringHelper.pluralSuffix;
import static org.github.torand.openapi2java.writers.WriterFactory.createResourceWriter;

public class RestClientGenerator {

    private final Options opts;

    public RestClientGenerator(Options opts) {
        this.opts = opts;
    }

    public void generate(OpenAPI openApiDoc) {
        ComponentResolver componentResolver = new ComponentResolver(openApiDoc);
        ResourceInfoCollector resourceInfoCollector = new ResourceInfoCollector(componentResolver, opts);

        AtomicInteger clientCount = new AtomicInteger(0);

        openApiDoc.getTags().forEach(tag -> {
            if (isEmpty(opts.includeTags) || opts.includeTags.contains(tag.getName())) {
                clientCount.incrementAndGet();
                String resourceName = getResourceClassName(tag);

                if (opts.verbose) {
                    System.out.printf("Generating REST client for tag \"%s\": %s%n", tag.getName(), resourceName + opts.resourceNameSuffix);
                }

                ResourceInfo resourceInfo = resourceInfoCollector.getResourceInfo(resourceName, openApiDoc.getPaths(), tag.getName(), tag.getDescription());

                String resourceFilename = resourceInfo.name + opts.getFileExtension();
                try (ResourceWriter resourceWriter = createResourceWriter(resourceFilename, opts)) {
                    if (resourceInfo.isEmpty()) {
                        System.out.printf("No paths found for tag \"%s\"%n", tag.getName());
                    } else {
                        resourceWriter.write(resourceInfo);
                    }
                } catch (IOException e) {
                    System.out.printf("Failed to write file %s: %s%n", resourceFilename, e);
                }
            }
        });

        System.out.printf("Generated %d REST client%s in directory %s%n", clientCount.get(), pluralSuffix(clientCount.get()), opts.outputDir);
    }

    private String getResourceClassName(Tag tag) {
        String tagName = tag.getName().trim();
        String[] tagSubNames = tagName.split(" ");
        return Stream.of(tagSubNames).map(StringHelper::capitalize).collect(joining());
    }
}
