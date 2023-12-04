package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.github.torand.openapi2java.model.MethodInfo;
import org.github.torand.openapi2java.model.TypeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class MethodInfoCollector {
    private static final String CONTENT_TYPE_JSON = "application/json";
    private final ParameterResolver parameterResolver;
    private final ResponseResolver responseResolver;
    private final TypeInfoCollector typeInfoCollector;

    public MethodInfoCollector(ParameterResolver parameterResolver, ResponseResolver responseResolver, TypeInfoCollector typeInfoCollector) {
        this.parameterResolver = parameterResolver;
        this.responseResolver = responseResolver;
        this.typeInfoCollector = typeInfoCollector;
    }

    public MethodInfo getMethodInfo(String verb, String path, Operation operation) {
        MethodInfo methodInfo = new MethodInfo();

        methodInfo.name = operation.getOperationId();

        methodInfo.imports.add("jakarta.ws.rs.%s".formatted(verb));
        methodInfo.annotations.add("@%s".formatted(verb));

        methodInfo.imports.add("jakarta.ws.rs.Path");
        methodInfo.annotations.add("@Path(\"%s\")".formatted(normalizePath(path)));

        if (nonNull(operation.getRequestBody())) {
            String consumesAnnotation = getConsumesAnnotation(operation.getRequestBody(), methodInfo.imports);
            methodInfo.annotations.add(consumesAnnotation);
        }

        if (nonNull(operation.getResponses())) {
            String producesAnnotation = getProducesAnnotation(operation.getResponses(), methodInfo.imports, methodInfo.staticImports);
            methodInfo.annotations.add(producesAnnotation);
        }

        methodInfo.imports.add("org.eclipse.microprofile.openapi.annotations.Operation");
        methodInfo.annotations.add("@Operation(operationId = \"%s\", summary = \"%s\")".formatted(operation.getOperationId(), operation.getSummary()));

        if (nonNull(operation.getParameters()) && !operation.getParameters().isEmpty()) {
            operation.getParameters().forEach(parameter -> {
                String parameterAnnotation = getParameterAnnotation(parameter, methodInfo.imports, methodInfo.staticImports);
                methodInfo.annotations.add(parameterAnnotation);
            });
        }

        if (nonNull(operation.getResponses()) && !operation.getResponses().isEmpty()) {
            operation.getResponses().forEach((code, response) -> {
                String apiResponseAnnotation = getApiResponseAnnotation(response, code, methodInfo.imports, methodInfo.staticImports);
                methodInfo.annotations.add(apiResponseAnnotation);
            });
        }

        if (nonNull(operation.getParameters())) {
            operation.getParameters().forEach(param -> {
                Parameter realParam = param;
                if (nonNull(param.get$ref())) {
                    realParam = parameterResolver.get(param.get$ref())
                        .orElseThrow(() -> new IllegalStateException("Parameter %s not found".formatted(param.get$ref())));
                }

                StringBuilder paramBuilder = new StringBuilder();

                String methodParamAnnotation = getMethodParameterAnnotation(realParam, methodInfo.imports);
                paramBuilder.append(methodParamAnnotation);

                if (Boolean.TRUE.equals(realParam.getRequired())) {
                    methodInfo.imports.add("jakarta.validation.constraints.NotNull");
                    paramBuilder.append("@NotNull ");
                }

                Schema realSchema = realParam.getSchema();
                if (isNull(realSchema)) {
                    throw new IllegalStateException("No schema found for ApiParameter %s".formatted(realParam.getName()));
                }

                TypeInfo paramType = typeInfoCollector.getTypeInfo((JsonSchema)realParam.getSchema());

                // TODO: @Valid
                methodInfo.imports.addAll(paramType.typeImports);
                paramBuilder.append(paramType.name).append(" ");
                paramBuilder.append(toParamName(realParam.getName()));

                methodInfo.parameters.add(paramBuilder.toString());
                methodInfo.parameterComments.add(paramType.description);
            });
        }

        // Payload parameters
        if (nonNull(operation.getRequestBody())) {
            if (nonNull(operation.getRequestBody().getContent())) {
                operation.getRequestBody().getContent().values().stream()
                    .findFirst()
                    .ifPresent(mt -> {
                        if (nonNull(mt.getSchema())) {
                            StringBuilder paramBuilder = new StringBuilder();
                            TypeInfo bodyType = typeInfoCollector.getTypeInfo((JsonSchema)mt.getSchema());

                            // TODO: @Valid
                            methodInfo.imports.addAll(bodyType.typeImports);
                            paramBuilder.append(bodyType.name).append(" ");
                            paramBuilder.append(toParamName(bodyType.name));

                            methodInfo.parameters.add(paramBuilder.toString());
                            methodInfo.parameterComments.add(bodyType.description);
                        }
                    });
            }
        }

        return methodInfo;
    }

    private String getConsumesAnnotation(RequestBody requestBody, Set<String> imports) {
        imports.add("jakarta.ws.rs.Consumes");
        imports.add("jakarta.ws.rs.core.MediaType.APPLICATION_JSON");
        List<String> mediaTypes = new ArrayList<>();
        mediaTypes.add("APPLICATION_JSON");
        if (nonNull(requestBody.getContent())) {
            requestBody.getContent().keySet().stream()
                .filter(mt -> !CONTENT_TYPE_JSON.equals(mt))
                .map(mt -> "\"" + mt + "\"")
                .forEach(mediaTypes::add);
        }
        String mediaTypesString = mediaTypes.size() == 1 ? mediaTypes.get(0) : "{" + String.join(", ", mediaTypes) + "}";
        return "@Consumes(%s)".formatted(mediaTypesString);
    }

    private String getProducesAnnotation(ApiResponses responses, Set<String> imports, Set<String> staticImports) {
        imports.add("jakarta.ws.rs.Produces");
        staticImports.add("jakarta.ws.rs.core.MediaType.APPLICATION_JSON");
        List<String> mediaTypes = new ArrayList<>();
        mediaTypes.add("APPLICATION_JSON");
        Optional<ApiResponse> maybeSuccessResponse = getSuccessResponse(responses);
        maybeSuccessResponse.ifPresent(apiResponse -> {
            if (nonNull(apiResponse.getContent())) {
                apiResponse.getContent().keySet().stream()
                    .filter(mt -> !CONTENT_TYPE_JSON.equals(mt))
                    .map(mt -> "\"" + mt + "\"")
                    .forEach(mediaTypes::add);
            }
        });

        String mediaTypesString = mediaTypes.size() == 1 ? mediaTypes.get(0) : "{" + String.join(", ", mediaTypes) + "}";
        return "@Produces(%s)".formatted(mediaTypesString);
    }

    private String getParameterAnnotation(Parameter parameter, Set<String> imports, Set<String> staticImports) {
        StringBuilder parameterBuilder = new StringBuilder();
        Parameter realParameter = parameter;
        if (nonNull(parameter.get$ref())) {
            realParameter = parameterResolver.get(parameter.get$ref())
                .orElseThrow(() -> new IllegalStateException("Parameter %s not found".formatted(parameter.get$ref())));
        }

        imports.add("org.eclipse.microprofile.openapi.annotations.parameters.Parameter");
        String inValue = getParameterInValue(realParameter, staticImports);
        parameterBuilder.append("@Parameter(in = %s, name = \"%s\", description = \"%s\"".formatted(inValue, realParameter.getName(), realParameter.getDescription()));

        if (Boolean.TRUE.equals(realParameter.getRequired())) {
            parameterBuilder.append(", required = true");
        }

        if (nonNull(realParameter.getSchema())) {
            String schemaAnnotation = getSchemaAnnotation(realParameter.getSchema(), imports, staticImports);
            parameterBuilder.append(", schema = %s".formatted(schemaAnnotation));
        }

        if (nonNull(realParameter.getContent())) {
            parameterBuilder.append(", content = { ");
            List<String> contentItems = new ArrayList<>();
            realParameter.getContent().forEach((contentType, mediaType) -> {
                String contentAnnotation = getContentAnnotation(contentType, mediaType, imports, staticImports);
                contentItems.add(contentAnnotation);
            });

            parameterBuilder.append(String.join(", ", contentItems));
            parameterBuilder.append(" }");
        }
        parameterBuilder.append(")");
        return parameterBuilder.toString();
    }

    private String getParameterInValue(Parameter parameter, Set<String> staticImports) {
        String inValue = switch (parameter.getIn().toLowerCase()) {
            case "" -> "DEFAULT";
            case "header" -> "HEADER";
            case "query" -> "QUERY";
            case "path" -> "PATH";
            case "cookie" -> "COOKIE";
            default -> throw new IllegalStateException("Parameter in-value %s not supported".formatted(parameter.getIn()));
        };

        staticImports.add("org.eclipse.microprofile.openapi.annotations.enums.ParameterIn." + inValue);
        return inValue;
    }

    private String getApiResponseAnnotation(ApiResponse response, String statusCode, Set<String> imports, Set<String> staticImports) {
        StringBuilder apiResponseBuilder = new StringBuilder();
        ApiResponse realResponse = response;
        if (nonNull(response.get$ref())) {
            realResponse = responseResolver.get(response.get$ref())
                .orElseThrow(() -> new IllegalStateException("Response %s not found".formatted(response.get$ref())));
        }

        imports.add("org.eclipse.microprofile.openapi.annotations.responses.APIResponse");
        apiResponseBuilder.append("@APIResponse(responseCode = \"%s\", description = \"%s\"".formatted(statusCode, realResponse.getDescription()));
        if (nonNull(realResponse.getContent())) {
            apiResponseBuilder.append(", content = { ");
            List<String> contentItems = new ArrayList<>();
            realResponse.getContent().forEach((contentType, mediaType) -> {
                String contentAnnotation = getContentAnnotation(contentType, mediaType, imports, staticImports);
                contentItems.add(contentAnnotation);
            });

            apiResponseBuilder.append(String.join(", ", contentItems));
            apiResponseBuilder.append(" }");
        }
        apiResponseBuilder.append(")");
        return apiResponseBuilder.toString();
    }

    private String getMethodParameterAnnotation(Parameter parameter, Set<String> imports) {
        String paramAnnotationName = switch (parameter.getIn().toLowerCase()) {
            case "header" -> "HeaderParam";
            case "query" -> "QueryParam";
            case "path" -> "PathParam";
            case "cookie" -> "CookieParam";
            default -> throw new IllegalStateException("Parameter in-value %s not supported".formatted(parameter.getIn()));
        };

        imports.add("jakarta.ws.rs." + paramAnnotationName);
        return "@%s(\"%s\") ".formatted(paramAnnotationName, parameter.getName());
    }

    private String getContentAnnotation(String contentType, MediaType mediaType, Set<String> imports, Set<String> staticImports) {
        imports.add("org.eclipse.microprofile.openapi.annotations.media.Content");
        if (contentType.equals(CONTENT_TYPE_JSON)) {
            contentType = "APPLICATION_JSON";
            staticImports.add("jakarta.ws.rs.core.MediaType.APPLICATION_JSON");
        } else {
            contentType = "\"" + contentType + "\"";
        }
        String schemaAnnotation = getSchemaAnnotation(mediaType.getSchema(), imports, staticImports);
        return "@Content(mediaType = %s, schema = %s)".formatted(contentType, schemaAnnotation);
    }

    private String getSchemaAnnotation(Schema schema, Set<String> imports, Set<String> staticImports) {
        imports.add("org.eclipse.microprofile.openapi.annotations.media.Schema");
        TypeInfo bodyType = typeInfoCollector.getTypeInfo((JsonSchema)schema);
        imports.addAll(bodyType.typeImports);
        if (nonNull(bodyType.itemType)) {
            staticImports.add("org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY");
            imports.addAll(bodyType.itemType.typeImports);
            return "@Schema(type = ARRAY, implementation = %s.class)".formatted(bodyType.itemType.name);
        } else {
            return "@Schema(implementation = %s.class)".formatted(bodyType.name);
        }
    }

    private String normalizePath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }
        return path;
    }

    private String toParamName(String paramName) {
        paramName = requireNonNull(paramName).replaceAll("\\-", "");
        return paramName.substring(0, 1).toLowerCase() + paramName.substring(1);
    }

    private Optional<ApiResponse> getSuccessResponse(ApiResponses responses) {
        requireNonNull(responses);
        return responses.keySet().stream()
            .filter(sc -> sc.startsWith("2"))
            .findFirst()
            .map(sc -> responses.get(sc));
    }
}
