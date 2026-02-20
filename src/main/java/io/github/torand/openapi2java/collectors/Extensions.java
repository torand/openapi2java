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
    /**
     * Config key used for getting MP Rest Client config.
     */
    public static final String EXT_RESTCLIENT_CONFIGKEY = "x-restclient-configkey";

    /**
     * Map of custom client header names and their values.
     */
    public static final String EXT_RESTCLIENT_HEADERS = "x-restclient-headers";

    /**
     * Fully qualified classname of an MP Rest Client header factory.
     */
    public static final String EXT_RESTCLIENT_HEADERSFACTORY = "x-restclient-headersfactory";

    /**
     * Array of fully qualified classnames of MP Rest Client providers.
     */
    public static final String EXT_RESTCLIENT_PROVIDERS = "x-restclient-providers";

    /**
     * Fully qualified classname of a Jackson JSON serializer class for the schema.
     */
    public static final String EXT_JSON_SERIALIZER = "x-json-serializer";

    /**
     * Custom date/time format pattern for Jackson java.time.* deserializers/serializers.
     */
    public static final String EXT_JSON_FORMAT = "x-json-format";

    /**
     * Fully qualified classname of an annotation class to validate the schema.
     */
    public static final String EXT_VALIDATION_CONSTRAINT = "x-validation-constraint";

    /**
     * If `true` the type of the schema/property can be `null`.
     */
    public static final String EXT_NULLABLE = "x-nullable";

    /**
     * Subdirectory to place the generated DTO model class.
     */
    public static final String EXT_MODEL_SUBDIR = "x-model-subdir";

    /**
     * Describing why something is deprecated, and what to use instead.
     */
    public static final String EXT_DEPRECATION_MESSAGE = "x-deprecation-message";

    private final Map<String, Object> extensionsByName;

    /**
     * Returns an {@link Extensions} object processing the specified OpenAPI extension map.
     * @param extensionsByName the OpenAPI extensions.
     * @return the {@link Extensions} object.
     */
    public static Extensions extensions(Map<String, Object> extensionsByName) {
        return new Extensions(extensionsByName);
    }

    private Extensions(Map<String, Object> extensionsByName) {
        this.extensionsByName = nonNull(extensionsByName) ? extensionsByName : Collections.emptyMap();
    }

    /**
     * Gets value of a string extension property.
     * @param name the extension property name.
     * @return the extension property string value, if found; else empty.
     */
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

    /**
     * Gets value of a string array extension property.
     * @param name the extension property name.
     * @return the extension property value, if found; else empty.
     */
    public Optional<List<String>> getStringArray(String name) {
        Object value = extensionsByName.get(name);
        if (isNull(value)) {
            return Optional.empty();
        }
        if (!(value instanceof List)) {
            throw new OpenApi2JavaException("Value of extension %s is not an array".formatted(name));
        }

        return Optional.of((List<String>)value);
    }

    /**
     * Gets value of a boolean extension property.
     * @param name the extension property name.
     * @return the extension property value, if found; else empty.
     */
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

    /**
     * Gets value of a map extension property.
     * @param name the extension property name.
     * @return the extension property value, if found; else empty.
     */
    public Optional<Map<String, Object>> getMap(String name) {
        Object value = extensionsByName.get(name);
        if (isNull(value)) {
            return Optional.empty();
        }
        if (!(value instanceof Map)) {
            throw new OpenApi2JavaException("Value of extension %s is not a Map (object)".formatted(name));
        }

        return Optional.of((Map<String, Object>)value);
    }
}
