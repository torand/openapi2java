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

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.github.torand.openapi2java.TestHelper.assertMatchingJavaFiles;
import static io.github.torand.openapi2java.TestHelper.assertMatchingKotlinFiles;
import static io.github.torand.openapi2java.TestHelper.assertSnippet;
import static io.github.torand.openapi2java.TestHelper.getJavaOptions;
import static io.github.torand.openapi2java.TestHelper.getKotlinOptions;
import static io.github.torand.openapi2java.TestHelper.loadOpenApiSpec;

public class ModelGeneratorTest {

    private static final Set<String> COMMON_POJOS = Set.of(
        "AddressV1",
        "Error"
    );

    private static final Set<String> TICKET_POJOS = Set.of(
        "TicketTypeV1",
        "TicketStatusV1",
        "TicketV1",
        "TicketCommentV1",
        "TicketAttachmentV1",
        "TicketDetailsV1"
    );

    private static final Set<String> USER_POJOS = Set.of(
        "UserProfileV1",
        "NewUserProfileV1"
    );

    @Test
    public void shouldGenerateJavaPojos() {
        Options opts = getJavaOptions();
        OpenAPI openApiDoc = loadOpenApiSpec();

        new ModelGenerator(opts).generate(openApiDoc);

        for (String pojo : COMMON_POJOS) {
            assertMatchingJavaFiles("model/common/%sDto.java".formatted(pojo));
        }

        for (String pojo : TICKET_POJOS) {
            assertMatchingJavaFiles("model/%sDto.java".formatted(pojo));
        }

        for (String pojo : USER_POJOS) {
            assertMatchingJavaFiles("model/%sDto.java".formatted(pojo));
        }
    }

    @Test
    public void shouldGenerateKotlinPojos() {
        Options opts = getKotlinOptions();
        OpenAPI openApiDoc = loadOpenApiSpec();

        new ModelGenerator(opts).generate(openApiDoc);

        for (String pojo : COMMON_POJOS) {
            assertMatchingKotlinFiles("model/common/%sDto.kt".formatted(pojo));
        }

        for (String pojo : TICKET_POJOS) {
            assertMatchingKotlinFiles("model/%sDto.kt".formatted(pojo));
        }

        for (String pojo : USER_POJOS) {
            assertMatchingKotlinFiles("model/%sDto.kt".formatted(pojo));
        }
    }

    @Test
    public void shouldIncludeJsonPropertyOnPropertiesIfUsingRestResponse() {
        OpenAPI openApiDoc = loadOpenApiSpec();

        Options javaOpts = getJavaOptions();
        javaOpts.useResteasyResponse = true;
        javaOpts.addJsonPropertyAnnotations = true;

        new ModelGenerator(javaOpts).generate(openApiDoc);
        assertSnippet("java/model/TicketDetailsV1Dto.java", """
                    @Schema(description = "TBD", required = true)
                    @JsonProperty("type")
                    @NotNull
                    TicketTypeV1Dto type,
                """);
        assertSnippet("java/model/TicketDetailsV1Dto.java", "import com.fasterxml.jackson.annotation.JsonProperty");

        Options kotlinOpts = getKotlinOptions();
        kotlinOpts.useResteasyResponse = true;
        kotlinOpts.addJsonPropertyAnnotations = true;

        new ModelGenerator(kotlinOpts).generate(openApiDoc);
        assertSnippet("kotlin/model/TicketDetailsV1Dto.kt", """
                    @field:Schema(description = "TBD", required = true)
                    @JsonProperty("type")
                    @field:NotNull
                    val type: TicketTypeV1Dto,
                """);
        assertSnippet("kotlin/model/TicketDetailsV1Dto.kt", "import com.fasterxml.jackson.annotation.JsonProperty");
    }
}
