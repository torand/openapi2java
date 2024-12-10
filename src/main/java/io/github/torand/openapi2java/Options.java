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

import java.util.List;

import static io.github.torand.openapi2java.utils.StringHelper.isBlank;

public class Options {
    public String outputDir;
    public String rootPackage;
    public String rootUrlPath = "api";
    public String resourceNameSuffix = "Api";
    public String pojoNameSuffix = "Dto";
    public boolean pojosAsRecords = true;
    public List<String> includeTags;
    public boolean generateResourceClasses = true;
    public boolean generateOpenApiDefClass = true;
    public boolean addJsonPropertyAnnotations = false;
    public boolean addJakartaBeanValidationAnnotations = true;
    public boolean addMpOpenApiAnnotations = true;
    public boolean addMpRestClientAnnotations = true;
    public boolean useKotlinSyntax = false;
    public boolean useResteasyResponse = false;
    public boolean indentWithTab = false;
    public int indentSize = 4;
    public boolean verbose = false;

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
