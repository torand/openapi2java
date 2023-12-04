package org.github.torand.openapi2java;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Goal which touches a timestamp file.
 */
@org.apache.maven.plugins.annotations.Mojo( name = "generate", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class Mojo extends AbstractMojo
{
    /**
     * Input file containing OpenAPI specification
     */
    @Parameter(property = "openApiFile", required = true )
    private File openApiFile;

    /**
     * Root directory of output
     */
    @Parameter( property = "outputDir", defaultValue = "${project.build.directory}" )
    private File outputDirectory;

    /**
     * Root package of classes and enums
     */
    @Parameter( property = "rootPackage", required = true )
    private String rootPackage;

    /**
     * Root URL path for resources
     */
    @Parameter( property = "rootUrlPath", defaultValue = "api" )
    private String rootUrlPath;

    /**
     * Resource name suffix
     */
    @Parameter( property = "resourceNameSuffix", defaultValue = "Api" )
    private String resourceNameSuffix;

    public void execute() throws MojoExecutionException {
        SwaggerParseResult result = new OpenAPIParser().readLocation(openApiFile.toString(), null, null);
        OpenAPI openApiDoc = result.getOpenAPI();

        Options opts = new Options();
        opts.rootPackage = rootPackage;
        opts.outputDir = outputDirectory.toString();
        opts.rootUrlPath = rootUrlPath;
        opts.resourceNameSuffix = resourceNameSuffix;

        ModelGenerator modelGenerator = new ModelGenerator();
        modelGenerator.generate(openApiDoc, opts);

        RestClientGenerator restClientGenerator = new RestClientGenerator();
        restClientGenerator.generate(openApiDoc, opts);
    }
}
