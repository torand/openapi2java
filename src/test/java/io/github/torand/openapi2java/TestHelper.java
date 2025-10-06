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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.torand.openapi2java.generators.Options;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.torand.openapi2java.TestHelper.ConfigVariant.COMMON_HEADERS_FACTORY;
import static io.github.torand.openapi2java.TestHelper.ConfigVariant.OIDC_CLIENT_ANNOTATION;
import static io.github.torand.openapi2java.TestHelper.ConfigVariant.RESTEASY;
import static io.github.torand.openapi2java.utils.StringUtils.removeLineBreaks;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public final class TestHelper {

    private TestHelper() {}

    public enum ConfigVariant {
        RESTEASY("Resteasy"),
        NO_HEADER_PARAM("NoHeaderParam"),
        COMMON_HEADERS_FACTORY("CommonHeadersFactory"),
        OIDC_CLIENT_ANNOTATION("OidcClientAnnotation");

        String suffix;

        ConfigVariant(String suffix) {
            this.suffix = suffix;
        }
    }

    public static OpenAPI loadOpenApi31Spec() {
        String openApiUri = getResourceUri("openapi-3.1.json").toString();
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(openApiUri, null, null);
        return result.getOpenAPI();
    }

    public static OpenAPI loadOpenApi30Spec() {
        String openApiUri = getResourceUri("openapi-3.0.json").toString();
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(openApiUri, null, null);
        return result.getOpenAPI();
    }

    public static Options getJavaOptions() {
        return Options.defaults()
            .withRootPackage("io.github.torand.openapi2java.test")
            .withOutputDir("target/test-output/java")
            .withIncludeTags(emptyList())
            .withUseKotlinSyntax(false)
            .withUseResteasyResponse(false)
            .withVerbose(true);
    }

    public static Options getKotlinOptions() {
        return Options.defaults()
            .withRootPackage("io.github.torand.openapi2java.test")
            .withOutputDir("target/test-output/kotlin")
            .withIncludeTags(emptyList())
            .withUseKotlinSyntax(true)
            .withUseResteasyResponse(false)
            .withVerbose(true);
    }

    public static Options withResteasyResponse(Options baseOptions) {
        return baseOptions
            .withResourceNameSuffix(baseOptions.resourceNameSuffix() + "_" + RESTEASY.suffix)
            .withUseResteasyResponse(true);
    }

    public static Options withHeadersFactoryOverride(Options baseOptions) {
        return baseOptions
            .withResourceNameSuffix(baseOptions.resourceNameSuffix() + "_" + COMMON_HEADERS_FACTORY.suffix)
            .withResourceClientHeadersFactoryOverride("io.github.torand.openapi2java.test.CommonClientHeadersFactory");
    }

    public static Options withOidcClientAnnotation(Options baseOptions) {
        return baseOptions
            .withResourceNameSuffix(baseOptions.resourceNameSuffix() + "_" + OIDC_CLIENT_ANNOTATION.suffix)
            .withUseOidcClientAnnotation(true);
    }

    public static void assertSnippet(String path, String expectedSnippet) {
        try {
            Path actualPath = Path.of("target/test-output/" + path);
            String content = Files.readString(actualPath);
            assertThat(content).contains(expectedSnippet);
        } catch (IOException e) {
            throw new RuntimeException("Could not find file by name %s".formatted(path), e);
        }
    }

    public static void assertMatchingJavaFiles(String filename) {
        Path expectedPath = getResourcePath("expected-output/java/%s".formatted(filename));
        Path actualPath = Path.of("target/test-output/java/%s".formatted(filename));

        assertMatchingFiles(expectedPath, actualPath);
    }

    public static void assertMatchingJavaFilesForOpenApi30(String filename) {
        Path expectedPath = getResourcePath("expected-output-30/java/%s".formatted(filename));
        Path actualPath = Path.of("target/test-output/java/%s".formatted(filename));

        assertMatchingFiles(expectedPath, actualPath);
    }

    public static void assertMatchingJavaFilesVariant(String filename, ConfigVariant variant) {
        assertMatchingJavaFiles("%s%s.java".formatted(filename, "_" + variant.suffix));
    }

    public static void assertMatchingKotlinFiles(String filename) {
        Path expectedPath = getResourcePath("expected-output/kotlin/%s".formatted(filename));
        Path actualPath = Path.of("target/test-output/kotlin/%s".formatted(filename));

        assertMatchingFiles(expectedPath, actualPath);
    }

    public static void assertMatchingKotlinFilesForOpenApi30(String filename) {
        Path expectedPath = getResourcePath("expected-output-30/kotlin/%s".formatted(filename));
        Path actualPath = Path.of("target/test-output/kotlin/%s".formatted(filename));

        assertMatchingFiles(expectedPath, actualPath);
    }

    public static void assertMatchingKotlinFilesVariant(String filename, ConfigVariant variant) {
        assertMatchingKotlinFiles("%s%s.kt".formatted(filename, "_" + variant.suffix));
    }

    public static JsonNode parseJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.reader().readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    private static void assertMatchingFiles(Path expectedPath, Path actualPath) {
        try {
            int mismatchPos = (int)Files.mismatch(expectedPath, actualPath);
            if (mismatchPos != -1) {
                System.out.printf("Unexpected content in %s at position %d:%n", actualPath, mismatchPos);
                printDiff(expectedPath, actualPath, mismatchPos);
                fail("Actual file %s does not match expected file %s".formatted(actualPath, expectedPath));
            }
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    private static void printDiff(Path expected, Path actual, int mismatchPos) throws IOException {
        String expectedContent = Files.readString(expected);
        String actualContent = Files.readString(actual);

        int printFrom = Math.max(0, mismatchPos - 50);
        int printTo = Math.min(mismatchPos + 60, Math.min(expectedContent.length(), actualContent.length()));

        System.out.printf("Expected content : %s%n", removeLineBreaks(expectedContent.substring(printFrom, printTo)));
        System.out.printf("Actual content   : %s%n", removeLineBreaks(actualContent.substring(printFrom, printTo)));
        System.out.printf("                   %s^%s%n", "-".repeat(mismatchPos-printFrom), "-".repeat(printTo-mismatchPos));
    }

    private static URI getResourceUri(String name) {
        try {
            URL resource = TestHelper.class.getResource("/" + name);
            if (isNull(resource)) {
                throw new IllegalArgumentException("Resource %s not found".formatted(name));
            }
            return resource.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to create URI for resource " + name, e);
        }
    }

    private static Path getResourcePath(String name) {
        return Paths.get(getResourceUri(name));
    }
}
