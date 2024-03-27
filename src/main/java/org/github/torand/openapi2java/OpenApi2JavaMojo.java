package org.github.torand.openapi2java;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

/**
 * Goal which generates Java (or Kotlin) source code for a REST-API with resource and representation model classes
 * based on an OpenAPI 3.1 specification.
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class OpenApi2JavaMojo extends AbstractMojo
{
    /**
     * Input file containing OpenAPI specification
     */
    @Parameter(property = "openApiFile", required = true )
    private String openApiFile;

    /**
     * Root directory of output
     */
    @Parameter( property = "outputDir", defaultValue = "${project.build.directory}" )
    private String outputDirectory;

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

    /**
     * Pojo name suffix
     */
    @Parameter( property = "pojoNameSuffix", defaultValue = "Dto" )
    private String pojoNameSuffix;

    /**
     * Use Java records for pojos
     */
    @Parameter( property = "pojosAsRecords", defaultValue = "true" )
    private boolean pojosAsRecords;

    /**
     * Tags to generate source code for. Includes all tags if not specified.
     */
    @Parameter( property = "includeTags", defaultValue = "" )
    private List<String> includeTags;

    /**
     * Generate Kotlin source code
     */
    @Parameter( property = "useKotlinSyntax", defaultValue = "false" )
    private boolean useKotlinSyntax;

    /**
     * Enables verbose logging
     */
    @Parameter( property = "verbose", defaultValue = "false" )
    private boolean verbose;

    public void execute() throws MojoExecutionException {
        SwaggerParseResult result = new OpenAPIParser().readLocation(openApiFile, null, null);
        OpenAPI openApiDoc = result.getOpenAPI();

        Options opts = new Options();
        opts.rootPackage = rootPackage;
        opts.outputDir = outputDirectory.toString();
        opts.rootUrlPath = rootUrlPath;
        opts.resourceNameSuffix = resourceNameSuffix;
        opts.pojoNameSuffix = pojoNameSuffix;
        opts.pojosAsRecords = pojosAsRecords;
        opts.includeTags = includeTags;
        opts.useKotlinSyntax = useKotlinSyntax;
        opts.verbose = verbose;

        ModelGenerator modelGenerator = new ModelGenerator(opts);
        modelGenerator.generate(openApiDoc);

        RestClientGenerator restClientGenerator = new RestClientGenerator(opts);
        restClientGenerator.generate(openApiDoc);

        OpenApiDefGenerator openApiDefGenerator = new OpenApiDefGenerator(opts);
        openApiDefGenerator.generate(openApiDoc);
    }
}
