package io.github.torand.openapi2java.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class OpenApiDefInfo {
    public String name;

    public Set<String> imports = new TreeSet<>();

    public Set<String> annotations = new LinkedHashSet<>();
}
