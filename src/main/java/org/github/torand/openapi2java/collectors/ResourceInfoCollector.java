package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.ResourceInfo;

import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ResourceInfoCollector {

    private final MethodInfoCollector methodInfoCollector;

    private final Options opts;

    public ResourceInfoCollector(SchemaResolver schemaResolver, ParameterResolver parameterResolver, ResponseResolver responseResolver, Options opts) {
        this.methodInfoCollector = new MethodInfoCollector(
            parameterResolver,
            responseResolver,
            new TypeInfoCollector(schemaResolver, opts)
        );

        this.opts = opts;
    }

    public ResourceInfo getResourceInfo(String resourceName, Map<String, PathItem> paths, String tag, String description) {
        ResourceInfo resourceInfo = new ResourceInfo();

        resourceInfo.imports.add("org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement");
        resourceInfo.annotations.add("@SecurityRequirement(name = \"basic\")");

        resourceInfo.imports.add("org.eclipse.microprofile.openapi.annotations.tags.Tag");
        resourceInfo.annotations.add("@Tag(name = \"%s\", description = \"%s\")".formatted(tag, description));

        resourceInfo.imports.add("org.eclipse.microprofile.rest.client.inject.RegisterRestClient");
        resourceInfo.annotations.add("@RegisterRestClient(configKey=\"%s\")".formatted(tag.toLowerCase()));

        resourceInfo.imports.add("org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam");
        resourceInfo.staticImports.add("jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION");
        resourceInfo.annotations.add("@ClientHeaderParam(name = AUTHORIZATION, value = \"{basicAuth}\")");

        resourceInfo.imports.add("jakarta.ws.rs.Path");
        resourceInfo.staticImports.add("%s.%s.ROOT_PATH".formatted(opts.rootPackage, resourceName + opts.resourceNameSuffix));
        resourceInfo.annotations.add("@Path(ROOT_PATH)");

        paths.forEach((path, pathInfo) -> {
            if (nonNull(pathInfo.getGet()) && usesTag(pathInfo.getGet(), tag)) {
                resourceInfo.methods.add(methodInfoCollector.getMethodInfo("GET", path, pathInfo.getGet()));
            }
            if (nonNull(pathInfo.getPost()) && usesTag(pathInfo.getPost(), tag)) {
                resourceInfo.methods.add(methodInfoCollector.getMethodInfo("POST", path, pathInfo.getPost()));
            }
            if (nonNull(pathInfo.getDelete()) && usesTag(pathInfo.getDelete(), tag)) {
                resourceInfo.methods.add(methodInfoCollector.getMethodInfo("DELETE", path, pathInfo.getDelete()));
            }
            if (nonNull(pathInfo.getPut()) && usesTag(pathInfo.getPut(), tag)) {
                resourceInfo.methods.add(methodInfoCollector.getMethodInfo("PUT", path, pathInfo.getPut()));
            }
            if (nonNull(pathInfo.getPatch()) && usesTag(pathInfo.getPatch(), tag)) {
                resourceInfo.methods.add(methodInfoCollector.getMethodInfo("PATCH", path, pathInfo.getPatch()));
            }
        });

        return resourceInfo;
    }

    private boolean usesTag(Operation operation, String tag) {
        if (isNull(tag)) {
            return true;
        }

        return nonNull(operation.getTags()) && operation.getTags().contains(tag);
    }
}
