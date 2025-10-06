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

import io.github.torand.openapi2java.generators.ModelGenerator;
import io.github.torand.openapi2java.generators.OpenApiDefGenerator;
import io.github.torand.openapi2java.generators.Options;
import io.github.torand.openapi2java.generators.ResourceGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Generates source code for a REST-API with resource interfaces and representation model classes
 * based on an OpenAPI specification file.
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class OpenApi2JavaMojo extends AbstractMojo {

    private static final Logger logger = LoggerFactory.getLogger(OpenApi2JavaMojo.class);

    /**
     * Input file containing OpenAPI specification.
     */
    @Parameter(property = "openApiFile", required = true )
    private String openApiFile;

    /**
     * Root directory of output.
     */
    @Parameter( property = "outputDir", defaultValue = "${project.build.directory}" )
    private String outputDir;

    /**
     * Root package of classes and enums.
     */
    @Parameter( property = "rootPackage", required = true )
    private String rootPackage;

    /**
     * Root URL path for resources.
     */
    @Parameter( property = "rootUrlPath", defaultValue = "api" )
    private String rootUrlPath;

    /**
     * Resource name suffix.
     */
    @Parameter( property = "resourceNameSuffix", defaultValue = "Api" )
    private String resourceNameSuffix;

    /**
     * Resource name override. Specify this to have all endpoints generated into a single resource interface.
     * If not specified, the endpoints are generated into separate resource interfaces, one per tag.
     */
    @Parameter( property = "resourceNameOverride", defaultValue = "" )
    private String resourceNameOverride;

    /**
     * Microprofile Rest Client headers factory override. Specify this to have all resources generated use the same client headers factory.
     * If not specified, each resource gets a separate client headers factory based on a tag extension property (if any).
     */
    @Parameter( property = "resourceClientHeadersFactoryOverride", defaultValue = "" )
    private String resourceClientHeadersFactoryOverride;

    /**
     * Pojo name suffix.
     */
    @Parameter( property = "pojoNameSuffix", defaultValue = "Dto" )
    private String pojoNameSuffix;

    /**
     * Use Java records for Pojos.
     */
    @Parameter( property = "pojosAsRecords", defaultValue = "true" )
    private boolean pojosAsRecords;

    /**
     * Tags to generate source code for. Includes all tags if not specified.
     */
    @Parameter( property = "includeTags", defaultValue = "" )
    private List<String> includeTags;

    /**
     * Generate resource interfaces (one for each tag included).
     */
    @Parameter( property = "generateResourceInterfaces", defaultValue = "true" )
    private boolean generateResourceInterfaces;

    /**
     * Generate an OpenAPI definition class file with implementation oriented annotations.
     */
    @Parameter( property = "generateOpenApiDefClass", defaultValue = "true" )
    private boolean generateOpenApiDefClass;

    /**
     * Generate Jackson JSON property annotations.
     */
    @Parameter( property = "addJsonPropertyAnnotations", defaultValue = "true" )
    private boolean addJsonPropertyAnnotations;

    /**
     * Generate Jakarta Bean Validation annotations.
     */
    @Parameter( property = "addJakartaBeanValidationAnnotations", defaultValue = "true" )
    private boolean addJakartaBeanValidationAnnotations;

    /**
     * Generate Microprofile OpenAPI annotations.
     */
    @Parameter( property = "addMpOpenApiAnnotations", defaultValue = "true" )
    private boolean addMpOpenApiAnnotations;

    /**
     * Generate Microprofile Rest Client annotations (on resource interfaces).
     */
    @Parameter( property = "addMpRestClientAnnotations", defaultValue = "true" )
    private boolean addMpRestClientAnnotations;

    /**
     * Generate Kotlin source code.
     */
    @Parameter( property = "useKotlinSyntax", defaultValue = "false" )
    private boolean useKotlinSyntax;

    /**
     * Use the more typesafe {@code RestResponse} from RESTEasy instead of the
     * normal untyped {@code Response} from Jakarta WS core.
     *
     * Only use this if the client will be implemented using RESTEasy.
     *
     * Note! Setting this to true implicitly sets 'addJsonPropertyAnnotations' to true, as well.
     */
    @Parameter( property = "useResteasyResponse", defaultValue = "false" )
    private boolean useResteasyResponse;

    /**
     * Add the Quarkus OIDC client annotation to all resource interfaces.
     */
    @Parameter( property = "useOidcClientAnnotation", defaultValue = "false" )
    private boolean useOidcClientAnnotation;

    /**
     * Whether to output indents with the tab character.
     */
    @Parameter( property = "indentWithTab", defaultValue = "false" )
    private boolean indentWithTab;

    /**
     * The number of spaces for each indent level, when not using the tab character.
     */
    @Parameter( property = "indentSize", defaultValue = "4" )
    private int indentSize;

    /**
     * Enables verbose logging
     */
    @Parameter( property = "verbose", defaultValue = "false" )
    private boolean verbose;

    public void execute() throws MojoExecutionException {
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(openApiFile, null, null);
        OpenAPI openApiDoc = result.getOpenAPI();

        Options opts = new Options(
            outputDir,
            rootPackage,
            rootUrlPath,
            resourceNameSuffix,
            resourceNameOverride,
            resourceClientHeadersFactoryOverride,
            pojoNameSuffix,
            pojosAsRecords,
            includeTags,
            generateResourceInterfaces,
            generateOpenApiDefClass,
            addJsonPropertyAnnotations,
            addJakartaBeanValidationAnnotations,
            addMpOpenApiAnnotations,
            addMpRestClientAnnotations,
            useKotlinSyntax,
            useResteasyResponse,
            useOidcClientAnnotation,
            indentWithTab,
            indentSize,
            verbose
        );

        if (useResteasyResponse && !addJsonPropertyAnnotations) {
            logger.info("Using ResteasyResponse: explicitly setting 'addJsonPropertyAnnotations' to true");
            addJsonPropertyAnnotations = true;
        }

        ModelGenerator modelGenerator = new ModelGenerator(opts);
        modelGenerator.generate(openApiDoc);

        if (opts.generateResourceInterfaces()) {
            ResourceGenerator resourceGenerator = new ResourceGenerator(opts);
            resourceGenerator.generate(openApiDoc);
        }

        if (opts.generateOpenApiDefClass()) {
            OpenApiDefGenerator openApiDefGenerator = new OpenApiDefGenerator(opts);
            openApiDefGenerator.generate(openApiDoc);
        }
    }
}
