package io.github.torand.openapi2java.model;

import io.github.torand.openapi2java.utils.CollectionHelper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class PojoInfo {
    public String name;

    public Set<String> imports = new TreeSet<>();
    public Set<String> annotations = new LinkedHashSet<>();

    public List<PropertyInfo> properties = new ArrayList<>();

    public boolean isEmpty() {
        return CollectionHelper.isEmpty(properties);
    }
}
