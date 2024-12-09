package io.github.torand.openapi2java.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class TypeInfo {
    public String name;
    public String description;
    public boolean nullable;
    public TypeInfo itemType;
    public String schemaFormat;
    public String schemaPattern;
    public List<String> annotations = new ArrayList<>();
    public List<String> annotationImports = new ArrayList<>();
    public List<String> typeImports = new ArrayList<>();

    public String getFullName() {
        if (nonNull(itemType)) {
            return name + "<" + itemType.getFullName() + ">";
        } else {
            return name;
        }
    }

    public Stream<String> typeImports() {
        return isNull(itemType) ? typeImports.stream() : Stream.concat(typeImports.stream(), itemType.typeImports.stream());
    }

    public Stream<String> annotationImports() {
        return isNull(itemType) ? annotationImports.stream() : Stream.concat(annotationImports.stream(), itemType.annotationImports.stream());
    }
}