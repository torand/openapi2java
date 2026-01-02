/*
 * Copyright (c) 2024-2026 Tore Eide Andersen
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

import io.github.torand.openapi2java.generators.ModelGenerator;
import io.github.torand.openapi2java.generators.Options;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.github.torand.openapi2java.TestHelper.*;

class ModelGeneratorTest {

    private static final Set<String> COMMON_POJOS = Set.of(
        "AddressV1",
        "Error"
    );

    private static final Set<String> DOMAIN_POJOS = Set.of(
        "OrderStatusV1",
        "OrderV1",
        "OrderItemV1",
        "ProductCategoryV1",
        "ProductV1",
        "UserTypeV1",
        "UserProfileV1",
        "NewUserProfileV1"
    );

    // These POJOs generate different model for OpenAPI 3.0 than for OpenAPI 3.1
    private static final Set<String> POJOS_WITH_30_MODEL = Set.of(
        "OrderV1",
        "OrderItemV1",
        "ProductV1",
        "UserProfileV1",
        "NewUserProfileV1"
    );

    @Test
    void shouldGenerateJavaPojos() {
        Options opts = getJavaOptions();
        OpenAPI openApiDoc = loadOpenApi31Spec();

        new ModelGenerator(opts).generate(openApiDoc);

        for (String pojo : COMMON_POJOS) {
            assertMatchingJavaFiles("model/common/%sDto.java".formatted(pojo));
        }

        for (String pojo : DOMAIN_POJOS) {
            assertMatchingJavaFiles("model/%sDto.java".formatted(pojo));
        }
    }

    @Test
    void shouldGenerateJavaPojos_OpenApi30() {
        Options opts = getJavaOptions();
        OpenAPI openApiDoc = loadOpenApi30Spec();

        new ModelGenerator(opts).generate(openApiDoc);

        for (String pojo : COMMON_POJOS) {
            String filename = "model/common/%sDto.java".formatted(pojo);
            if (POJOS_WITH_30_MODEL.contains(pojo)) {
                assertMatchingJavaFilesForOpenApi30(filename);
            } else {
                assertMatchingJavaFiles(filename);
            }
        }

        for (String pojo : DOMAIN_POJOS) {
            String filename = "model/%sDto.java".formatted(pojo);
            if (POJOS_WITH_30_MODEL.contains(pojo)) {
                assertMatchingJavaFilesForOpenApi30(filename);
            } else {
                assertMatchingJavaFiles(filename);
            }
        }
    }

    @Test
    void shouldGenerateKotlinPojos() {
        Options opts = getKotlinOptions();
        OpenAPI openApiDoc = loadOpenApi31Spec();

        new ModelGenerator(opts).generate(openApiDoc);

        for (String pojo : COMMON_POJOS) {
            assertMatchingKotlinFiles("model/common/%sDto.kt".formatted(pojo));
        }

        for (String pojo : DOMAIN_POJOS) {
            assertMatchingKotlinFiles("model/%sDto.kt".formatted(pojo));
        }
    }

    @Test
    void shouldGenerateKotlinPojos_OpenApi30() {
        Options opts = getKotlinOptions();
        OpenAPI openApiDoc = loadOpenApi30Spec();

        new ModelGenerator(opts).generate(openApiDoc);

        for (String pojo : COMMON_POJOS) {
            String filename = "model/common/%sDto.kt".formatted(pojo);
            if (POJOS_WITH_30_MODEL.contains(pojo)) {
                assertMatchingKotlinFilesForOpenApi30(filename);
            } else {
                assertMatchingKotlinFiles(filename);
            }
        }

        for (String pojo : DOMAIN_POJOS) {
            String filename = "model/%sDto.kt".formatted(pojo);
            if (POJOS_WITH_30_MODEL.contains(pojo)) {
                assertMatchingKotlinFilesForOpenApi30(filename);
            } else {
                assertMatchingKotlinFiles(filename);
            }
        }
    }
}
