package io.github.torand.openapi2java.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class MethodParamInfo {
    public String name;
    public Set<String> imports = new TreeSet<>();
    public Set<String> staticImports = new TreeSet<>();

    public Set<String> annotations = new LinkedHashSet<>();
    public TypeInfo type;
    public String comment;
    public boolean nullable;
}
