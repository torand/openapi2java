/*
 * Copyright (c) 2024 Tore Eide Andersen
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
package io.github.torand.openapi2java;

import io.github.torand.openapi2java.collectors.ComponentResolver;
import io.github.torand.openapi2java.collectors.ResourceInfoCollector;
import io.github.torand.openapi2java.model.ResourceInfo;
import io.github.torand.openapi2java.utils.StringHelper;
import io.github.torand.openapi2java.writers.ResourceWriter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.github.torand.openapi2java.utils.CollectionHelper.isEmpty;
import static io.github.torand.openapi2java.utils.StringHelper.pluralSuffix;
import static io.github.torand.openapi2java.writers.WriterFactory.createResourceWriter;
import static java.util.stream.Collectors.joining;

public class ResourceGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ResourceGenerator.class);
    private final Options opts;

    public ResourceGenerator(Options opts) {
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
                    logger.info("Generating REST client for tag \"{}\": {}", tag.getName(), resourceName + opts.resourceNameSuffix);
                }

                ResourceInfo resourceInfo = resourceInfoCollector.getResourceInfo(resourceName, openApiDoc.getPaths(), openApiDoc.getSecurity(), tag);

                String resourceFilename = resourceInfo.name + opts.getFileExtension();
                try (ResourceWriter resourceWriter = createResourceWriter(resourceFilename, opts)) {
                    if (resourceInfo.isEmpty()) {
                        logger.warn("No paths found for tag \"{}\"", tag.getName());
                    } else {
                        resourceWriter.write(resourceInfo);
                    }
                } catch (IOException e) {
                    logger.error("Failed to write file {}", resourceFilename, e);
                }
            }
        });

        logger.info("Generated {} REST client{} in directory {}", clientCount.get(), pluralSuffix(clientCount.get()), opts.outputDir);
    }

    private String getResourceClassName(Tag tag) {
        String tagName = tag.getName().trim();
        String[] tagSubNames = tagName.split(" ");
        return Stream.of(tagSubNames).map(StringHelper::capitalize).collect(joining());
    }
}