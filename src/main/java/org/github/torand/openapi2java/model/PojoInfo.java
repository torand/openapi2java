package org.github.torand.openapi2java.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class PojoInfo {
    public Set<String> imports = new TreeSet<>();
    public Set<String> staticImports = new TreeSet<>();
    public Set<String> annotations = new LinkedHashSet<>();

    public List<PropertyInfo> properties = new ArrayList<>();

    public boolean isEmpty() {
        return properties.isEmpty();
    }
}
