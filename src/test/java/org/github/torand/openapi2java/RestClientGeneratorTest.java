package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.Test;

import static org.github.torand.openapi2java.TestHelper.assertMatchingFiles;
import static org.github.torand.openapi2java.TestHelper.getOptions;
import static org.github.torand.openapi2java.TestHelper.loadOpenApiSpec;

public class RestClientGeneratorTest {

    private static final String[] RESOURCES = {
        "Users",
        "Tickets"
    };

    @Test
    public void shouldGenerateRestClients() {
        Options opts = getOptions();
        OpenAPI openApiDoc = loadOpenApiSpec();

        new RestClientGenerator(opts).generate(openApiDoc);

        for (String resource : RESOURCES) {
            assertMatchingFiles("%sApi.java".formatted(resource));
        }
    }
}