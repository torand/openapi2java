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

import io.swagger.v3.oas.models.OpenAPI;

public class ComponentResolver {
    private final HeaderResolver headers;
    private final ParameterResolver parameters;
    private final ResponseResolver responses;
    private final SchemaResolver schemas;
    private final SecuritySchemeResolver securitySchemes;

    public ComponentResolver(OpenAPI openApiDoc) {
        this.headers = new HeaderResolver(openApiDoc.getComponents().getHeaders());
        this.parameters = new ParameterResolver(openApiDoc.getComponents().getParameters());
        this.responses = new ResponseResolver(openApiDoc.getComponents().getResponses());
        this.schemas = new SchemaResolver(openApiDoc.getComponents().getSchemas());
        this.securitySchemes = new SecuritySchemeResolver(openApiDoc.getComponents().getSecuritySchemes());
    }

    public HeaderResolver headers() {
        return headers;
    }

    public ParameterResolver parameters() {
        return parameters;
    }

    public ResponseResolver responses() {
        return responses;
    }

    public SchemaResolver schemas() {
        return schemas;
    }

    public SecuritySchemeResolver securitySchemes() {
        return securitySchemes;
    }
}
