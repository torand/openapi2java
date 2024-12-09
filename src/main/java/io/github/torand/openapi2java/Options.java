package io.github.torand.openapi2java;

import java.util.List;

public class Options {
    public String outputDir;
    public String rootPackage;
    public String rootUrlPath = "api";
    public String resourceNameSuffix = "Api";
    public String pojoNameSuffix = "Dto";
    public boolean pojosAsRecords = true;
    public List<String> includeTags;
    public boolean useKotlinSyntax = false;
    public boolean verbose = false;

    public String getModelOutputDir() {
        return outputDir + "/model";
    }

    public String getModelPackage() {
        return rootPackage + ".model";
    }

    public String getFileExtension() {
        return useKotlinSyntax ? ".kt" : ".java";
    }
}
