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

import io.github.torand.openapi2java.utils.CollectionHelper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ResourceInfo {
    public String name;

    public Set<String> imports = new TreeSet<>();
    public Set<String> staticImports = new TreeSet<>();
    public Set<String> annotations = new LinkedHashSet<>();

    public List<MethodInfo> methods = new ArrayList<>();

    public MethodInfo authMethod = null;

    public boolean isEmpty() {
        return CollectionHelper.isEmpty(methods);
    }
}
