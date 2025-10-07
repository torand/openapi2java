package io.github.torand.openapi2java.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.github.torand.javacommons.collection.CollectionHelper.tailOf;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.requireNonEmpty;

/**
 * Describes a full type name including its bean validation annotations.
 */
public class AnnotatedTypeName {
    private final List<String> annotatedTypeNameItems;

    /**
     * Constructs an {@link AnnotatedTypeName} object.
     * Note! Any bean validation annotations on subtypes (key or item) of a compound type is expected
     * to be embedded into the type name.
     * @param annotatedTypeNameItems the annotation and type name items.
     */
    public AnnotatedTypeName(List<String> annotatedTypeNameItems) {
        requireNonEmpty(annotatedTypeNameItems);
        this.annotatedTypeNameItems = new ArrayList<>(annotatedTypeNameItems);
    }

    /**
     * Returns whether the main type has any bean validation annotations.
     * @return true if the main type has any bean validation annotations; else false.
     */
    public boolean hasAnnotations() {
        return annotatedTypeNameItems.size() >= 2;
    }

    /**
     * Gets the main type bean validation annotations.
     * @return the main type bean validation annotations.
     */
    public Stream<String> annotations() {
        if (!hasAnnotations()) {
            return Stream.empty();
        }
        return annotatedTypeNameItems.stream().limit(annotatedTypeNameItems.size()-1L);
    }

    /**
     * Gets the main type bean validation annotations combined into a space delimited string.
     * @return the main type bean validation annotations as a single string.
     */
    public String annotationsAsString() {
        return annotations().collect(joining(" "));
    }

    /**
     * Gets the full type name including any bean validation annotations in subtypes.
     * @return the full type name.
     */
    public String typeName() {
        return tailOf(annotatedTypeNameItems);
    }

    /**
     * Gets the items of the annotated type name combined into a space delimited string.
     * @return the complete annotated type name as a single string.
     */
    public String asString() {
        return String.join(" ", annotatedTypeNameItems);
    }

    /**
     * Gets the annotation and type name items.
     * @return the annotation and type name items.
     */
    public Stream<String> items() {
        return annotatedTypeNameItems.stream();
    }
}
