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
package io.github.torand.openapi2java;

import io.github.torand.openapi2java.generators.Options;
import io.github.torand.openapi2java.generators.ResourceGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static io.github.torand.openapi2java.TestHelper.ConfigVariant.RESTEASY;
import static io.github.torand.openapi2java.TestHelper.*;
import static java.util.Collections.emptyList;

class ResourceGeneratorTest {

    private static final String[] RESOURCES = {
        "Users",
        "Products",
        "Orders"
    };

    @Test
    void shouldGenerateJavaResources() {
        Options opts = getJavaOptions();
        OpenAPI openApiDoc = loadOpenApi31Spec();

        new ResourceGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingJavaFiles("%sApi.java".formatted(resource));
        }
    }

    @Test
    void shouldGenerateJavaResources_OpenApi30() {
        Options opts = getJavaOptions();
        OpenAPI openApiDoc = loadOpenApi30Spec();

        new ResourceGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingJavaFiles("%sApi.java".formatted(resource));
        }
    }

    @Test
    void shouldGenerateJavaResources_withResteasyResponses() {
        Options opts = withResteasyResponse(getJavaOptions());
        OpenAPI openApiDoc = loadOpenApi31Spec();

        new ResourceGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingJavaFilesVariant("%sApi".formatted(resource), RESTEASY);
        }
    }

    @Test
    void shouldGenerateJavaResource_withNameOverride() {
        Options opts = getJavaOptions()
            .withResourceNameOverride("Compound");

        OpenAPI openApiDoc = loadOpenApi31Spec();
        removeTags(openApiDoc);

        new ResourceGenerator(opts).generate(openApiDoc);

        assertMatchingJavaFiles("%sApi.java".formatted(opts.resourceNameOverride()));
    }

    @Test
    void shouldGenerateKotlinResources() {
        Options opts = getKotlinOptions();
        OpenAPI openApiDoc = loadOpenApi31Spec();

        new ResourceGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingKotlinFiles("%sApi.kt".formatted(resource));
        }
    }

    @Test
    void shouldGenerateKotlinResources_OpenApi30() {
        Options opts = getKotlinOptions();
        OpenAPI openApiDoc = loadOpenApi30Spec();

        new ResourceGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingKotlinFiles("%sApi.kt".formatted(resource));
        }
    }

    @Test
    void shouldGenerateKotlinResources_withResteasyResponses() {
        Options opts = withResteasyResponse(getKotlinOptions());
        OpenAPI openApiDoc = loadOpenApi31Spec();

        new ResourceGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingKotlinFilesVariant("%sApi".formatted(resource), RESTEASY);
        }
    }

    @Test
    void shouldGenerateKotlinResource_withNameOverride() {
        Options opts = getKotlinOptions()
            .withResourceNameOverride("Compound");

        OpenAPI openApiDoc = loadOpenApi31Spec();
        removeTags(openApiDoc);

        new ResourceGenerator(opts).generate(openApiDoc);

        assertMatchingKotlinFiles("%sApi.kt".formatted(opts.resourceNameOverride()));
    }

    private void removeTags(OpenAPI openApiDoc) {
        openApiDoc.setTags(emptyList());
        openApiDoc.getPaths().values()
            .forEach(pathItem -> pathItem.readOperations()
                .forEach(operation -> operation.setTags(emptyList()))
            );
    }
}
