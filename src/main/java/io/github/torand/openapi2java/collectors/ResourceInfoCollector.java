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
package io.github.torand.openapi2java.collectors;

import io.github.torand.openapi2java.generators.Options;
import io.github.torand.openapi2java.model.*;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.tags.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.torand.javacommons.collection.CollectionHelper.isEmpty;
import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.lang.StringHelper.isBlank;
import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static io.github.torand.openapi2java.collectors.Extensions.*;
import static io.github.torand.openapi2java.utils.StringUtils.getClassNameFromFqn;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Collects information about a resource from a collection of path items.
 */
public class ResourceInfoCollector extends BaseCollector {
    private final MethodInfoCollector methodInfoCollector;
    private final SecurityRequirementCollector securityRequirementCollector;

    public ResourceInfoCollector(ComponentResolver componentResolver, Options opts) {
        super(opts);
        TypeInfoCollector typeInfoCollector = new TypeInfoCollector(componentResolver.schemas(), opts);
        this.methodInfoCollector = new MethodInfoCollector(componentResolver, typeInfoCollector, opts);
        this.securityRequirementCollector = new SecurityRequirementCollector(opts);
    }

    public ResourceInfo getResourceInfo(String resourceName, Map<String, PathItem> paths, List<SecurityRequirement> securityRequirements, Tag tag) {
        ResourceInfo resourceInfo = new ResourceInfo(resourceName + opts.resourceNameSuffix());

        if (opts.useResteasyResponse()) {
            resourceInfo = resourceInfo.withAddedNormalImport("org.jboss.resteasy.reactive.RestResponse");
        } else {
            resourceInfo = resourceInfo.withAddedNormalImport("jakarta.ws.rs.core.Response");
        }

        if (nonEmpty(securityRequirements)) {
            SecurityRequirementInfo secReqInfo = securityRequirementCollector.getSequrityRequirementInfo(securityRequirements);
            resourceInfo = resourceInfo.withAddedAnnotation(secReqInfo.annotation());
        }

        if (opts.addMpOpenApiAnnotations() && nonNull(tag)) {
            AnnotationInfo tagAnnotation = getTagAnnotation(tag);
            resourceInfo = resourceInfo.withAddedAnnotation(tagAnnotation);
        }

        if (opts.addMpRestClientAnnotations()) {
            String configKey = opts.resourceConfigKeyOverride();
            if (isBlank(configKey)) {
                configKey = nonNull(tag) ?
                    extensions(tag.getExtensions())
                        .getString(EXT_RESTCLIENT_CONFIGKEY)
                        .orElse(tag.getName().toLowerCase() + "-api") :
                    resourceName.toLowerCase() + "-api";
            }

            AnnotationInfo registerRestClientAnnotation = getRegisterRestClientAnnotation(configKey);
            resourceInfo = resourceInfo.withAddedAnnotation(registerRestClientAnnotation);

            if (nonNull(tag)) {
                Optional<Map<String, Object>> maybeHeaders = extensions(tag.getExtensions())
                    .getMap(EXT_RESTCLIENT_HEADERS);

                if (maybeHeaders.isPresent()) {
                    List<AnnotationInfo> clientHeaderAnnotations = getClientHeaderParamAnnotations(maybeHeaders.get());
                    resourceInfo = resourceInfo.withAddedAnnotations(clientHeaderAnnotations);
                }
            }

            String clientHeadersFactory = opts.resourceClientHeadersFactoryOverride();
            if (isBlank(clientHeadersFactory)) {
                clientHeadersFactory = nonNull(tag) ?
                    extensions(tag.getExtensions())
                        .getString(EXT_RESTCLIENT_HEADERSFACTORY)
                        .orElse("") :
                    "";
            }

            if (nonBlank(clientHeadersFactory)) {
                AnnotationInfo registerClientHeadersAnnotation = getRegisterClientHeadersAnnotation(clientHeadersFactory);
                resourceInfo = resourceInfo.withAddedAnnotation(registerClientHeadersAnnotation);
            }

            List<String> providers = opts.resourceProvidersOverride();
            if (isEmpty(providers)) {
                providers = nonNull(tag) ?
                    extensions(tag.getExtensions())
                        .getStringArray(EXT_RESTCLIENT_PROVIDERS)
                        .orElse(emptyList()) :
                    emptyList();
            }

            if (nonEmpty(providers)) {
                List<AnnotationInfo> registerProviderAnnotations = getRegisterProviderAnnotations(providers);
                resourceInfo = resourceInfo.withAddedAnnotations(registerProviderAnnotations);
            }

            if (opts.useOidcClientAnnotation()) {
                AnnotationInfo oidcClientFilterAnnotation = getOidcClientFilterAnnotation(configKey);
                resourceInfo = resourceInfo.withAddedAnnotation(oidcClientFilterAnnotation);
            }
        }

        AnnotationInfo pathAnnotation = getPathAnnotation(resourceInfo);
        resourceInfo = resourceInfo.withAddedAnnotation(pathAnnotation);

        String tagName = nonNull(tag) ? tag.getName() : null;

        List<MethodInfo> methods = new ArrayList<>();
        paths.forEach((path, pathInfo) -> {
            if (shouldProcessOperation(pathInfo.getGet(), tagName)) {
                methods.add(methodInfoCollector.getMethodInfo("GET", path, pathInfo.getGet()));
            }
            if (shouldProcessOperation(pathInfo.getPost(), tagName)) {
                methods.add(methodInfoCollector.getMethodInfo("POST", path, pathInfo.getPost()));
            }
            if (shouldProcessOperation(pathInfo.getDelete(), tagName)) {
                methods.add(methodInfoCollector.getMethodInfo("DELETE", path, pathInfo.getDelete()));
            }
            if (shouldProcessOperation(pathInfo.getPut(), tagName)) {
                methods.add(methodInfoCollector.getMethodInfo("PUT", path, pathInfo.getPut()));
            }
            if (shouldProcessOperation(pathInfo.getPatch(), tagName)) {
                methods.add(methodInfoCollector.getMethodInfo("PATCH", path, pathInfo.getPatch()));
            }
        });

        for (MethodInfo method : methods) {
            resourceInfo = resourceInfo.withAddedMethod(method);
        }

        return resourceInfo;
    }

    private List<AnnotationInfo> getRegisterProviderAnnotations(List<String> providers) {
        List<AnnotationInfo> registerProviderAnnotations = new ArrayList<>();

        providers.forEach(provider -> {
            AnnotationInfo registerProviderAnnotation = new AnnotationInfo(
                "@RegisterProvider(%s)".formatted(formatClassRef(getClassNameFromFqn(provider))),
                "org.eclipse.microprofile.rest.client.annotation.RegisterProvider"
            ).withAddedNormalImport(provider);
            registerProviderAnnotations.add(registerProviderAnnotation);
        });

        return registerProviderAnnotations;
    }

    private static AnnotationInfo getOidcClientFilterAnnotation(String configKey) {
        return new AnnotationInfo(
            "@OidcClientFilter(\"%s\")".formatted(configKey),
            "io.quarkus.oidc.client.filter.OidcClientFilter");
    }

    private AnnotationInfo getRegisterClientHeadersAnnotation(String headerFactory) {
        return new AnnotationInfo(
            "@RegisterClientHeaders(%s)".formatted(formatClassRef(getClassNameFromFqn(headerFactory))),
            "org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders"
        ).withAddedNormalImport(headerFactory);
    }

    private AnnotationInfo getPathAnnotation(ResourceInfo resourceInfo) {
        return new AnnotationInfo("@Path(ROOT_PATH)", "jakarta.ws.rs.Path")
            .withAddedStaticImport("%s.%s.ROOT_PATH".formatted(opts.rootPackage(), resourceInfo.name()));
    }

    private AnnotationInfo getTagAnnotation(Tag tag) {
        return new AnnotationInfo(
            "@Tag(name = \"%s\", description = \"%s\")".formatted(tag.getName(), normalizeDescription(tag.getDescription())),
            "org.eclipse.microprofile.openapi.annotations.tags.Tag"
        );
    }

    private static AnnotationInfo getRegisterRestClientAnnotation(String configKey) {
        return new AnnotationInfo(
            "@RegisterRestClient(configKey = \"%s\")".formatted(configKey),
            "org.eclipse.microprofile.rest.client.inject.RegisterRestClient"
        );
    }

    private List<AnnotationInfo> getClientHeaderParamAnnotations(Map<String, Object> headers) {
        List<AnnotationInfo> clientHeaderAnnotations = new ArrayList<>();

        if (nonEmpty(headers)) {
            headers.forEach((header, value) -> {
                ConstantValue headerName = getHeaderNameConstant(header);
                String formattedValue = formatAnnotationNamedParam(List.of("\"%s\"".formatted(value)));
                AnnotationInfo clientHeaderAnnotation = new AnnotationInfo("@ClientHeaderParam(name = %s, value = %s)".formatted(headerName.value(), formattedValue))
                    .withAddedNormalImport("org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam")
                    .withAddedImports(headerName);
                clientHeaderAnnotations.add(clientHeaderAnnotation);
            });
        }

        return clientHeaderAnnotations;
    }

    private boolean shouldProcessOperation(Operation operation, String tag) {
        if (isNull(operation)) {
            return false;
        }

        if (isNull(tag)) {
            return true;
        }

        return nonNull(operation.getTags()) && operation.getTags().contains(tag);
    }
}
