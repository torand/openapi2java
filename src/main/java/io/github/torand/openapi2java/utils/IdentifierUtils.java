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
package io.github.torand.openapi2java.utils;

import static io.github.torand.javacommons.contract.Requires.requireNonBlank;

/**
 * A collection of Java identifier related utilities.
 */
public class IdentifierUtils {
    private IdentifierUtils() {}

    /**
     * Transforms specified operation id, parameter name or property name into a valid Java identifier.
     * @param str the id or name to transform
     * @return a valid Java identifier
     */
    public static String toJavaIdentifier(String str) {
        requireNonBlank(str, "No string specified");

        StringBuilder sb = new StringBuilder(str.length());
        int i = 0;
        int cp;
        boolean capitalizeNext = false;

        // Skip leading characters that cannot start a Java identifier.
        while (i < str.length()) {
            cp = str.codePointAt(i);
            i += Character.charCount(cp);
            if (Character.isWhitespace(cp)) {
                // Leading whitespace: ignore, but remember to capitalize the
                // next valid character (only relevant if it's also a valid part
                // but not a valid start — in practice the first kept char stays
                // as-is here because we need a start char anyway).
                continue;
            }
            if (Character.isJavaIdentifierStart(cp)) {
                cp = Character.toLowerCase(cp);
                sb.appendCodePoint(cp);
                break;
            }
        }

        // Keep only characters allowed inside a Java identifier;
        // upper-case the first valid char after any run of whitespace.
        while (i < str.length()) {
            cp = str.codePointAt(i);
            i += Character.charCount(cp);

            if (Character.isWhitespace(cp) || cp == '-') {
                capitalizeNext = true;
                continue;
            }
            if (Character.isJavaIdentifierPart(cp)) {
                if (capitalizeNext) {
                    cp = Character.toUpperCase(cp);
                    capitalizeNext = false;
                }
                sb.appendCodePoint(cp);
            }
        }

        return sb.toString();
    }
}
