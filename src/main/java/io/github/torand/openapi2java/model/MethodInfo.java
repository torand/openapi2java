/*
 * Copyright (c) 2024-2025 Tore Eide Andersen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.torand.openapi2java.model;

import java.util.*;

import static java.util.Objects.nonNull;

/**
 * Describes a method.
 * @param name the method name.
 * @param parameters the method parameters.
 * @param returnType the method return type.
 * @param deprecationMessage the deprecation message, if any.
 * @param imports the imports required by the method annotations and return type.
 * @param staticImports the static imports required by the method annotations and return type.
 * @param annotations the annotations decorating this method.
 */
public record MethodInfo (
    String name,
    List<MethodParamInfo> parameters,
    String returnType,
    String deprecationMessage,
    Set<String> imports,
    Set<String> staticImports,
    Set<String> annotations
) {
    /**
     * Constructs a {@link MethodInfo} object.
     * @param name the meethod name.
     */
    public MethodInfo(String name) {
        this(name, new ArrayList<>(), null, null, new TreeSet<>(), new TreeSet<>(), new LinkedHashSet<>());
    }

    public MethodInfo withAddedParameters(Collection<MethodParamInfo> params) {
        List<MethodParamInfo> newParameters = new ArrayList<>(parameters);
        newParameters.addAll(params);
        return new MethodInfo(name, newParameters, returnType, deprecationMessage, imports, staticImports, annotations);
    }

    public MethodInfo withReturnType(String returnType) {
        return new MethodInfo(name, parameters, returnType, deprecationMessage, imports, staticImports, annotations);
    }

    public MethodInfo withDeprecationMessage(String deprecationMessage) {
        return new MethodInfo(name, parameters, returnType, deprecationMessage, imports, staticImports, annotations);
    }

    /**
     * Returns a new {@link MethodInfo} object with specified annotation added.
     * @param annotation the annotation to add.
     * @return the new and updated {@link MethodInfo} object.
     */
    public MethodInfo withAddedAnnotation(AnnotationInfo annotation) {
        Set<String> newImports = new TreeSet<>(this.imports);
        newImports.addAll(annotation.imports());
        Set<String> newStaticImports = new TreeSet<>(this.staticImports);
        newStaticImports.addAll(annotation.staticImports());
        Set<String> newAnnotations = new LinkedHashSet<>(this.annotations);
        newAnnotations.add(annotation.annotation());
        return new MethodInfo(name, parameters, returnType, deprecationMessage, newImports, newStaticImports, newAnnotations);
    }

    /**
     * Returns a new {@link MethodInfo} object with specified annotations added.
     * @param annotations the annotations to add.
     * @return the new and updated {@link MethodInfo} object.
     */
    public MethodInfo withAddedAnnotations(Collection<AnnotationInfo> annotations) {
        MethodInfo merged = this;
        for (AnnotationInfo annotation : annotations) {
            merged = merged.withAddedAnnotation(annotation);
        }
        return merged;
    }

    /**
     * Indicates whether the method is deprecated.
     * @return true if method is deprecated; else false.
     */
    public boolean isDeprecated() {
        return nonNull(deprecationMessage);
    }
}
