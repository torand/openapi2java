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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;

/**
 * Resolves all components referenced in an OpenAPI specification.
 */
public class ComponentResolver {
    private final HeaderResolver headers;
    private final ParameterResolver parameters;
    private final ResponseResolver responses;
    private final SchemaResolver schemas;
    private final SecuritySchemeResolver securitySchemes;

    /**
     * Constructs a {@link ComponentResolver} object.
     * @param openApiDoc the OpenAPI document.
     */
    public ComponentResolver(OpenAPI openApiDoc) {
        this.headers = new HeaderResolver(openApiDoc.getComponents().getHeaders());
        this.parameters = new ParameterResolver(openApiDoc.getComponents().getParameters());
        this.responses = new ResponseResolver(openApiDoc.getComponents().getResponses());
        this.schemas = new SchemaResolver((Map<String, Schema<?>>)(Object)openApiDoc.getComponents().getSchemas());
        this.securitySchemes = new SecuritySchemeResolver(openApiDoc.getComponents().getSecuritySchemes());
    }

    /**
     * Gets a header resolver.
     * @return the header resolver.
     */
    public HeaderResolver headers() {
        return headers;
    }

    /**
     * Gets a parameter resolver.
     * @return the parameter resolver.
     */
    public ParameterResolver parameters() {
        return parameters;
    }

    /**
     * Gets a response resolver.
     * @return the response resolver.
     */
    public ResponseResolver responses() {
        return responses;
    }

    /**
     * Gets a schema resolver.
     * @return the schema resolver.
     */
    public SchemaResolver schemas() {
        return schemas;
    }

    /**
     * Gets a security scheme resolver.
     * @return the security scheme resolver.
     */
    public SecuritySchemeResolver securitySchemes() {
        return securitySchemes;
    }
}
