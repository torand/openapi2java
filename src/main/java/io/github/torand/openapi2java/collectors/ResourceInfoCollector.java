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
import io.github.torand.openapi2java.model.MethodInfo;
import io.github.torand.openapi2java.model.ResourceInfo;
import io.github.torand.openapi2java.model.SecurityRequirementInfo;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.tags.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.openapi2java.collectors.Extensions.EXT_RESTCLIENT_CONFIGKEY;
import static io.github.torand.openapi2java.collectors.Extensions.extensions;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Collects information about a resource from a collection of path items.
 */
public class ResourceInfoCollector extends BaseCollector {
    public static final String AUTH_METHOD_NAME = "authorization";

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
            resourceInfo = resourceInfo.withAddedImport("org.jboss.resteasy.reactive.RestResponse");
        } else {
            resourceInfo = resourceInfo.withAddedImport("jakarta.ws.rs.core.Response");
        }

        if (nonEmpty(securityRequirements)) {
            SecurityRequirementInfo secReqInfo = securityRequirementCollector.getSequrityRequirementInfo(securityRequirements);
            resourceInfo = resourceInfo.withAddedAnnotation(secReqInfo.annotation());
        }

        if (opts.addMpOpenApiAnnotations() && nonNull(tag)) {
            AnnotationInfo tagAnnotation = new AnnotationInfo(
                "@Tag(name = \"%s\", description = \"%s\")".formatted(tag.getName(), normalizeDescription(tag.getDescription())),
                "org.eclipse.microprofile.openapi.annotations.tags.Tag"
            );
            resourceInfo = resourceInfo.withAddedAnnotation(tagAnnotation);
        }

        if (opts.addMpRestClientAnnotations()) {
            String configKey = nonNull(tag) ?
                extensions(tag.getExtensions())
                    .getString(EXT_RESTCLIENT_CONFIGKEY)
                    .orElse(tag.getName().toLowerCase()+"-api") :
                resourceName.toLowerCase()+"-api";

            AnnotationInfo registerRestClientAnnotation = new AnnotationInfo(
                "@RegisterRestClient(configKey = \"%s\")".formatted(configKey),
                "org.eclipse.microprofile.rest.client.inject.RegisterRestClient"
            );
            AnnotationInfo clientHeaderParamAnnotation = new AnnotationInfo(
                "@ClientHeaderParam(name = AUTHORIZATION, value = %s)".formatted(formatAnnotationNamedParam(List.of("\"{%s}\"".formatted(AUTH_METHOD_NAME)))),
                "org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam"
            ).withAddedStaticImport("jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION");

            resourceInfo = resourceInfo
                .withAddedAnnotation(registerRestClientAnnotation)
                .withAddedAnnotation(clientHeaderParamAnnotation)
                .withAuthMethod(getAuthMethodInfo());
        }

        AnnotationInfo pathAnnotation = new AnnotationInfo("@Path(ROOT_PATH)", "jakarta.ws.rs.Path")
            .withAddedStaticImport("%s.%s.ROOT_PATH".formatted(opts.rootPackage(), resourceInfo.name()));
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

    private MethodInfo getAuthMethodInfo() {
        MethodInfo authMethod = new MethodInfo(AUTH_METHOD_NAME);

        if (!opts.useKotlinSyntax()) {
            authMethod = authMethod.withAddedAnnotation(new AnnotationInfo("@SuppressWarnings(\"unused\") // Used by @ClientHeaderParam"));
        }

        return authMethod;
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
