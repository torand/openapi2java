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

import static io.github.torand.openapi2java.TestHelper.ConfigVariant.Resteasy;
import static io.github.torand.openapi2java.TestHelper.assertMatchingJavaFiles;
import static io.github.torand.openapi2java.TestHelper.assertMatchingJavaFilesVariant;
import static io.github.torand.openapi2java.TestHelper.assertMatchingKotlinFiles;
import static io.github.torand.openapi2java.TestHelper.assertMatchingKotlinFilesVariant;
import static io.github.torand.openapi2java.TestHelper.getJavaOptions;
import static io.github.torand.openapi2java.TestHelper.getKotlinOptions;
import static io.github.torand.openapi2java.TestHelper.loadOpenApi30Spec;
import static io.github.torand.openapi2java.TestHelper.loadOpenApi31Spec;
import static io.github.torand.openapi2java.TestHelper.withResteasyResponse;
import static java.util.Collections.emptyList;

public class ResourceGeneratorTest {

    private static final String[] RESOURCES = {
        "Users",
        "Products",
        "Orders"
    };

    @Test
    public void shouldGenerateJavaResources() {
        Options opts = getJavaOptions();
        OpenAPI openApiDoc = loadOpenApi31Spec();

        new ResourceGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingJavaFiles("%sApi.java".formatted(resource));
        }
    }

    @Test
    public void shouldGenerateJavaResources_OpenApi30() {
        Options opts = getJavaOptions();
        OpenAPI openApiDoc = loadOpenApi30Spec();

        new ResourceGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingJavaFiles("%sApi.java".formatted(resource));
        }
    }

    @Test
    public void shouldGenerateJavaResources_withResteasyResponses() {
        Options opts = withResteasyResponse(getJavaOptions());
        OpenAPI openApiDoc = loadOpenApi31Spec();

        new ResourceGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingJavaFilesVariant("%sApi".formatted(resource), Resteasy);
        }
    }

    @Test
    public void shouldGenerateJavaResource_withNameOverride() {
        Options opts = getJavaOptions();
        opts.resourceNameOverride = "Compound";

        OpenAPI openApiDoc = loadOpenApi31Spec();
        removeTags(openApiDoc);

        new ResourceGenerator(opts).generate(openApiDoc);

        assertMatchingJavaFiles("%sApi.java".formatted(opts.resourceNameOverride));
    }

    @Test
    public void shouldGenerateKotlinResources() {
        Options opts = getKotlinOptions();
        OpenAPI openApiDoc = loadOpenApi31Spec();

        new ResourceGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingKotlinFiles("%sApi.kt".formatted(resource));
        }
    }

    @Test
    public void shouldGenerateKotlinResources_OpenApi30() {
        Options opts = getKotlinOptions();
        OpenAPI openApiDoc = loadOpenApi30Spec();

        new ResourceGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingKotlinFiles("%sApi.kt".formatted(resource));
        }
    }

    @Test
    public void shouldGenerateKotlinResources_withResteasyResponses() {
        Options opts = withResteasyResponse(getKotlinOptions());
        OpenAPI openApiDoc = loadOpenApi31Spec();

        new ResourceGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingKotlinFilesVariant("%sApi".formatted(resource), Resteasy);
        }
    }

    @Test
    public void shouldGenerateKotlinResource_withNameOverride() {
        Options opts = getKotlinOptions();
        opts.resourceNameOverride = "Compound";

        OpenAPI openApiDoc = loadOpenApi31Spec();
        removeTags(openApiDoc);

        new ResourceGenerator(opts).generate(openApiDoc);

        assertMatchingKotlinFiles("%sApi.kt".formatted(opts.resourceNameOverride));
    }

    private void removeTags(OpenAPI openApiDoc) {
        openApiDoc.setTags(emptyList());
        openApiDoc.getPaths().values()
            .forEach(pathItem -> pathItem.readOperations()
                .forEach(operation -> operation.setTags(emptyList()))
            );
    }
}
