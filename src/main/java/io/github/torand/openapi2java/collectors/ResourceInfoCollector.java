package io.github.torand.openapi2java.collectors;

import io.github.torand.openapi2java.Options;
import io.github.torand.openapi2java.model.MethodInfo;
import io.github.torand.openapi2java.model.ResourceInfo;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ResourceInfoCollector extends BaseCollector {

    private final MethodInfoCollector methodInfoCollector;

    public ResourceInfoCollector(ComponentResolver componentResolver, Options opts) {
        super(opts);
        this.methodInfoCollector = new MethodInfoCollector(
            componentResolver,
            new TypeInfoCollector(componentResolver.schemas(), opts),
            opts
        );
    }

    public ResourceInfo getResourceInfo(String resourceName, Map<String, PathItem> paths, String tag, String description) {
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.name = resourceName + opts.resourceNameSuffix;

        resourceInfo.imports.add("jakarta.ws.rs.core.Response");

        resourceInfo.imports.add("org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement");
        resourceInfo.annotations.add("@SecurityRequirement(name = \"basic\")");

        resourceInfo.imports.add("org.eclipse.microprofile.openapi.annotations.tags.Tag");
        resourceInfo.annotations.add("@Tag(name = \"%s\", description = \"%s\")".formatted(tag, normalizeDescription(description)));

        resourceInfo.imports.add("org.eclipse.microprofile.rest.client.inject.RegisterRestClient");
        resourceInfo.annotations.add("@RegisterRestClient(configKey = \"%s\")".formatted(tag.toLowerCase()));

        resourceInfo.imports.add("org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam");
        resourceInfo.staticImports.add("jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION");
        resourceInfo.annotations.add("@ClientHeaderParam(name = AUTHORIZATION, value = %s)".formatted(formatAnnotationNamedParam(List.of("\"{basicAuth}\""))));

        resourceInfo.authMethod = getAuthMethodInfo();

        resourceInfo.imports.add("jakarta.ws.rs.Path");
        resourceInfo.staticImports.add("%s.%s.ROOT_PATH".formatted(opts.rootPackage, resourceInfo.name));
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

    private MethodInfo getAuthMethodInfo() {
        MethodInfo authMethod = new MethodInfo();

        if (!opts.useKotlinSyntax) {
            authMethod.annotations.add("@SuppressWarnings(\"unused\") // Used by @ClientHeaderParam");
        }
        authMethod.name = "basicAuth";

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
