package io.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static io.github.torand.openapi2java.TestHelper.assertMatchingJavaFiles;
import static io.github.torand.openapi2java.TestHelper.assertMatchingKotlinFiles;
import static io.github.torand.openapi2java.TestHelper.getJavaOptions;
import static io.github.torand.openapi2java.TestHelper.getKotlinOptions;
import static io.github.torand.openapi2java.TestHelper.loadOpenApiSpec;

public class ModelGeneratorTest {

    private static final String[] POJOS = {
        "AddressV1",
        "Error",
        "TicketTypeV1",
        "TicketStatusV1",
        "TicketV1",
        "TicketCommentV1",
        "TicketAttachmentV1",
        "TicketDetailsV1",
        "UserProfileV1",
        "NewUserProfileV1"
    };

    @Test
    public void shouldGenerateJavaPojos() {
        Options opts = getJavaOptions();
        OpenAPI openApiDoc = loadOpenApiSpec();

        new ModelGenerator(opts).generate(openApiDoc);

        for (String pojo : POJOS) {
            assertMatchingJavaFiles("model/%sDto.java".formatted(pojo));
        }
    }

    @Test
    public void shouldGenerateKotlinPojos() {
        Options opts = getKotlinOptions();
        OpenAPI openApiDoc = loadOpenApiSpec();

        new ModelGenerator(opts).generate(openApiDoc);

        for (String pojo : POJOS) {
            assertMatchingKotlinFiles("model/%sDto.kt".formatted(pojo));
        }
    }
}
