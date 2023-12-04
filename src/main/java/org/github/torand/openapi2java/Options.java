package org.github.torand.openapi2java;

public class Options {
    public String outputDir;
    public String rootPackage;
    public String rootUrlPath = "api";
    public String resourceNameSuffix = "Api";

    public String getModelOutputDir() {
        return outputDir + "/model";
    }

    public String getModelPackage() {
        return rootPackage + ".model";
    }
}
