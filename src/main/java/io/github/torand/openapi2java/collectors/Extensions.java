/*
 * Copyright (c) 2024 Tore Eide Andersen
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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static io.github.torand.openapi2java.utils.StringHelper.nonBlank;

public class Extensions {
    public static final String EXT_RESTCLIENT_CONFIGKEY = "x-restclient-configkey";
    public static final String EXT_JSON_SERIALIZER = "x-json-serializer";
    public static final String EXT_VALIDATION_CONSTRAINT = "x-validation-constraint";
    public static final String EXT_NULLABLE = "x-nullable";
    public static final String EXT_MODEL_SUBDIR = "x-model-subdir";
    public static final String EXT_DEPRECATION_MESSAGE = "x-deprecation-message";

    private final Map<String, Object> extensions;

    public static Extensions extensions(Map<String, Object> extensions) {
        return new Extensions(extensions);
    }

    public Extensions(Map<String, Object> extensions) {
        this.extensions = nonNull(extensions) ? extensions : Collections.emptyMap();
    }

    public Optional<String> getString(String name) {
        Object value = extensions.get(name);
        if (isNull(value)) {
            return Optional.empty();
        }
        if (!(value instanceof String)) {
            throw new RuntimeException("Value of extension %s is not a String".formatted(name));
        }
        if (nonBlank((String)value)) {
            return Optional.of((String)value);
        }

        return Optional.empty();
    }

    public Optional<Boolean> getBoolean(String name) {
        Object value = extensions.get(name);
        if (isNull(value)) {
            return Optional.empty();
        }
        if (!(value instanceof Boolean)) {
            throw new RuntimeException("Value of extension %s is not a Boolean".formatted(name));
        }

        return Optional.of((Boolean)value);
    }
}
