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

import io.github.torand.openapi2java.generators.OpenApiDefGenerator;
import io.github.torand.openapi2java.generators.Options;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static io.github.torand.openapi2java.TestHelper.assertMatchingJavaFiles;
import static io.github.torand.openapi2java.TestHelper.assertMatchingKotlinFiles;
import static io.github.torand.openapi2java.TestHelper.getJavaOptions;
import static io.github.torand.openapi2java.TestHelper.getKotlinOptions;
import static io.github.torand.openapi2java.TestHelper.loadOpenApi30Spec;
import static io.github.torand.openapi2java.TestHelper.loadOpenApi31Spec;

public class OpenApiDefGeneratorTest {

    @Test
    public void shouldGenerateJavaOpenApiDef() {
        Options opts = getJavaOptions();
        OpenAPI openApiDoc = loadOpenApi31Spec();

        new OpenApiDefGenerator(opts).generate(openApiDoc);

        assertMatchingJavaFiles("OpenApiDefinition.java");
    }

    @Test
    public void shouldGenerateJavaOpenApiDef_OpenApi30() {
        Options opts = getJavaOptions();
        OpenAPI openApiDoc = loadOpenApi30Spec();

        new OpenApiDefGenerator(opts).generate(openApiDoc);

        assertMatchingJavaFiles("OpenApiDefinition.java");
    }

    @Test
    public void shouldGenerateKotlinOpenApiDef() {
        Options opts = getKotlinOptions();
        OpenAPI openApiDoc = loadOpenApi31Spec();

        new OpenApiDefGenerator(opts).generate(openApiDoc);

        assertMatchingKotlinFiles("OpenApiDefinition.kt");
    }

    @Test
    public void shouldGenerateKotlinOpenApiDef_OpenApi30() {
        Options opts = getKotlinOptions();
        OpenAPI openApiDoc = loadOpenApi30Spec();

        new OpenApiDefGenerator(opts).generate(openApiDoc);

        assertMatchingKotlinFiles("OpenApiDefinition.kt");
    }
}
