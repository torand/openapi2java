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
package io.github.torand.openapi2java.collectors;

import io.github.torand.openapi2java.generators.Options;

import java.util.List;
import java.util.Map;

import static io.github.torand.javacommons.lang.StringHelper.*;
import static io.github.torand.openapi2java.utils.KotlinTypeMapper.toKotlinNative;
import static io.github.torand.openapi2java.utils.StringUtils.joinCsv;

/**
 * Base class for all collectors.
 */
public abstract class BaseCollector {

    protected final Options opts;

    protected BaseCollector(Options opts) {
        this.opts = opts;
    }

    protected String normalizeDescription(String description) {
        return nonBlank(description) ? description.replace("%", "%%") : "TBD";
    }

    protected String normalizePath(String path) {
        if (path.startsWith("/")) {
            path = stripHead(path, 1);
        }
        if (path.endsWith("/")) {
            path = stripTail(path, 1);
        }
        return path;
    }

    protected String dirPath2PackagePath(String dirPath) {
        return dirPath.replace("\\/", ".");
    }

    protected String modelName2SchemaName(String modelName) {
        return modelName.replaceFirst(opts.pojoNameSuffix()+"$", "");
    }

    protected String formatClassRef(String className) {
        return opts.useKotlinSyntax()
            ? "%s::class".formatted(toKotlinNative(className))
            : "%s.class".formatted(className);
    }

    protected String formatInnerAnnotation(String annotation, Object... args) {
        return (opts.useKotlinSyntax() ? "" : "@") + annotation.formatted(args);
    }

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

    protected String formatAnnotationNamedParam(List<String> value) {
        if (opts.useKotlinSyntax()) {
            return "[ " + joinCsv(value) + " ]";
        } else {
            return value.size() == 1 ? value.get(0) : "{ " + joinCsv(value) + " }";
        }
    }

    protected String formatDeprecationMessage(Map<String, Object> extensions) {
        return new Extensions(extensions).getString(Extensions.EXT_DEPRECATION_MESSAGE).orElse("Deprecated");
    }
}
