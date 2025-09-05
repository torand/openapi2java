/*
 * Copyright (c) 2024-2025 Tore Eide Andersen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.torand.openapi2java.generators;

import io.github.torand.javacommons.lang.StringHelper;
import io.github.torand.openapi2java.collectors.ComponentResolver;
import io.github.torand.openapi2java.collectors.ResourceInfoCollector;
import io.github.torand.openapi2java.model.ResourceInfo;
import io.github.torand.openapi2java.writers.ResourceWriter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.github.torand.javacommons.collection.CollectionHelper.isEmpty;
import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static io.github.torand.openapi2java.utils.StringUtils.pluralSuffix;
import static io.github.torand.openapi2java.writers.WriterFactory.createResourceWriter;
import static java.util.stream.Collectors.joining;

/**
 * Generates source code for resources.
 */
public class ResourceGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ResourceGenerator.class);
    private final Options opts;

    public ResourceGenerator(Options opts) {
        this.opts = opts;
    }

    public void generate(OpenAPI openApiDoc) {
        int clientCount = 0;

        if (nonBlank(opts.resourceNameOverride())) {
            clientCount = generateWithNameOverride(openApiDoc);
        } else {
            if (isEmpty(openApiDoc.getTags())) {
                logger.error("The OpenAPI specification does not contain tags. Please configure a resource name override to generate a resource interface.");
                return;
            }

            clientCount = generateFromTags(openApiDoc);
        }

        if (logger.isInfoEnabled()) {
            logger.info("Generated {} REST client{} in directory {}", clientCount, pluralSuffix(clientCount), opts.outputDir());
        }
    }

    private int generateWithNameOverride(OpenAPI openApiDoc) {
        ComponentResolver componentResolver = new ComponentResolver(openApiDoc);
        ResourceInfoCollector resourceInfoCollector = new ResourceInfoCollector(componentResolver, opts);

        String resourceName = opts.resourceNameOverride();

        if (opts.verbose()) {
            logger.info("Generating REST client: {}{}", resourceName, opts.resourceNameSuffix());
        }

        ResourceInfo resourceInfo = resourceInfoCollector.getResourceInfo(resourceName, openApiDoc.getPaths(), openApiDoc.getSecurity(), null);

        String resourceFilename = resourceInfo.name + opts.getFileExtension();
        try (ResourceWriter resourceWriter = createResourceWriter(resourceFilename, opts)) {
            if (resourceInfo.isEmpty()) {
                logger.warn("No paths found in OpenAPI specification");
                return 0;
            } else {
                resourceWriter.write(resourceInfo);
                return 1;
            }
        } catch (IOException e) {
            logger.error("Failed to write file {}", resourceFilename, e);
            return 0;
        }
    }

    private int generateFromTags(OpenAPI openApiDoc) {
        ComponentResolver componentResolver = new ComponentResolver(openApiDoc);
        ResourceInfoCollector resourceInfoCollector = new ResourceInfoCollector(componentResolver, opts);

        AtomicInteger clientCount = new AtomicInteger(0);

        openApiDoc.getTags().forEach(tag -> {
            if (isEmpty(opts.includeTags()) || opts.includeTags().contains(tag.getName())) {
                String resourceName = getResourceName(tag);

                if (opts.verbose()) {
                    logger.info("Generating REST client for tag \"{}\": {}{}", tag.getName(), resourceName, opts.resourceNameSuffix());
                }

                ResourceInfo resourceInfo = resourceInfoCollector.getResourceInfo(resourceName, openApiDoc.getPaths(), openApiDoc.getSecurity(), tag);

                String resourceFilename = resourceInfo.name + opts.getFileExtension();
                try (ResourceWriter resourceWriter = createResourceWriter(resourceFilename, opts)) {
                    if (resourceInfo.isEmpty()) {
                        logger.warn("No paths found for tag \"{}\"", tag.getName());
                    } else {
                        resourceWriter.write(resourceInfo);
                        clientCount.incrementAndGet();
                    }
                } catch (IOException e) {
                    logger.error("Failed to write file {}", resourceFilename, e);
                }
            }
        });

        return clientCount.get();
    }

    private String getResourceName(Tag tag) {
        String tagName = tag.getName().trim();
        String[] tagSubNames = tagName.split(" ");
        return Stream.of(tagSubNames).map(StringHelper::capitalize).collect(joining());
    }
}
