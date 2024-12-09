package io.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static io.github.torand.openapi2java.TestHelper.assertMatchingJavaFiles;
import static io.github.torand.openapi2java.TestHelper.assertMatchingKotlinFiles;
import static io.github.torand.openapi2java.TestHelper.getJavaOptions;
import static io.github.torand.openapi2java.TestHelper.getKotlinOptions;
import static io.github.torand.openapi2java.TestHelper.loadOpenApiSpec;

public class OpenApiDefGeneratorTest {

    @Test
    public void shouldGenerateJavaOpenApiDef() {
        Options opts = getJavaOptions();
        OpenAPI openApiDoc = loadOpenApiSpec();

        new OpenApiDefGenerator(opts).generate(openApiDoc);

        assertMatchingJavaFiles("OpenApiDefinition.java");
    }

    @Test
    public void shouldGenerateKotlinOpenApiDef() {
        Options opts = getKotlinOptions();
        OpenAPI openApiDoc = loadOpenApiSpec();

        new OpenApiDefGenerator(opts).generate(openApiDoc);

        assertMatchingKotlinFiles("OpenApiDefinition.kt");
    }
}
