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
    boolean indentWithTab,
    int indentSize,
    boolean verbose
) {
    public static Options defaults() {
        return new Options(
            null,
            null,
            "api",
            "Api",
            "",
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
            4,
            false
        );
    }

    private Options with(String outputDir, String rootPackage, String resourceNameSuffix, String resourceNameOverride, List<String> includeTags, boolean useKotlinSyntax, boolean useResteasyResponse, boolean verbose) {
        return new Options(
            outputDir,
            rootPackage,
            this.rootUrlPath,
            resourceNameSuffix,
            resourceNameOverride,
            this.pojoNameSuffix,
            this.pojosAsRecords,
            includeTags,
            this.generateResourceInterfaces,
            this.generateOpenApiDefClass,
            this.addJsonPropertyAnnotations,
            this.addJakartaBeanValidationAnnotations,
            this.addMpOpenApiAnnotations,
            this.addMpRestClientAnnotations,
            useKotlinSyntax,
            useResteasyResponse,
            this.indentWithTab,
            this.indentSize,
            verbose
        );
    }

    public Options withOutputDir(String outputDir) {
        return with(outputDir, this.rootPackage, this.resourceNameSuffix,  this.resourceNameOverride, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.verbose);
    }

    public Options withRootPackage(String rootPackage) {
        return with(this.outputDir, rootPackage, this.resourceNameSuffix, this.resourceNameOverride, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.verbose);
    }

    public Options withResourceNameSuffix(String resourceNameSuffix) {
        return with(this.outputDir, this.rootPackage, resourceNameSuffix, this.resourceNameOverride, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.verbose);
    }

    public Options withResourceNameOverride(String resourceNameOverride) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, resourceNameOverride, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, this.verbose);
    }

    public Options withIncludeTags(List<String> includeTags) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, this.resourceNameOverride, includeTags, this.useKotlinSyntax, this.useResteasyResponse,  this.verbose);
    }

    public Options withUseKotlinSyntax(boolean useKotlinSyntax) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, this.resourceNameOverride, this.includeTags, useKotlinSyntax, this.useResteasyResponse, this.verbose);
    }

    public Options withUseResteasyResponse(boolean useResteasyResponse) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, this.resourceNameOverride, this.includeTags, this.useKotlinSyntax, useResteasyResponse, this.verbose);
    }

    public Options withVerbose(boolean verbose) {
        return with(this.outputDir, this.rootPackage, this.resourceNameSuffix, this.resourceNameOverride, this.includeTags, this.useKotlinSyntax, this.useResteasyResponse, verbose);
    }

    public String getModelOutputDir(String customSubdir) {
        return outputDir + "/model" + (isBlank(customSubdir) ? "" : "/"+customSubdir);
    }

    public String getModelPackage(String customSubpackage) {
        return rootPackage + ".model" + (isBlank(customSubpackage) ? "" : "."+customSubpackage);
    }

    public String getFileExtension() {
        return useKotlinSyntax ? ".kt" : ".java";
    }
}
