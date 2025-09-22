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

import io.github.torand.openapi2java.generators.Options;
import io.github.torand.openapi2java.model.AnnotationInfo;
import io.github.torand.openapi2java.model.ImportInfo;
import io.github.torand.openapi2java.model.OpenApiDefInfo;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.ArrayList;
import java.util.List;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static io.github.torand.openapi2java.utils.StringUtils.joinCsv;
import static java.util.Objects.nonNull;

/**
 * Collects information about an OpenAPI definition from a collection of security requirements.
 */
public class OpenApiDefInfoCollector extends BaseCollector {
    private final ComponentResolver componentResolver;

    public OpenApiDefInfoCollector(ComponentResolver componentResolver, Options opts) {
        super(opts);
        this.componentResolver = componentResolver;
    }

    public OpenApiDefInfo getOpenApiDefInfo(String name, List<SecurityRequirement> securityRequirements) {
        OpenApiDefInfo openApiDefInfo = new OpenApiDefInfo(name)
            .withAddedImports("jakarta.ws.rs.core.Application");

        if (nonEmpty(securityRequirements)) {
            openApiDefInfo = openApiDefInfo.withAddedAnnotation(getSecuritySchemesAnnotation(securityRequirements));
        }

        return openApiDefInfo;
    }

    private AnnotationInfo getSecuritySchemesAnnotation(List<SecurityRequirement> securityRequirements) {
        List<AnnotationInfo> securitySchemeAnnotations = new ArrayList<>();
        securityRequirements.forEach(sr -> {
            sr.keySet().forEach(schemeName -> {
                securitySchemeAnnotations.add(getSecuritySchemeAnnotation(schemeName));
            });
        });

        return new AnnotationInfo(
            "@SecuritySchemes(%s)".formatted(formatAnnotationDefaultParam(securitySchemeAnnotations.stream().map(AnnotationInfo::annotation).toList())),
            "org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes"
        ).withAddedImports(securitySchemeAnnotations);
    }

    private AnnotationInfo getSecuritySchemeAnnotation(String name) {
        SecurityScheme securityScheme = componentResolver.securitySchemes().getOrThrow(name);

        ImportInfo imports = new ImportInfo();
        List<String> params = new ArrayList<>();

        params.add("securitySchemeName = \"%s\"".formatted(name));

        if (nonBlank(securityScheme.getDescription())) {
            params.add("description = \"%s\"".formatted(normalizeDescription(securityScheme.getDescription())));
        }

        imports = imports.withAddedNormalImport("org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType");
        params.add("type = SecuritySchemeType.%s".formatted(securityScheme.getType().name()));

        switch (securityScheme.getType()) {
            case APIKEY ->
                params.add("name = \"%s\"".formatted(securityScheme.getName()));

            case HTTP -> {
                params.add("scheme = \"%s\"".formatted(securityScheme.getScheme()));
                if (nonNull(securityScheme.getBearerFormat())) {
                    params.add("bearerFormat = \"%s\"".formatted(securityScheme.getBearerFormat()));
                }
            }
            case OAUTH2 -> {
                if (nonNull(securityScheme.getFlows())) {
                    AnnotationInfo flowsAnnotation = getOAuthFlowsAnnotation(securityScheme.getFlows());
                    imports = imports.withAddedImports(flowsAnnotation);
                    params.add("flows = %s".formatted(flowsAnnotation.annotation()));
                }
            }
            case OPENIDCONNECT ->
                params.add("openIdConnectUrl = \"%s\"".formatted(securityScheme.getOpenIdConnectUrl()));

            case MUTUALTLS ->
                throw new IllegalStateException("Security scheme MUTUALTLS not supported");
        }

        return new AnnotationInfo(
            (opts.useKotlinSyntax() ? "" : "@") + "SecurityScheme(%s)".formatted(joinCsv(params)),
            "org.eclipse.microprofile.openapi.annotations.security.SecurityScheme"
        ).withAddedImports(imports);
    }

    private AnnotationInfo getOAuthFlowsAnnotation(OAuthFlows flows) {
        ImportInfo imports = new ImportInfo();
        List<String> params = new ArrayList<>();

        if (nonNull(flows.getAuthorizationCode())) {
            AnnotationInfo flowAnnotation = getOAuthFlowAnnotation(flows.getAuthorizationCode());
            imports = imports.withAddedImports(flowAnnotation);
            params.add("authorizationCode = %s".formatted(flowAnnotation.annotation()));
        }
        if (nonNull(flows.getImplicit())) {
            AnnotationInfo flowAnnotation = getOAuthFlowAnnotation(flows.getImplicit());
            imports = imports.withAddedImports(flowAnnotation);
            params.add("implicit = %s".formatted(flowAnnotation.annotation()));
        }
        if (nonNull(flows.getClientCredentials())) {
            AnnotationInfo flowAnnotation = getOAuthFlowAnnotation(flows.getClientCredentials());
            imports = imports.withAddedImports(flowAnnotation);
            params.add("clientCredentials = %s".formatted(flowAnnotation.annotation()));
        }
        if (nonNull(flows.getPassword())) {
            AnnotationInfo flowAnnotation = getOAuthFlowAnnotation(flows.getPassword());
            imports = imports.withAddedImports(flowAnnotation);
            params.add("password = %s".formatted(flowAnnotation.annotation()));
        }

        return new AnnotationInfo(
            (opts.useKotlinSyntax() ? "" : "@") + "OAuthFlows(%s)".formatted(joinCsv(params)),
            "org.eclipse.microprofile.openapi.annotations.security.OAuthFlows"
        ).withAddedImports(imports);
    }

    private AnnotationInfo getOAuthFlowAnnotation(OAuthFlow flow) {
        ImportInfo imports = new ImportInfo();
        List<String> params = new ArrayList<>();

        if (nonBlank(flow.getAuthorizationUrl())) {
            params.add("authorizationUrl = \"%s\"".formatted(flow.getAuthorizationUrl()));
        }
        if (nonBlank(flow.getTokenUrl())) {
            params.add("tokenUrl = \"%s\"".formatted(flow.getTokenUrl()));
        }
        if (nonBlank(flow.getRefreshUrl())) {
            params.add("refreshUrl = \"%s\"".formatted(flow.getRefreshUrl()));
        }
        if (nonEmpty(flow.getScopes())) {
            List<AnnotationInfo> scopesAnnotations = getScopesAnnotations(flow.getScopes());
            imports = imports.withAddedImports(ImportInfo.concatImports(scopesAnnotations));
            params.add("scopes = %s".formatted(formatAnnotationNamedParam(scopesAnnotations.stream().map(AnnotationInfo::annotation).toList())));
        }

        return new AnnotationInfo(
            (opts.useKotlinSyntax() ? "" : "@") + "OAuthFlow(%s)".formatted(joinCsv(params)),
            "org.eclipse.microprofile.openapi.annotations.security.OAuthFlow"
        ).withAddedImports(imports);
    }

    private List<AnnotationInfo> getScopesAnnotations(Scopes scopes) {
        return scopes.keySet().stream()
            .map(name -> getScopeAnnotation(name, scopes.get(name)))
            .toList();
    }

    private AnnotationInfo getScopeAnnotation(String name, String description) {;
        return new AnnotationInfo(
            (opts.useKotlinSyntax() ? "" : "@") + "OAuthScope(name = \"%s\", description = \"%s\")".formatted(name, normalizeDescription(description)),
            "org.eclipse.microprofile.openapi.annotations.security.OAuthScope"
        );
    }
}
