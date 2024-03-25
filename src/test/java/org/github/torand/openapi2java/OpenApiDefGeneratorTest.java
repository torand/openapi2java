package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.Test;

public class OpenApiDefGeneratorTest {

    @Test
    public void shouldGenerateOpenApiDef() {
        Options opts = TestHelper.getOptions();
        OpenAPI openApiDoc = TestHelper.loadOpenApiSpec();

        new OpenApiDefGenerator(opts).generate(openApiDoc);

        TestHelper.assertMatchingFiles("OpenApiDefinition.java");
    }
}