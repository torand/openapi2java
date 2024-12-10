/*
 * Copyright (c) 2024 Tore Eide Andersen
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

import io.github.torand.openapi2java.Options;
import io.github.torand.openapi2java.model.MethodInfo;
import io.github.torand.openapi2java.model.MethodParamInfo;
import io.github.torand.openapi2java.model.SecurityRequirementInfo;
import io.github.torand.openapi2java.model.TypeInfo;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.github.torand.openapi2java.collectors.SchemaResolver.isObjectType;
import static io.github.torand.openapi2java.collectors.TypeInfoCollector.NullabilityResolution.FORCE_NOT_NULLABLE;
import static io.github.torand.openapi2java.collectors.TypeInfoCollector.NullabilityResolution.FORCE_NULLABLE;
import static io.github.torand.openapi2java.utils.CollectionHelper.nonEmpty;
import static io.github.torand.openapi2java.utils.StringHelper.joinCsv;
import static io.github.torand.openapi2java.utils.StringHelper.nonBlank;
import static io.github.torand.openapi2java.utils.StringHelper.quote;
import static io.github.torand.openapi2java.utils.StringHelper.stripTail;
import static io.github.torand.openapi2java.utils.StringHelper.uncapitalize;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class MethodInfoCollector extends BaseCollector {
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String TEXT_PLAIN = "text/plain";

    private static final Map<String, String> standardContentTypes = Map.of(
        APPLICATION_JSON, "APPLICATION_JSON",
        APPLICATION_OCTET_STREAM, "APPLICATION_OCTET_STREAM",
        APPLICATION_FORM_URLENCODED, "APPLICATION_FORM_URLENCODED",
        MULTIPART_FORM_DATA, "MULTIPART_FORM_DATA",
        TEXT_PLAIN, "TEXT_PLAIN"
    );

    private final ComponentResolver componentResolver;
    private final TypeInfoCollector typeInfoCollector;
    private final SecurityRequirementCollector securityRequirementCollector;

    public MethodInfoCollector(ComponentResolver componentResolver, TypeInfoCollector typeInfoCollector, Options opts) {
        super(opts);
        this.componentResolver = componentResolver;
        this.typeInfoCollector = typeInfoCollector;
        this.securityRequirementCollector = new SecurityRequirementCollector(opts);
    }

    public MethodInfo getMethodInfo(String verb, String path, Operation operation) {
        MethodInfo methodInfo = new MethodInfo();

        methodInfo.name = operation.getOperationId();

        if (TRUE.equals(operation.getDeprecated())) {
            methodInfo.deprecationMessage = formatDeprecationMessage(operation.getExtensions());
        }

        methodInfo.imports.add("jakarta.ws.rs.%s".formatted(verb));
        methodInfo.annotations.add("@%s".formatted(verb));

        methodInfo.imports.add("jakarta.ws.rs.Path");
        methodInfo.annotations.add("@Path(\"%s\")".formatted(normalizePath(path)));

        if (nonNull(operation.getRequestBody())) {
            String consumesAnnotation = getConsumesAnnotation(operation.getRequestBody(), methodInfo.imports, methodInfo.staticImports);
            methodInfo.annotations.add(consumesAnnotation);
        }

        if (nonNull(operation.getResponses())) {
            String producesAnnotation = getProducesAnnotation(operation.getResponses(), methodInfo.imports, methodInfo.staticImports);
            methodInfo.annotations.add(producesAnnotation);
        }

        if (nonEmpty(operation.getSecurity())) {
            SecurityRequirementInfo secReqInfo = securityRequirementCollector.getSequrityRequirementInfo(operation.getSecurity());

            methodInfo.imports.addAll(secReqInfo.imports);
            methodInfo.annotations.addAll(secReqInfo.annotations);
        }

        if (opts.addMpOpenApiAnnotations) {
            methodInfo.annotations.add(getOperationAnnotation(operation, methodInfo.imports));

            if (nonEmpty(operation.getParameters())) {
                operation.getParameters().forEach(parameter -> {
                    String parameterAnnotation = getParameterAnnotation(parameter, methodInfo.imports, methodInfo.staticImports);
                    methodInfo.annotations.add(parameterAnnotation);
                });
            }

            if (nonEmpty(operation.getResponses())) {
                operation.getResponses().forEach((code, response) -> {
                    String apiResponseAnnotation = getApiResponseAnnotation(response, code, methodInfo.imports, methodInfo.staticImports);
                    if (opts.useResteasyResponse && isNull(methodInfo.returnType)) {
                        methodInfo.returnType = getResponseType(code, response);
                    }
                    methodInfo.annotations.add(apiResponseAnnotation);
                });
            }
        }

        if (nonEmpty(operation.getParameters())) {
            operation.getParameters().forEach(param -> {
                Parameter realParam = param;
                if (nonNull(param.get$ref())) {
                    realParam = componentResolver.parameters().getOrThrow(param.get$ref());
                }

                MethodParamInfo paramInfo = new MethodParamInfo();
                paramInfo.nullable = !TRUE.equals(realParam.getRequired());

                String methodParamAnnotation = getMethodParameterAnnotation(realParam, paramInfo.imports, paramInfo.staticImports);
                paramInfo.annotations.add(methodParamAnnotation);

                Schema<?> realSchema = realParam.getSchema();
                if (isNull(realSchema)) {
                    throw new IllegalStateException("No schema found for ApiParameter %s".formatted(realParam.getName()));
                }

                TypeInfo paramType = typeInfoCollector.getTypeInfo(realParam.getSchema(), paramInfo.nullable ? FORCE_NULLABLE : FORCE_NOT_NULLABLE);
                paramInfo.type = paramType;

                paramInfo.name = toParamName(realParam.getName());
                paramInfo.comment = paramType.description;

                paramInfo.annotations.addAll(paramType.annotations);
                paramInfo.imports.addAll(paramType.annotationImports);

                if (TRUE.equals(realParam.getDeprecated())) {
                    paramInfo.deprecationMessage = formatDeprecationMessage(realParam.getExtensions());
                }

                methodInfo.parameters.add(paramInfo);
            });
        }

        // Payload parameters
        if (nonNull(operation.getRequestBody()) && nonEmpty(operation.getRequestBody().getContent())) {
            operation.getRequestBody().getContent().keySet().stream()
                .findFirst()
                .ifPresent(mtKey -> {
                    boolean isMultipart = MULTIPART_FORM_DATA.equals(mtKey);
                    MediaType mt = operation.getRequestBody().getContent().get(mtKey);
                    Schema<?> mtSchema = mt.getSchema();

                    if (nonNull(mtSchema)) {
                        if (isMultipart) {
                            if (!isObjectType(mtSchema)) {
                                throw new IllegalStateException("Multipart body should be of type 'object'");
                            }

                            if (mtSchema.getProperties().containsKey("file") && !mtSchema.getProperties().containsKey("filename")) {
                                throw new IllegalStateException("A multipart property 'file' should be accompanied by a 'filename' property containing the filename, since the File object will reference a random temporary internal filename.");
                            }

                            mtSchema.getProperties().forEach((propName, propSchema) -> {
                                MethodParamInfo paramInfo = getMultipartPayloadMethodParameter(propName, propSchema);
                                methodInfo.parameters.add(paramInfo);
                            });
                        } else {
                            MethodParamInfo paramInfo = getSingularPayloadMethodParameter(mtSchema);
                            methodInfo.parameters.add(paramInfo);
                        }
                    }
                });
        }

        return methodInfo;
    }

    private String getResponseType(String code, ApiResponse response) {
        String responseType = null;

        int numericCode = Integer.parseInt(code);
        if (numericCode >= 200 && numericCode <= 299) {
            if (nonEmpty(response.getContent())) {
                for (MediaType mediaType : response.getContent().values()) {
                    Schema<?> schema = mediaType.getSchema();
                    TypeInfo bodyType = typeInfoCollector.getTypeInfo(schema);
                    if (nonNull(bodyType)) {
                        String fullName = bodyType.getFullName();
                        if (isNull(responseType)) {
                            // If no return type is set yet, the type of this media type is used...
                            responseType = fullName;
                        } else if (!fullName.equals(responseType)) {
                            // ...but if a return type is already set, and this media type specifies
                            // a different type, we cannot safely infer one single return type, and
                            // give up type safety and allow anything
                            responseType = opts.useKotlinSyntax ? "*" : "?";
                            break; // no need to look any further
                        }
                    }
                }
            }
        }
        return responseType;
    }

    private MethodParamInfo getSingularPayloadMethodParameter(Schema<?> schema) {
        MethodParamInfo paramInfo = new MethodParamInfo();
        paramInfo.nullable = false;

        TypeInfo bodyType = typeInfoCollector.getTypeInfo(schema, FORCE_NOT_NULLABLE);
        paramInfo.type = bodyType;

        paramInfo.name = toParamName(bodyType.name);
        paramInfo.comment = bodyType.description;

        paramInfo.annotations.addAll(bodyType.annotations);
        paramInfo.imports.addAll(bodyType.annotationImports);

        return paramInfo;
    }

    private MethodParamInfo getMultipartPayloadMethodParameter(String name, Schema<?> schema) {
        MethodParamInfo paramInfo = new MethodParamInfo();

        TypeInfo bodyType;
        String partMediaType = null;

        if ("file".equals(name)) {
            bodyType = new TypeInfo();
            bodyType.name = "File";
            bodyType.typeImports.add("java.io.File");
            bodyType.nullable = false;
            bodyType.annotations.add("@NotNull");
            bodyType.annotationImports.add("jakarta.validation.constraints.NotNull");
            bodyType.description = schema.getDescription();

            partMediaType = APPLICATION_OCTET_STREAM;
        } else {
            bodyType = typeInfoCollector.getTypeInfo(schema);

            if (isObjectType(schema)) {
                throw new IllegalStateException("Multipart property of type 'object' not supported. Use $ref instead.");
            }

            partMediaType = APPLICATION_JSON;
            if (bodyType.isPrimitive() || (bodyType.isArray() && bodyType.itemType.isPrimitive())) {
                partMediaType = TEXT_PLAIN;
            }
        }

        if (nonBlank(schema.getContentMediaType())) {
            partMediaType = schema.getContentMediaType();
        }
        partMediaType = toMediaTypeConstant(partMediaType, paramInfo.staticImports);

        paramInfo.nullable = bodyType.nullable;
        paramInfo.type = bodyType;

        paramInfo.name = name;
        paramInfo.comment = bodyType.description;

        paramInfo.annotations.add("@RestForm(\"%s\")".formatted(name));
        paramInfo.imports.add("org.jboss.resteasy.reactive.RestForm");

        paramInfo.annotations.add("@PartType(%s)".formatted(partMediaType));
        paramInfo.imports.add("org.jboss.resteasy.reactive.PartType");

        paramInfo.annotations.addAll(bodyType.annotations);
        paramInfo.imports.addAll(bodyType.annotationImports);

        return paramInfo;
    }

    private String getConsumesAnnotation(RequestBody requestBody, Set<String> imports, Set<String> staticImports) {
        imports.add("jakarta.ws.rs.Consumes");

        List<String> mediaTypes = new ArrayList<>();

        if (nonEmpty(requestBody.getContent())) {
            requestBody.getContent().keySet().stream()
                .forEach(mt -> mediaTypes.add(toMediaTypeConstant(mt, staticImports)));
        }

        String mediaTypesString = formatAnnotationDefaultParam(mediaTypes);
        return "@Consumes(%s)".formatted(mediaTypesString);
    }

    private String getProducesAnnotation(ApiResponses responses, Set<String> imports, Set<String> staticImports) {
        imports.add("jakarta.ws.rs.Produces");
        staticImports.add("jakarta.ws.rs.core.MediaType.APPLICATION_JSON");

        List<String> mediaTypes = new ArrayList<>();
        mediaTypes.add("APPLICATION_JSON");

        getSuccessResponse(responses).ifPresent(apiResponse -> {
            if (nonNull(apiResponse.getContent())) {
                apiResponse.getContent().keySet().stream()
                    .filter(mt -> !APPLICATION_JSON.equals(mt))
                    .forEach(mt -> mediaTypes.add(toMediaTypeConstant(mt, staticImports)));
            }
        });

        String mediaTypesString = formatAnnotationDefaultParam(mediaTypes);
        return "@Produces(%s)".formatted(mediaTypesString);
    }

    private String getOperationAnnotation(Operation operation, Set<String> imports) {
        imports.add("org.eclipse.microprofile.openapi.annotations.Operation");
        List<String> operationParams = new ArrayList<>();
        operationParams.add("operationId = \"%s\"".formatted(operation.getOperationId()));
        operationParams.add("summary = \"%s\"".formatted(operation.getSummary()));

        if (TRUE.equals(operation.getDeprecated())) {
            operationParams.add("deprecated = true");
        }

        return "@Operation(%s)".formatted(joinCsv(operationParams));
    }

    private String getParameterAnnotation(Parameter parameter, Set<String> imports, Set<String> staticImports) {
        Parameter realParameter = parameter;
        if (nonNull(parameter.get$ref())) {
            realParameter = componentResolver.parameters().getOrThrow(parameter.get$ref());
        }

        List<String> parameterParams = new ArrayList<>();
        imports.add("org.eclipse.microprofile.openapi.annotations.parameters.Parameter");
        String inValue = getParameterInValue(realParameter, staticImports);
        String inName = opts.useKotlinSyntax ? "`in`" : "in";
        parameterParams.add("%s = %s".formatted(inName, inValue));

        if (inValue.equalsIgnoreCase("header")) {
            parameterParams.add("name = %s".formatted(getHeaderNameConstant(realParameter.getName(), staticImports)));
        } else {
            parameterParams.add("name = \"%s\"".formatted(realParameter.getName()));
        }

        parameterParams.add("description = \"%s\"".formatted(normalizeDescription(realParameter.getDescription())));

        if (TRUE.equals(realParameter.getRequired())) {
            parameterParams.add("required = true");
        }

        if (nonNull(realParameter.getSchema())) {
            String schemaAnnotation = getSchemaAnnotation(realParameter.getSchema(), imports, staticImports);
            parameterParams.add("schema = %s".formatted(schemaAnnotation));
        }

        if (nonEmpty(realParameter.getContent())) {
            List<String> contentItems = new ArrayList<>();
            realParameter.getContent().forEach((contentType, mediaType) -> {
                String contentAnnotation = getContentAnnotation(contentType, mediaType, imports, staticImports);
                contentItems.add(contentAnnotation);
            });

            parameterParams.add("content = %s".formatted(formatAnnotationNamedParam(contentItems)));
        }

        if (TRUE.equals(realParameter.getDeprecated())) {
            parameterParams.add("deprecated = true");
        }

        return "@Parameter(%s)".formatted(joinCsv(parameterParams));
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
        ApiResponse realResponse = response;
        if (nonNull(response.get$ref())) {
            realResponse = componentResolver.responses().getOrThrow(response.get$ref());
        }

        List<String> apiResponseParams = new ArrayList<>();
        imports.add("org.eclipse.microprofile.openapi.annotations.responses.APIResponse");
        apiResponseParams.add("responseCode = \"%s\"".formatted(statusCode));
        apiResponseParams.add("description = \"%s\"".formatted(normalizeDescription(realResponse.getDescription())));

        if (nonEmpty(realResponse.getHeaders())) {
            List<String> headerItems = new ArrayList<>();
            realResponse.getHeaders().forEach((name, header) -> {
                String headerAnnotation = getHeaderAnnotation(name, header, imports, staticImports);
                headerItems.add(headerAnnotation);
            });

            apiResponseParams.add("headers = %s".formatted(formatAnnotationNamedParam(headerItems)));
        }

        if (nonEmpty(realResponse.getContent())) {
            List<String> contentItems = new ArrayList<>();
            realResponse.getContent().forEach((contentType, mediaType) -> {
                String contentAnnotation = getContentAnnotation(contentType, mediaType, imports, staticImports);
                contentItems.add(contentAnnotation);
            });

            apiResponseParams.add("content = %s".formatted(formatAnnotationNamedParam(contentItems)));
        }

        return "@APIResponse(%s)".formatted(joinCsv(apiResponseParams));
    }

    private String getMethodParameterAnnotation(Parameter parameter, Set<String> imports, Set<String> staticImports) {
        String paramAnnotationName = switch (parameter.getIn().toLowerCase()) {
            case "header" -> "HeaderParam";
            case "query" -> "QueryParam";
            case "path" -> "PathParam";
            case "cookie" -> "CookieParam";
            default -> throw new IllegalStateException("Parameter in-value %s not supported".formatted(parameter.getIn()));
        };

        imports.add("jakarta.ws.rs." + paramAnnotationName);
        if (paramAnnotationName.equals("HeaderParam")) {
            return "@%s(%s)".formatted(paramAnnotationName, getHeaderNameConstant(parameter.getName(), staticImports));
        } else {
            return "@%s(\"%s\")".formatted(paramAnnotationName, parameter.getName());
        }
    }

    private String getHeaderNameConstant(String name, Set<String> staticImports) {
        String standardHeaderConstant = switch (name.toUpperCase()) {
            case "ACCEPT-LANGUAGE" -> "ACCEPT_LANGUAGE";
            case "CONTENT-LANGUAGE" -> "CONTENT_LANGUAGE";
            case "LOCATION" -> "LOCATION";
            default -> null;
        };

        if (nonNull(standardHeaderConstant)) {
            staticImports.add("jakarta.ws.rs.core.HttpHeaders." + standardHeaderConstant);
            return standardHeaderConstant;
        }

        return quote(name);
    }

    private String getContentAnnotation(String contentType, MediaType mediaType, Set<String> imports, Set<String> staticImports) {
        imports.add("org.eclipse.microprofile.openapi.annotations.media.Content");
        contentType = toMediaTypeConstant(contentType, staticImports);
        String schemaAnnotation = getSchemaAnnotation(mediaType.getSchema(), imports, staticImports);
        return formatInnerAnnotation("Content(mediaType = %s, schema = %s)", contentType, schemaAnnotation);
    }

    private String getSchemaAnnotation(Schema<?> schema, Set<String> imports, Set<String> staticImports) {
        imports.add("org.eclipse.microprofile.openapi.annotations.media.Schema");
        List<String> schemaParams = new ArrayList<>();

        TypeInfo bodyType = typeInfoCollector.getTypeInfo(schema);
        imports.addAll(bodyType.typeImports);

        if (nonNull(bodyType.itemType)) {
            staticImports.add("org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY");
            imports.addAll(bodyType.itemType.typeImports);
            schemaParams.add("type = ARRAY");
            bodyType = bodyType.itemType;
        }

        schemaParams.add("implementation = %s".formatted(formatClassRef(bodyType.name)));
        if (nonNull(schema.getDefault())) {
            schemaParams.add("defaultValue = \"%s\"".formatted(schema.getDefault().toString()));
        }
        if (nonBlank(bodyType.schemaFormat)) {
            schemaParams.add("format = \"%s\"".formatted(bodyType.schemaFormat));
        }
        if (nonBlank(bodyType.schemaPattern)) {
            schemaParams.add("pattern = \"%s\"".formatted(bodyType.schemaPattern));
        }

        return formatInnerAnnotation("Schema(%s)", joinCsv(schemaParams));
    }

    private String getHeaderAnnotation(String name, Header header, Set<String> imports, Set<String> staticImports) {
        Header realHeader = header;
        if (nonNull(header.get$ref())) {
            realHeader = componentResolver.headers().getOrThrow(header.get$ref());
        }

        imports.add("org.eclipse.microprofile.openapi.annotations.headers.Header");
        String schemaAnnotation = getSchemaAnnotation(realHeader.getSchema(), imports, staticImports);
        return formatInnerAnnotation("Header(name = \"%s\", description = \"%s\", schema = %s)", name, normalizeDescription(realHeader.getDescription()), schemaAnnotation);
    }

    private String toParamName(String paramName) {
        requireNonNull(paramName);
        if (nonBlank(opts.pojoNameSuffix) && paramName.endsWith(opts.pojoNameSuffix)) {
            paramName = stripTail(paramName, opts.pojoNameSuffix.length());
        }
        paramName = paramName.replaceAll("-", "");

        // paramName may contain array-symbol, replace with plural "s"
        paramName = paramName.replaceAll("\\[]", "s");
        return uncapitalize(paramName);
    }

    private Optional<ApiResponse> getSuccessResponse(ApiResponses responses) {
        requireNonNull(responses);
        return responses.keySet().stream()
            .filter(sc -> sc.startsWith("2"))
            .findFirst()
            .map(responses::get);
    }

    private String toMediaTypeConstant(String contentType, Set<String> staticImports) {
        if (standardContentTypes.containsKey(contentType)) {
            contentType = standardContentTypes.get(contentType);
            staticImports.add("jakarta.ws.rs.core.MediaType." + contentType);
        } else {
            contentType = quote(contentType);
        }
        return contentType;
    }
}
