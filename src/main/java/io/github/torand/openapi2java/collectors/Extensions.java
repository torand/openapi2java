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

import io.github.torand.openapi2java.utils.OpenApi2JavaException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Handles custom OpenAPI format extensions.
 */
public class Extensions {
    public static final String EXT_RESTCLIENT_CONFIGKEY = "x-restclient-configkey";
    public static final String EXT_RESTCLIENT_HEADERS = "x-restclient-headers";
    public static final String EXT_RESTCLIENT_HEADERSFACTORY = "x-restclient-headersfactory";
    public static final String EXT_RESTCLIENT_PROVIDERS = "x-restclient-providers";
    public static final String EXT_JSON_SERIALIZER = "x-json-serializer";
    public static final String EXT_VALIDATION_CONSTRAINT = "x-validation-constraint";
    public static final String EXT_NULLABLE = "x-nullable";
    public static final String EXT_MODEL_SUBDIR = "x-model-subdir";
    public static final String EXT_DEPRECATION_MESSAGE = "x-deprecation-message";

    private final Map<String, Object> extensionsByName;

    public static Extensions extensions(Map<String, Object> extensionsByName) {
        return new Extensions(extensionsByName);
    }

    private Extensions(Map<String, Object> extensionsByName) {
        this.extensionsByName = nonNull(extensionsByName) ? extensionsByName : Collections.emptyMap();
    }

    public Optional<String> getString(String name) {
        Object value = extensionsByName.get(name);
        if (isNull(value)) {
            return Optional.empty();
        }
        if (!(value instanceof String)) {
            throw new OpenApi2JavaException("Value of extension %s is not a String".formatted(name));
        }
        if (nonBlank((String)value)) {
            return Optional.of((String)value);
        }

        return Optional.empty();
    }

    public Optional<List<String>> getStringArray(String name) {
        Object value = extensionsByName.get(name);
        if (isNull(value)) {
            return Optional.empty();
        }
        if (!(value instanceof List)) {
            throw new RuntimeException("Value of extension %s is not an array".formatted(name));
        }

        return Optional.of((List<String>)value);
    }

    public Optional<Boolean> getBoolean(String name) {
        Object value = extensionsByName.get(name);
        if (isNull(value)) {
            return Optional.empty();
        }
        if (!(value instanceof Boolean)) {
            throw new OpenApi2JavaException("Value of extension %s is not a Boolean".formatted(name));
        }

        return Optional.of((Boolean)value);
    }

    public Optional<Map<String, Object>> getMap(String name) {
        Object value = extensionsByName.get(name);
        if (isNull(value)) {
            return Optional.empty();
        }
        if (!(value instanceof Map)) {
            throw new RuntimeException("Value of extension %s is not a Map (object)".formatted(name));
        }

        return Optional.of((Map<String, Object>)value);
    }
}
