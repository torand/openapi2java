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
