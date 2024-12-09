package io.github.torand.openapi2java.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class EnumInfo {
    public String name;

    public Set<String> imports = new TreeSet<>();
    public Set<String> annotations = new LinkedHashSet<>();
    public List<String> constants = new ArrayList<>();
}
