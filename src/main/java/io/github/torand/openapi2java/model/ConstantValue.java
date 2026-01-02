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
package io.github.torand.openapi2java.model;

/**
 * Describes a constant value.
 * @param value the constant value.
 * @param imports the imports required by the constant value, if any.
 */
public record ConstantValue (
    String value,
    ImportInfo imports
) implements ImportsSupplier {
    /**
     * Constructs an {@link ConstantValue} object.
     * @param value the constant value.
     */
    public ConstantValue(String value) {
        this(value, ImportInfo.empty());
    }

    /**
     * Returns a new {@link ConstantValue} object with specified static import added.
     * @param staticImport the static import to add.
     * @return the new and updated {@link ConstantValue} object.
     */
    public ConstantValue withStaticImport(String staticImport) {
        return new ConstantValue(value, imports.withAddedStaticImport(staticImport));
    }
}
