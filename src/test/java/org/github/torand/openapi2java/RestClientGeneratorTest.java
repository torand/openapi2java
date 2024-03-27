package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.Test;

import static org.github.torand.openapi2java.TestHelper.assertMatchingJavaFiles;
import static org.github.torand.openapi2java.TestHelper.assertMatchingKotlinFiles;
import static org.github.torand.openapi2java.TestHelper.getJavaOptions;
import static org.github.torand.openapi2java.TestHelper.getKotlinOptions;
import static org.github.torand.openapi2java.TestHelper.loadOpenApiSpec;

public class RestClientGeneratorTest {

    private static final String[] RESOURCES = {
        "Users",
        "Tickets"
    };

    @Test
    public void shouldGenerateJavaRestClients() {
        Options opts = getJavaOptions();
        OpenAPI openApiDoc = loadOpenApiSpec();

        new RestClientGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingJavaFiles("%sApi.java".formatted(resource));
        }
    }

    @Test
    public void shouldGenerateKotlinRestClients() {
        Options opts = getKotlinOptions();
        OpenAPI openApiDoc = loadOpenApiSpec();

        new RestClientGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingKotlinFiles("%sApi.kt".formatted(resource));
        }
    }
}
