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
package io.github.torand.openapi2java.utils;

import java.util.List;

import static io.github.torand.javacommons.collection.CollectionHelper.isEmpty;
import static io.github.torand.javacommons.lang.StringHelper.isBlank;

public class StringUtils {
    private StringUtils() {}

    public static String pluralSuffix(int count) {
        return count == 1 ? "" : "s";
    }

    public static String removeLineBreaks(String value) {
        if (isBlank(value)) {
            return value;
        }
        return value.replace("\n", " ");
    }

    public static String joinCsv(List<String> values) {
        if (isEmpty(values)) {
            return "";
        }

        return String.join(", ", values);
    }

    public static String getClassNameFromFqn(String fqn) {
        int lastDot = fqn.lastIndexOf(".");
        if (lastDot == -1) {
            throw new IllegalArgumentException("Unexpected fully qualified class name: %s".formatted(fqn));
        }
        return fqn.substring(lastDot+1);
    }
}
