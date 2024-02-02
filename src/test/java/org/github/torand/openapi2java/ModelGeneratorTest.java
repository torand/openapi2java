package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.Test;

import static org.github.torand.openapi2java.TestHelper.assertMatchingFiles;
import static org.github.torand.openapi2java.TestHelper.getOptions;
import static org.github.torand.openapi2java.TestHelper.loadOpenApiSpec;

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
    public void shouldGeneratePojos() {
        Options opts = getOptions();
        OpenAPI openApiDoc = loadOpenApiSpec();

        new ModelGenerator(opts).generate(openApiDoc);

        for (String pojo : POJOS) {
            assertMatchingFiles("model/%sDto.java".formatted(pojo));
        }
    }
}