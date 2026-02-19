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
package io.github.torand.openapi2java.generators;

import java.util.List;

import static io.github.torand.javacommons.lang.StringHelper.isBlank;

/**
 * Contains configuration of the source code generators.
 * @param outputDir the root directory of output.
 * @param rootPackage the root package of classes and enums.
 * @param rootUrlPath the root URL path for resources.
 * @param resourceNameSuffix the resource name suffix.
 * @param resourceNameOverride the resource name override.
 * @param resourceConfigKeyOverride the resource config-key override.
 * @param resourceClientHeadersFactoryOverride the resource client headers factory override.
 * @param resourceProvidersOverride the resource providers override.
 * @param pojoNameSuffix the Pojo name suffix.
 * @param pojosAsRecords the flag to use Java records for Pojos.
 * @param includeTags the tags to generate source code for. Includes all tags if not specified.
 * @param generateResourceInterfaces the flag to generate resource interfaces (one for each tag included).
 * @param generateOpenApiDefClass the flag to generate an OpenAPI definition class file with implementation oriented annotations.
 * @param addJsonPropertyAnnotations the flag to generate Jackson JSON property annotations.
 * @param addJakartaBeanValidationAnnotations the flag to generate Jakarta Bean Validation annotations.
 * @param addMpOpenApiAnnotations the flag to generate Microprofile OpenAPI annotations.
 * @param addMpRestClientAnnotations the flag to generate Microprofile Rest Client annotations (on resource interfaces).
 * @param useKotlinSyntax the flag to generate Kotlin source code.
 * @param useResteasyResponse the flag to use the more typesafe {@code RestResponse} from RESTEasy instead of the normal untyped {@code Response} from Jakarta WS core.
 * @param useOidcClientAnnotation the flag to generate Quarkus OIDC client annotation (on resource interfaces).
 * @param indentWithTab the flag to output indents with the tab character.
 * @param indentSize the number of spaces for each indent level, when not using the tab character.
 * @param verbose the flag to enable verbose logging.
 */
public record Options (
    String outputDir,
    String rootPackage,
    String rootUrlPath,
    String resourceNameSuffix,
    String resourceNameOverride,
    String resourceConfigKeyOverride,
    String resourceClientHeadersFactoryOverride,
    List<String> resourceProvidersOverride,
    String pojoNameSuffix,
    boolean pojosAsRecords,
    List<String> includeTags,
    boolean generateResourceInterfaces,
    boolean generateOpenApiDefClass,
    boolean addJsonPropertyAnnotations,
    boolean addJakartaBeanValidationAnnotations,
    boolean addMpOpenApiAnnotations,
    boolean addMpRestClientAnnotations,
    boolean useKotlinSyntax,
    boolean useResteasyResponse,
    boolean useOidcClientAnnotation,
    boolean indentWithTab,
    int indentSize,
    boolean verbose
) {
    /**
     * Returns the default settings.
     * @return the default settings.
     */
    public static Options defaults() {
        return new Options(
            null,
            null,
            "api",
            "Api",
            "",
            "",
            "",
            null,
            "Dto",
            true,
            null,
            true,
            true,
            true,
            true,
            true,
            true,
            false,
            false,
            false,
            false,
            4,
            false
        );
    }

    private Options with(String outputDir, String rootPackage, String resourceNameSuffix, String resourceNameOverride, String resourceConfigKeyOverride, String resourceClientHeadersFactoryOverride, List<String> resourceProvidersOverride, boolean pojosAsRecords, List<String> includeTags, boolean useKotlinSyntax, boolean useResteasyResponse, boolean useOidcClientAnnotation, boolean verbose) {
        return new Options(
            outputDir,
            rootPackage,
            this.rootUrlPath,
            resourceNameSuffix,
            resourceNameOverride,
            resourceConfigKeyOverride,
            resourceClientHeadersFactoryOverride,
            resourceProvidersOverride,
            this.pojoNameSuffix,
            pojosAsRecords,
            includeTags,
            this.generateResourceInterfaces,
            this.generateOpenApiDefClass,
            this.addJsonPropertyAnnotations,
            this.addJakartaBeanValidationAnnotations,
            this.addMpOpenApiAnnotations,
            this.addMpRestClientAnnotations,
            useKotlinSyntax,
            useResteasyResponse,
            useOidcClientAnnotation,
            this.indentWithTab,
            this.indentSize,
            verbose
        );
    }

    /**
     * Returns a new {@link Options} object with specified output directory.
     * @param outputDir the output directory.
     * @return the new and updated {@link Options} object.
     */
    public Options withOutputDir(String outputDir) {
        return with(outputDir, this.rootPackage, this.resourceNameSuffix,  this.resourceNameOverride, this.resourceConfigKeyOverride, this.resourceClientHeadersFactoryOverride, this.resourceProvidersOverride, this.pojosAsRecords, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.useOidcClientAnnotation, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified root package.
     * @param rootPackage the root package.
     * @return the new and updated {@link Options} object.
     */
    public Options withRootPackage(String rootPackage) {
        return with(this.outputDir, rootPackage, this.resourceNameSuffix, this.resourceNameOverride, this.resourceConfigKeyOverride, this.resourceClientHeadersFactoryOverride, this.resourceProvidersOverride, this.pojosAsRecords, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.useOidcClientAnnotation, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified resource name suffix.
     * @param resourceNameSuffix the resource name suffix.
     * @return the new and updated {@link Options} object.
     */
    public Options withResourceNameSuffix(String resourceNameSuffix) {
        return with(this.outputDir, this.rootPackage, resourceNameSuffix, this.resourceNameOverride, this.resourceConfigKeyOverride, this.resourceClientHeadersFactoryOverride, this.resourceProvidersOverride, this.pojosAsRecords, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.useOidcClientAnnotation, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified resource name override.
     * @param resourceNameOverride the resource name override.
     * @return the new and updated {@link Options} object.
     */
    public Options withResourceNameOverride(String resourceNameOverride) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, resourceNameOverride, this.resourceConfigKeyOverride, this.resourceClientHeadersFactoryOverride, this.resourceProvidersOverride, this.pojosAsRecords, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.useOidcClientAnnotation, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified resource config-key override.
     * @param resourceConfigKeyOverride the resource config-key override.
     * @return the new and updated {@link Options} object.
     */
    public Options withResourceConfigKeyOverride(String resourceConfigKeyOverride) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, this.resourceNameOverride, resourceConfigKeyOverride, this.resourceClientHeadersFactoryOverride, this.resourceProvidersOverride, this.pojosAsRecords, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.useOidcClientAnnotation, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified resource client headers factory override.
     * @param resourceClientHeadersFactoryOverride the resource client headers factory override.
     * @return the new and updated {@link Options} object.
     */
    public Options withResourceClientHeadersFactoryOverride(String resourceClientHeadersFactoryOverride) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, this.resourceNameOverride, this.resourceConfigKeyOverride, resourceClientHeadersFactoryOverride, this.resourceProvidersOverride, this.pojosAsRecords, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.useOidcClientAnnotation, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified resource providers override.
     * @param resourceProvidersOverride the resource providers override.
     * @return the new and updated {@link Options} object.
     */
    public Options withResourceProvidersOverride(List<String> resourceProvidersOverride) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, this.resourceNameOverride, this.resourceConfigKeyOverride, this.resourceClientHeadersFactoryOverride, resourceProvidersOverride, this.pojosAsRecords, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.useOidcClientAnnotation, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified POJOs as records flag.
     * @param pojosAsRecords the POJOs as records.
     * @return the new and updated {@link Options} object.
     */
    public Options withPojosAsRecords(boolean pojosAsRecords) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, this.resourceNameOverride, this.resourceConfigKeyOverride, this.resourceClientHeadersFactoryOverride, resourceProvidersOverride, pojosAsRecords, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.useOidcClientAnnotation, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified included tags.
     * @param includeTags the included tags.
     * @return the new and updated {@link Options} object.
     */
    public Options withIncludeTags(List<String> includeTags) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, this.resourceNameOverride, this.resourceConfigKeyOverride, this.resourceClientHeadersFactoryOverride, this.resourceProvidersOverride, this.pojosAsRecords, includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.useOidcClientAnnotation,  this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified use Kotlin flag.
     * @param useKotlinSyntax the use Kotlin flag.
     * @return the new and updated {@link Options} object.
     */
    public Options withUseKotlinSyntax(boolean useKotlinSyntax) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, this.resourceNameOverride, this.resourceConfigKeyOverride, this.resourceClientHeadersFactoryOverride, this.resourceProvidersOverride, this.pojosAsRecords, this.includeTags, useKotlinSyntax, this.useResteasyResponse, this.useOidcClientAnnotation, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified use RESTEasy flag.
     * @param useResteasyResponse the use RESTEasy flag.
     * @return the new and updated {@link Options} object.
     */
    public Options withUseResteasyResponse(boolean useResteasyResponse) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, this.resourceNameOverride, this.resourceConfigKeyOverride, this.resourceClientHeadersFactoryOverride, this.resourceProvidersOverride, this.pojosAsRecords, this.includeTags, this.useKotlinSyntax, useResteasyResponse, this.useOidcClientAnnotation, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified use OIDC Client annotation flag.
     * @param useOidcClientAnnotation the use OIDC Client annotation flag.
     * @return the new and updated {@link Options} object.
     */
    public Options withUseOidcClientAnnotation(boolean useOidcClientAnnotation) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, this.resourceNameOverride, this.resourceConfigKeyOverride, this.resourceClientHeadersFactoryOverride, this.resourceProvidersOverride, this.pojosAsRecords, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, useOidcClientAnnotation, this.verbose);
    }

    /**
     * Returns a new {@link Options} object with specified verbose flag.
     * @param verbose the verbose flag.
     * @return the new and updated {@link Options} object.
     */
    public Options withVerbose(boolean verbose) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, this.resourceNameOverride, this.resourceConfigKeyOverride, this.resourceClientHeadersFactoryOverride, this.resourceProvidersOverride, this.pojosAsRecords, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.useOidcClientAnnotation, verbose);
    }

    /**
     * Returns a new {@link Options} object with specified model output directory.
     * @param customSubdir the model output directory.
     * @return the new and updated {@link Options} object.
     */
    public String getModelOutputDir(String customSubdir) {
        return outputDir + "/model" + (isBlank(customSubdir) ? "" : "/"+customSubdir);
    }

    /**
     * Returns a new {@link Options} object with specified model package.
     * @param customSubpackage the model subpackage.
     * @return the new and updated {@link Options} object.
     */
    public String getModelPackage(String customSubpackage) {
        return rootPackage + ".model" + (isBlank(customSubpackage) ? "" : "."+customSubpackage);
    }

    /**
     * Gets the language specific code file extension.
     * @return the language specific code file extension.
     */
    public String getFileExtension() {
        return useKotlinSyntax ? ".kt" : ".java";
    }
}
