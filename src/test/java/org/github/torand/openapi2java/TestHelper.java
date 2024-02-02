package org.github.torand.openapi2java;

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

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.github.torand.openapi2java.utils.StringHelper.removeLineBreaks;
import static org.junit.Assert.fail;

class TestHelper {

    private TestHelper() {}

    static OpenAPI loadOpenApiSpec() {
        String openApiUri = getResourceUri("openapi.json").toString();
        SwaggerParseResult result = new OpenAPIParser().readLocation(openApiUri, null, null);
        OpenAPI openApiDoc = result.getOpenAPI();
        return openApiDoc;
    }

    static Options getOptions() {
        Options opts = new Options();
        opts.rootPackage = "org.github.torand.test";
        opts.outputDir = "target/test-output";
        opts.includeTags = emptyList();
        opts.verbose = true;
        return opts;
    }

    static void assertMatchingFiles(String filename) {
        try {
            Path expectedPath = getResourcePath("expected-output/%s".formatted(filename));
            Path actualPath = Path.of("target/test-output/%s".formatted(filename));

            int mismatchPos = (int)Files.mismatch(expectedPath, actualPath);
            if (mismatchPos != -1) {
                printDiff(expectedPath, actualPath, mismatchPos);
                fail("Unexpected content in %s at position %d".formatted(actualPath, mismatchPos));
            }
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    private static void printDiff(Path expected, Path actual, int mismatchPos) throws IOException {
        String expectedContent = Files.readString(expected);
        String actualContent = Files.readString(actual);

        int printFrom = Math.max(0, mismatchPos - 30);
        int printTo = Math.min(mismatchPos + 30, Math.min(expectedContent.length(), actualContent.length()));

        System.out.println("Expected content : %s".formatted(removeLineBreaks(expectedContent.substring(printFrom, printTo))));
        System.out.println("Actual content   : %s".formatted(removeLineBreaks(actualContent.substring(printFrom, printTo))));
        System.out.println("                   %s^%s".formatted("-".repeat(mismatchPos-printFrom), "-".repeat(printTo-mismatchPos)));
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