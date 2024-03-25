package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.model.ResourceInfo;

import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.github.torand.openapi2java.utils.StringHelper.normalizeDescription;

public class ResourceInfoCollector {

    private final MethodInfoCollector methodInfoCollector;

    private final Options opts;

    public ResourceInfoCollector(ComponentResolver componentResolver, Options opts) {
        this.methodInfoCollector = new MethodInfoCollector(
            componentResolver,
            new TypeInfoCollector(componentResolver.schemas(), opts),
            opts
        );

        this.opts = opts;
    }

    public ResourceInfo getResourceInfo(String resourceName, Map<String, PathItem> paths, String tag, String description) {
        ResourceInfo resourceInfo = new ResourceInfo();

        resourceInfo.imports.add("jakarta.ws.rs.core.Response");

        resourceInfo.imports.add("org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement");
        resourceInfo.annotations.add("@SecurityRequirement(name = \"basic\")");

        resourceInfo.imports.add("org.eclipse.microprofile.openapi.annotations.tags.Tag");
        resourceInfo.annotations.add("@Tag(name = \"%s\", description = \"%s\")".formatted(tag, normalizeDescription(description)));

        resourceInfo.imports.add("org.eclipse.microprofile.rest.client.inject.RegisterRestClient");
        resourceInfo.annotations.add("@RegisterRestClient(configKey=\"%s\")".formatted(tag.toLowerCase()));

        resourceInfo.imports.add("org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam");
        resourceInfo.staticImports.add("jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION");
        resourceInfo.annotations.add("@ClientHeaderParam(name = AUTHORIZATION, value = \"{basicAuth}\")");

        resourceInfo.imports.add("jakarta.ws.rs.Path");
        resourceInfo.staticImports.add("%s.%s.ROOT_PATH".formatted(opts.rootPackage, resourceName + opts.resourceNameSuffix));
        resourceInfo.annotations.add("@Path(ROOT_PATH)");

        paths.forEach((path, pathInfo) -> {
            if (shouldProcessOperation(pathInfo.getGet(), tag)) {
                resourceInfo.methods.add(methodInfoCollector.getMethodInfo("GET", path, pathInfo.getGet()));
            }
            if (shouldProcessOperation(pathInfo.getPost(), tag)) {
                resourceInfo.methods.add(methodInfoCollector.getMethodInfo("POST", path, pathInfo.getPost()));
            }
            if (shouldProcessOperation(pathInfo.getDelete(), tag)) {
                resourceInfo.methods.add(methodInfoCollector.getMethodInfo("DELETE", path, pathInfo.getDelete()));
            }
            if (shouldProcessOperation(pathInfo.getPut(), tag)) {
                resourceInfo.methods.add(methodInfoCollector.getMethodInfo("PUT", path, pathInfo.getPut()));
            }
            if (shouldProcessOperation(pathInfo.getPatch(), tag)) {
                resourceInfo.methods.add(methodInfoCollector.getMethodInfo("PATCH", path, pathInfo.getPatch()));
            }
        });

        return resourceInfo;
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
