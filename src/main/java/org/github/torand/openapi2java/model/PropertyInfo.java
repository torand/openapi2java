package org.github.torand.openapi2java.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class PropertyInfo {
    public String name;
    public TypeInfo type = new TypeInfo();

    public Set<String> imports = new TreeSet<>();
    public Set<String> annotations = new LinkedHashSet<>();
}
