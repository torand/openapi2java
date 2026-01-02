/*
 * Copyright (c) 2024-2026 Tore Eide Andersen
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
package io.github.torand.openapi2java.collectors;

import io.github.torand.openapi2java.generators.Options;
import io.github.torand.openapi2java.model.ConstantValue;

import java.util.List;
import java.util.Map;

import static io.github.torand.javacommons.lang.StringHelper.*;
import static io.github.torand.openapi2java.collectors.Extensions.extensions;
import static io.github.torand.openapi2java.utils.KotlinTypeMapper.toKotlinNative;
import static io.github.torand.openapi2java.utils.StringUtils.joinCsv;
import static java.util.Objects.nonNull;

/**
 * Base class for all collectors.
 */
public abstract class BaseCollector {

    /**
     * The plugin options.
     */
    protected final Options opts;

    /**
     * Constructs a {@link BaseCollector} object.
     * @param opts the plugin options.
     */
    protected BaseCollector(Options opts) {
        this.opts = opts;
    }

    /**
     * Modifies the description text for further use in code generation.
     * @param description the original description text.
     * @return the normalized description.
     */
    protected String normalizeDescription(String description) {
        return nonBlank(description) ? description.replace("%", "%%") : "TBD";
    }

    /**
     * Modifies the path string for further use in code generation.
     * @param path the original path string.
     * @return the normalized path.
     */
    protected String normalizePath(String path) {
        if (path.startsWith("/")) {
            path = stripHead(path, 1);
        }
        if (path.endsWith("/")) {
            path = stripTail(path, 1);
        }
        return path;
    }

    /**
     * Converts a directory path into a package path.
     * @param dirPath the directory path.
     * @return the package path.
     */
    protected String dirPath2PackagePath(String dirPath) {
        return dirPath.replace("/", ".");
    }

    /**
     * Converts a model (pojo) name into a schema name.
     * @param modelName the model name.
     * @return the schema name.
     */
    protected String modelName2SchemaName(String modelName) {
        return modelName.replaceFirst(opts.pojoNameSuffix()+"$", "");
    }

    /**
     * Formats given class name to language specific class reference.
     * @param className the class name.
     * @return the class reference.
     */
    protected String formatClassRef(String className) {
        return opts.useKotlinSyntax()
            ? "%s::class".formatted(toKotlinNative(className))
            : "%s.class".formatted(className);
    }

    /**
     * Formats an annotation for use as parameter value for an outer annotation.
     * @param annotation the inner annotation.
     * @param args annotation string arguments.
     * @return the formatted annotation.
     */
    protected String formatInnerAnnotation(String annotation, Object... args) {
        return (opts.useKotlinSyntax() ? "" : "@") + annotation.formatted(args);
    }

    /**
     * Formats a default parameter value for an annotation.
     * @param value the parameter value
     * @return the formatted parameter.
     */
    protected String formatAnnotationDefaultParam(List<String> value) {
        if (value.size() == 1) {
            return value.get(0);
        }
        if (opts.useKotlinSyntax()) {
            return joinCsv(value);
        } else {
            return "{" + joinCsv(value) + "}";
        }
    }

    /**
     * Formats a named parameter for an annotation.
     * @param value the parameter value.
     * @return the formatted parameter.
     */
    protected String formatAnnotationNamedParam(List<String> value) {
        if (opts.useKotlinSyntax()) {
            return "[ " + joinCsv(value) + " ]";
        } else {
            return value.size() == 1 ? value.get(0) : "{ " + joinCsv(value) + " }";
        }
    }

    /**
     * Formats a deprecation message.
     * @param extensionsByName the OpenAPI extensions containing a custom deprecation message, or not.
     * @return the formatted deprecation message.
     */
    protected String formatDeprecationMessage(Map<String, Object> extensionsByName) {
        return extensions(extensionsByName).getString(Extensions.EXT_DEPRECATION_MESSAGE).orElse("Deprecated");
    }

    /**
     * Gets header name constant from specified header name.
     * @param name the header name.
     * @return the header name constant.
     */
    protected ConstantValue getHeaderNameConstant(String name) {
        String standardHeaderConstant = switch (name.toUpperCase()) {
            case "ACCEPT-LANGUAGE" -> "ACCEPT_LANGUAGE";
            case "AUTHORIZATION" -> "AUTHORIZATION";
            case "CONTENT-LANGUAGE" -> "CONTENT_LANGUAGE";
            case "LOCATION" -> "LOCATION";
            default -> null;
        };

        if (nonNull(standardHeaderConstant)) {
            return new ConstantValue(standardHeaderConstant).withStaticImport("jakarta.ws.rs.core.HttpHeaders." + standardHeaderConstant);
        }

        return new ConstantValue(quote(name));
    }
}
