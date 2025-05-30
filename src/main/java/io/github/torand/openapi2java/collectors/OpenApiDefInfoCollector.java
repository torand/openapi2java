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
import io.github.torand.openapi2java.model.OpenApiDefInfo;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.github.torand.openapi2java.utils.CollectionHelper.nonEmpty;
import static io.github.torand.openapi2java.utils.StringHelper.joinCsv;
import static io.github.torand.openapi2java.utils.StringHelper.nonBlank;
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
        OpenApiDefInfo openApiDefInfo = new OpenApiDefInfo();
        openApiDefInfo.name = name;

        openApiDefInfo.imports.add("jakarta.ws.rs.core.Application");

        if (nonEmpty(securityRequirements)) {
            openApiDefInfo.annotations.add(getSecuritySchemesAnnotation(securityRequirements, openApiDefInfo.imports));
        }

        return openApiDefInfo;
    }

    private String getSecuritySchemesAnnotation(List<SecurityRequirement> securityRequirements, Set<String> imports) {
        imports.add("org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes");

        List<String> securitySchemeAnnotations = new ArrayList<>();
        securityRequirements.forEach(sr -> {
            sr.keySet().forEach(schemeName -> {
                securitySchemeAnnotations.add(getSecuritySchemeAnnotation(schemeName, imports));
            });
        });

        return "@SecuritySchemes(%s)".formatted(formatAnnotationDefaultParam(securitySchemeAnnotations));
    }

    private String getSecuritySchemeAnnotation(String name, Set<String> imports) {
        SecurityScheme securityScheme = componentResolver.securitySchemes().getOrThrow(name);

        List<String> params = new ArrayList<>();
        params.add("securitySchemeName = \"%s\"".formatted(name));

        if (nonBlank(securityScheme.getDescription())) {
            params.add("description = \"%s\"".formatted(normalizeDescription(securityScheme.getDescription())));
        }

        imports.add("org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType");
        params.add("type = SecuritySchemeType.%s".formatted(securityScheme.getType().name()));

        switch (securityScheme.getType()) {
            case APIKEY -> {
                params.add("name = \"%s\"".formatted(securityScheme.getName()));
            }
            case HTTP -> {
                params.add("scheme = \"%s\"".formatted(securityScheme.getScheme()));
                if (nonNull(securityScheme.getBearerFormat())) {
                    params.add("bearerFormat = \"%s\"".formatted(securityScheme.getBearerFormat()));
                }
            }
            case OAUTH2 -> {
                if (nonNull(securityScheme.getFlows())) {
                    params.add("flows = %s".formatted(getOAuthFlowsAnnotation(securityScheme.getFlows(), imports)));
                }
            }
            case OPENIDCONNECT -> {
                params.add("openIdConnectUrl = \"%s\"".formatted(securityScheme.getOpenIdConnectUrl()));
            }
            case MUTUALTLS -> {
                throw new IllegalStateException("Security scheme MUTUALTLS not supported");
            }
        }

        imports.add("org.eclipse.microprofile.openapi.annotations.security.SecurityScheme");
        return (opts.useKotlinSyntax ? "" : "@") + "SecurityScheme(%s)".formatted(joinCsv(params));
    }

    private String getOAuthFlowsAnnotation(OAuthFlows flows, Set<String> imports) {
        List<String> params = new ArrayList<>();

        if (nonNull(flows.getAuthorizationCode())) {
            params.add("authorizationCode = %s".formatted(getOAuthFlowAnnotation(flows.getAuthorizationCode(), imports)));
        }
        if (nonNull(flows.getImplicit())) {
            params.add("implicit = %s".formatted(getOAuthFlowAnnotation(flows.getImplicit(), imports)));
        }
        if (nonNull(flows.getClientCredentials())) {
            params.add("clientCredentials = %s".formatted(getOAuthFlowAnnotation(flows.getClientCredentials(), imports)));
        }
        if (nonNull(flows.getPassword())) {
            params.add("password = %s".formatted(getOAuthFlowAnnotation(flows.getPassword(), imports)));
        }

        imports.add("org.eclipse.microprofile.openapi.annotations.security.OAuthFlows");
        return (opts.useKotlinSyntax ? "" : "@") + "OAuthFlows(%s)".formatted(joinCsv(params));
    }

    private String getOAuthFlowAnnotation(OAuthFlow flow, Set<String> imports) {
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
            params.add("scopes = %s".formatted(getScopesAnnotation(flow.getScopes(), imports)));
        }

        imports.add("org.eclipse.microprofile.openapi.annotations.security.OAuthFlow");
        return (opts.useKotlinSyntax ? "" : "@") + "OAuthFlow(%s)".formatted(joinCsv(params));
    }

    private String getScopesAnnotation(Scopes scopes, Set<String> imports) {
        List<String> scopeAnnotations = scopes.keySet().stream()
            .map(name -> getScopeAnnotation(name, scopes.get(name), imports))
            .toList();

        return formatAnnotationNamedParam(scopeAnnotations);
    }

    private String getScopeAnnotation(String name, String description, Set<String> imports) {
        imports.add("org.eclipse.microprofile.openapi.annotations.security.OAuthScope");
        return (opts.useKotlinSyntax ? "" : "@") + "OAuthScope(name = \"%s\", description = \"%s\")".formatted(name, normalizeDescription(description));
    }
}
