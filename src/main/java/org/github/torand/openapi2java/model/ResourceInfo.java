package org.github.torand.openapi2java.model;

import org.github.torand.openapi2java.utils.CollectionHelper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ResourceInfo {
    public String name;

    public Set<String> imports = new TreeSet<>();
    public Set<String> staticImports = new TreeSet<>();
    public Set<String> annotations = new LinkedHashSet<>();

    public List<MethodInfo> methods = new ArrayList<>();

    public MethodInfo authMethod = null;

    public boolean isEmpty() {
        return CollectionHelper.isEmpty(methods);
    }
}
