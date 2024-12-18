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
import static io.github.torand.openapi2java.TestHelper.getJavaOptions;
import static io.github.torand.openapi2java.TestHelper.getKotlinOptions;
import static io.github.torand.openapi2java.TestHelper.loadOpenApiSpec;

public class ModelGeneratorTest {

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

    @Test
    public void shouldGenerateJavaPojos() {
        Options opts = getJavaOptions();
        OpenAPI openApiDoc = loadOpenApiSpec();

        new ModelGenerator(opts).generate(openApiDoc);

        for (String pojo : COMMON_POJOS) {
            assertMatchingJavaFiles("model/common/%sDto.java".formatted(pojo));
        }

        for (String pojo : DOMAIN_POJOS) {
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

        for (String pojo : DOMAIN_POJOS) {
            assertMatchingKotlinFiles("model/%sDto.kt".formatted(pojo));
        }
    }
}
