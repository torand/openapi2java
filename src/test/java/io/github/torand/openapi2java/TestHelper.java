package io.github.torand.openapi2java;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.torand.openapi2java.utils.StringHelper.removeLineBreaks;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.junit.jupiter.api.Assertions.fail;

class TestHelper {

    private TestHelper() {}

    static OpenAPI loadOpenApiSpec() {
        String openApiUri = getResourceUri("openapi.json").toString();
        SwaggerParseResult result = new OpenAPIParser().readLocation(openApiUri, null, null);
        return result.getOpenAPI();
    }

    static Options getJavaOptions() {
        Options opts = new Options();
        opts.rootPackage = "io.github.torand.test";
        opts.outputDir = "target/test-output/java";
        opts.includeTags = emptyList();
        opts.useKotlinSyntax = false;
        opts.verbose = true;
        return opts;
    }

    static Options getKotlinOptions() {
        Options opts = new Options();
        opts.rootPackage = "io.github.torand.test";
        opts.outputDir = "target/test-output/kotlin";
        opts.includeTags = emptyList();
        opts.useKotlinSyntax = true;
        opts.verbose = true;
        return opts;
    }

    static void assertMatchingJavaFiles(String filename) {
        Path expectedPath = getResourcePath("expected-output/java/%s".formatted(filename));
        Path actualPath = Path.of("target/test-output/java/%s".formatted(filename));

        assertMatchingFiles(expectedPath, actualPath);
    }

    static void assertMatchingKotlinFiles(String filename) {
        Path expectedPath = getResourcePath("expected-output/kotlin/%s".formatted(filename));
        Path actualPath = Path.of("target/test-output/kotlin/%s".formatted(filename));

        assertMatchingFiles(expectedPath, actualPath);
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
