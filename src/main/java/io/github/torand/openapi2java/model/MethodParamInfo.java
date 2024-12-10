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
package io.github.torand.openapi2java.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Objects.nonNull;

public class MethodParamInfo {
    public String name;
    public Set<String> imports = new TreeSet<>();
    public Set<String> staticImports = new TreeSet<>();

    public Set<String> annotations = new LinkedHashSet<>();
    public TypeInfo type;
    public String comment;
    public boolean nullable;

    public String deprecationMessage = null;

    public boolean isDeprecated() {
        return nonNull(deprecationMessage);
    }
}
