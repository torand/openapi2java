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
import io.github.torand.openapi2java.model.*;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.*;

import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.collection.CollectionHelper.streamSafely;
import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static io.github.torand.javacommons.lang.StringHelper.quote;
import static io.github.torand.javacommons.lang.StringHelper.stripTail;
import static io.github.torand.javacommons.lang.StringHelper.uncapitalize;
import static io.github.torand.openapi2java.collectors.SchemaResolver.isObjectType;
import static io.github.torand.openapi2java.collectors.TypeInfoCollector.NullabilityResolution.FORCE_NOT_NULLABLE;
import static io.github.torand.openapi2java.collectors.TypeInfoCollector.NullabilityResolution.FORCE_NULLABLE;
import static io.github.torand.openapi2java.utils.StringUtils.joinCsv;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * Collects information about a method from an operation.
 */
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
        MethodInfo methodInfo = new MethodInfo(operation.getOperationId())
            .withAddedAnnotation(getVerbAnnotation(verb))
            .withAddedAnnotation(getPathAnnotation(path));

        if (TRUE.equals(operation.getDeprecated())) {
            methodInfo = methodInfo.withDeprecationMessage(formatDeprecationMessage(operation.getExtensions()));
        }

        if (nonNull(operation.getRequestBody())) {
            methodInfo = methodInfo.withAddedAnnotation(getConsumesAnnotation(operation.getRequestBody()));
        }

        if (nonNull(operation.getResponses())) {
            methodInfo = methodInfo.withAddedAnnotation(getProducesAnnotation(operation.getResponses()));
        }

        if (nonEmpty(operation.getSecurity())) {
            SecurityRequirementInfo secReqInfo = securityRequirementCollector.getSequrityRequirementInfo(operation.getSecurity());
            if (nonNull(secReqInfo.annotation())) {
                methodInfo = methodInfo.withAddedAnnotation(secReqInfo.annotation());
            }
        }

        if (opts.addMpOpenApiAnnotations()) {
            methodInfo = methodInfo.withAddedAnnotation(getOperationAnnotation(operation));

            if (nonEmpty(operation.getParameters())) {
                List<AnnotationInfo> parameterAnnotations = new ArrayList<>();
                operation.getParameters().forEach(parameter ->
                    parameterAnnotations.add(getParameterAnnotation(parameter))
                );
                methodInfo = methodInfo.withAddedAnnotations(parameterAnnotations);
            }

            if (nonEmpty(operation.getResponses())) {
                List<AnnotationInfo> apiResponseAnnotations = new ArrayList<>();
                operation.getResponses().forEach((code, response) ->
                    apiResponseAnnotations.add(getApiResponseAnnotation(response, code))
                );
                methodInfo = methodInfo.withAddedAnnotations(apiResponseAnnotations);

                if (opts.useResteasyResponse()) {
                    String code = operation.getResponses().keySet().iterator().next();
                    ApiResponse response = operation.getResponses().get(code);
                    methodInfo = methodInfo.withReturnType(getResponseType(code, response));
                }
            }
        }

        List<MethodParamInfo> methodParams = new ArrayList<>();

        if (nonEmpty(operation.getParameters())) {
            operation.getParameters().forEach(param -> {
                Parameter realParam = param;
                if (nonNull(param.get$ref())) {
                    realParam = componentResolver.parameters().getOrThrow(param.get$ref());
                }

                MethodParamInfo paramInfo = new MethodParamInfo()
                    .withNullable(!TRUE.equals(realParam.getRequired()))
                    .withAddedAnnotation(getMethodParameterAnnotation(realParam));

                Schema<?> realSchema = realParam.getSchema();
                if (isNull(realSchema)) {
                    throw new IllegalStateException("No schema found for ApiParameter %s".formatted(realParam.getName()));
                }

                TypeInfo paramType = typeInfoCollector.getTypeInfo(realParam.getSchema(), paramInfo.nullable() ? FORCE_NULLABLE : FORCE_NOT_NULLABLE);
                paramInfo = paramInfo
                    .withType(paramType)
                    .withName(toParamName(realParam.getName()))
                    .withComment(paramType.description)
                    .withAddedAnnotations(paramType.annotations.stream().map(AnnotationInfo::new).toList())
                    .withAddedImports(paramType.annotationImports);

                if (TRUE.equals(realParam.getDeprecated())) {
                    paramInfo = paramInfo.withDeprecationMessage(formatDeprecationMessage(realParam.getExtensions()));
                }

                methodParams.add(paramInfo);
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
                                methodParams.add(paramInfo);
                            });
                        } else {
                            MethodParamInfo paramInfo = getSingularPayloadMethodParameter(mtSchema);
                            methodParams.add(paramInfo);
                        }
                    }
                });
        }

        return methodInfo.withAddedParameters(methodParams);
    }

    private AnnotationInfo getVerbAnnotation(String verb) {
        return new AnnotationInfo("@%s".formatted(verb), "jakarta.ws.rs.%s".formatted(verb));
    }

    private AnnotationInfo getPathAnnotation(String path) {
        return new AnnotationInfo("@Path(\"%s\")".formatted(normalizePath(path)), "jakarta.ws.rs.Path");
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
                            responseType = opts.useKotlinSyntax() ? "*" : "?";
                            break; // no need to look any further
                        }
                    }
                }
            }
        }
        return responseType;
    }

    private MethodParamInfo getSingularPayloadMethodParameter(Schema<?> schema) {
        TypeInfo bodyType = typeInfoCollector.getTypeInfo(schema, FORCE_NOT_NULLABLE);

        MethodParamInfo paramInfo = new MethodParamInfo(toParamName(bodyType.name))
            .withNullable(false)
            .withType(bodyType)
            .withComment(bodyType.description)
            .withAddedAnnotations(bodyType.annotations.stream().map(AnnotationInfo::new).toList())
            .withAddedImports(bodyType.annotationImports);

        return paramInfo;
    }

    private MethodParamInfo getMultipartPayloadMethodParameter(String name, Schema<?> schema) {
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

        // OpenAPI 3.1.x only
        if (nonBlank(schema.getContentMediaType())) {
            partMediaType = schema.getContentMediaType();
        }

        ConstantValue partMediaTypeConstant = getMediaTypeConstant(partMediaType);

        MethodParamInfo paramInfo = new MethodParamInfo(name)
            .withNullable(bodyType.nullable)
            .withType(bodyType)
            .withComment(bodyType.description)
            .withAddedAnnotation(new AnnotationInfo("@RestForm(\"%s\")".formatted(name), "org.jboss.resteasy.reactive.RestForm"))
            .withAddedAnnotation(new AnnotationInfo("@PartType(%s)".formatted(partMediaTypeConstant.value()), "org.jboss.resteasy.reactive.PartType"))
            // TODO: Combine with annotationImports
            .withAddedAnnotations(bodyType.annotations.stream().map(AnnotationInfo::new).toList())
            .withAddedImports(bodyType.annotationImports)

            .withAddedStaticImport(partMediaTypeConstant.staticImport());

        return paramInfo;
    }

    private AnnotationInfo getConsumesAnnotation(RequestBody requestBody) {
        List<ConstantValue> mediaTypes = new ArrayList<>();
        if (nonEmpty(requestBody.getContent())) {
            streamSafely(requestBody.getContent().keySet())
                .map(this::getMediaTypeConstant)
                .forEach(mediaTypes::add);
        }

        String mediaTypesString = formatAnnotationDefaultParam(mediaTypes.stream().map(ConstantValue::value).toList());

        return new AnnotationInfo("@Consumes(%s)".formatted(mediaTypesString))
            .withAddedImport("jakarta.ws.rs.Consumes")
            .withAddedConstantValueImports(mediaTypes);
    }

    private AnnotationInfo getProducesAnnotation(ApiResponses responses) {
        // TODO: Bør denne alltid være med?
        List<ConstantValue> mediaTypes = new ArrayList<>();
        mediaTypes.add(new ConstantValue("APPLICATION_JSON", "jakarta.ws.rs.core.MediaType.APPLICATION_JSON"));

        getSuccessResponse(responses).ifPresent(apiResponse -> {
            if (nonNull(apiResponse.getContent())) {
                apiResponse.getContent().keySet().stream()
                    .filter(mt -> !APPLICATION_JSON.equals(mt))
                    .map(this::getMediaTypeConstant)
                    .forEach(mediaTypes::add);
            }
        });

        String mediaTypesString = formatAnnotationDefaultParam(mediaTypes.stream().map(ConstantValue::value).toList());

        return new AnnotationInfo("@Produces(%s)".formatted(mediaTypesString))
            .withAddedImport("jakarta.ws.rs.Produces")
            .withAddedConstantValueImports(mediaTypes);
    }

    private AnnotationInfo getOperationAnnotation(Operation operation) {
        List<String> params = new ArrayList<>();
        params.add("operationId = \"%s\"".formatted(operation.getOperationId()));
        params.add("summary = \"%s\"".formatted(operation.getSummary()));

        if (TRUE.equals(operation.getDeprecated())) {
            params.add("deprecated = true");
        }

        return new AnnotationInfo("@Operation(%s)".formatted(joinCsv(params)), "org.eclipse.microprofile.openapi.annotations.Operation");
    }

    private AnnotationInfo getParameterAnnotation(Parameter parameter) {
        Parameter realParameter = parameter;
        if (nonNull(parameter.get$ref())) {
            realParameter = componentResolver.parameters().getOrThrow(parameter.get$ref());
        }

        AnnotationInfo parameterAnnotation = new AnnotationInfo();

        List<String> params = new ArrayList<>();

        ConstantValue inValue = getParameterInValue(realParameter);
        String inName = opts.useKotlinSyntax() ? "`in`" : "in";
        params.add("%s = %s".formatted(inName, inValue.value()));
        parameterAnnotation = parameterAnnotation.withAddedConstantValueImports(inValue);

        if (inValue.value().equalsIgnoreCase("header")) {
            ConstantValue headerNameConstant = getHeaderNameConstant(realParameter.getName());
            params.add("name = %s".formatted(headerNameConstant.value()));
            parameterAnnotation = parameterAnnotation.withAddedConstantValueImports(headerNameConstant);
        } else {
            params.add("name = \"%s\"".formatted(realParameter.getName()));
        }

        params.add("description = \"%s\"".formatted(normalizeDescription(realParameter.getDescription())));

        if (TRUE.equals(realParameter.getRequired())) {
            params.add("required = true");
        }

        if (nonNull(realParameter.getSchema())) {
            AnnotationInfo schemaAnnotation = getSchemaAnnotation(realParameter.getSchema());
            params.add("schema = %s".formatted(schemaAnnotation.annotation()));
            parameterAnnotation = parameterAnnotation.withAddedAllImportsFrom(schemaAnnotation);
        }

        if (nonEmpty(realParameter.getContent())) {
            List<AnnotationInfo> contentAnnotations = new ArrayList<>();
            realParameter.getContent().forEach((contentType, mediaType) ->
                contentAnnotations.add(getContentAnnotation(contentType, mediaType))
            );

            params.add("content = %s".formatted(formatAnnotationNamedParam(contentAnnotations.stream().map(AnnotationInfo::annotation).toList())));
            parameterAnnotation = parameterAnnotation.withAddedAllImportsFrom(contentAnnotations);
        }

        if (TRUE.equals(realParameter.getDeprecated())) {
            params.add("deprecated = true");
        }

        return parameterAnnotation.withAnnotation("@Parameter(%s)".formatted(joinCsv(params)))
            .withAddedImport("org.eclipse.microprofile.openapi.annotations.parameters.Parameter");
    }

    private ConstantValue getParameterInValue(Parameter parameter) {
        String inValue = switch (parameter.getIn().toLowerCase()) {
            case "" -> "DEFAULT";
            case "header" -> "HEADER";
            case "query" -> "QUERY";
            case "path" -> "PATH";
            case "cookie" -> "COOKIE";
            default -> throw new IllegalStateException("Parameter in-value %s not supported".formatted(parameter.getIn()));
        };

        return new ConstantValue(inValue, "org.eclipse.microprofile.openapi.annotations.enums.ParameterIn." + inValue);
    }

    private AnnotationInfo getApiResponseAnnotation(ApiResponse response, String statusCode) {
        ApiResponse realResponse = response;
        if (nonNull(response.get$ref())) {
            realResponse = componentResolver.responses().getOrThrow(response.get$ref());
        }

        AnnotationInfo apiResponseAnnotation = new AnnotationInfo();

        List<String> params = new ArrayList<>();
        params.add("responseCode = \"%s\"".formatted(statusCode));
        params.add("description = \"%s\"".formatted(normalizeDescription(realResponse.getDescription())));

        if (nonEmpty(realResponse.getHeaders())) {
            List<AnnotationInfo> headerAnnotations = new ArrayList<>();
            realResponse.getHeaders().forEach((name, header) ->
                headerAnnotations.add(getHeaderAnnotation(name, header))
            );

            params.add("headers = %s".formatted(
                formatAnnotationNamedParam(headerAnnotations.stream().map(AnnotationInfo::annotation).toList()))
            );

            apiResponseAnnotation = apiResponseAnnotation.withAddedAllImportsFrom(headerAnnotations);
        }

        if (nonEmpty(realResponse.getContent())) {
            List<AnnotationInfo> contentAnnotations = new ArrayList<>();
            realResponse.getContent().forEach((contentType, mediaType) ->
                contentAnnotations.add(getContentAnnotation(contentType, mediaType))
            );

            params.add("content = %s".formatted(
                formatAnnotationNamedParam(contentAnnotations.stream().map(AnnotationInfo::annotation).toList()))
            );

            apiResponseAnnotation = apiResponseAnnotation.withAddedAllImportsFrom(contentAnnotations);
        }

        return apiResponseAnnotation
            .withAnnotation("@APIResponse(%s)".formatted(joinCsv(params)))
            .withAddedImport("org.eclipse.microprofile.openapi.annotations.responses.APIResponse");
    }

    private AnnotationInfo getMethodParameterAnnotation(Parameter parameter) {
        String paramAnnotationName = switch (parameter.getIn().toLowerCase()) {
            case "header" -> "HeaderParam";
            case "query" -> "QueryParam";
            case "path" -> "PathParam";
            case "cookie" -> "CookieParam";
            default -> throw new IllegalStateException("Parameter in-value %s not supported".formatted(parameter.getIn()));
        };

        final String annotationImport = "jakarta.ws.rs." + paramAnnotationName;

        if (paramAnnotationName.equals("HeaderParam")) {
            ConstantValue headerNameConstant = getHeaderNameConstant(parameter.getName());
            return new AnnotationInfo("@%s(%s)".formatted(paramAnnotationName, headerNameConstant.value()))
                .withAddedImport(annotationImport)
                .withAddedConstantValueImports(headerNameConstant);
        } else {
            return new AnnotationInfo("@%s(\"%s\")".formatted(paramAnnotationName, parameter.getName()))
                .withAddedImport(annotationImport);
        }
    }

    private ConstantValue getHeaderNameConstant(String name) {
        String standardHeaderConstant = switch (name.toUpperCase()) {
            case "ACCEPT-LANGUAGE" -> "ACCEPT_LANGUAGE";
            case "CONTENT-LANGUAGE" -> "CONTENT_LANGUAGE";
            case "LOCATION" -> "LOCATION";
            default -> null;
        };

        if (nonNull(standardHeaderConstant)) {
            return new ConstantValue(standardHeaderConstant, "jakarta.ws.rs.core.HttpHeaders." + standardHeaderConstant);
        }

        return new ConstantValue(quote(name));
    }

    private AnnotationInfo getContentAnnotation(String contentType, MediaType mediaType) {
        ConstantValue mediaTypeConstant = getMediaTypeConstant(contentType);
        AnnotationInfo schemaAnnotation = getSchemaAnnotation(mediaType.getSchema());

        return new AnnotationInfo(formatInnerAnnotation("Content(mediaType = %s, schema = %s)", mediaTypeConstant.value(), schemaAnnotation.annotation()))
            .withAddedImport("org.eclipse.microprofile.openapi.annotations.media.Content")
            .withAddedConstantValueImports(mediaTypeConstant)
            .withAddedAllImportsFrom(schemaAnnotation);
    }

    private AnnotationInfo getSchemaAnnotation(Schema<?> schema) {
        AnnotationInfo schemaAnnotation = new AnnotationInfo();

        List<String> params = new ArrayList<>();

        TypeInfo bodyType = typeInfoCollector.getTypeInfo(schema);
        schemaAnnotation = schemaAnnotation.withAddedImports(bodyType.typeImports);

        if (nonNull(bodyType.itemType)) {
            schemaAnnotation = schemaAnnotation
                .withAddedStaticImport("org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY")
                .withAddedImports(bodyType.itemType.typeImports);

            params.add("type = ARRAY");
            bodyType = bodyType.itemType;
        }

        params.add("implementation = %s".formatted(formatClassRef(bodyType.name)));
        if (nonNull(schema.getDefault())) {
            params.add("defaultValue = \"%s\"".formatted(schema.getDefault().toString()));
        }
        if (nonBlank(bodyType.schemaFormat)) {
            params.add("format = \"%s\"".formatted(bodyType.schemaFormat));
        }
        if (nonBlank(bodyType.schemaPattern)) {
            params.add("pattern = \"%s\"".formatted(bodyType.schemaPattern));
        }

        return schemaAnnotation.withAnnotation(formatInnerAnnotation("Schema(%s)", joinCsv(params)))
            .withAddedImport("org.eclipse.microprofile.openapi.annotations.media.Schema");
    }

    private AnnotationInfo getHeaderAnnotation(String name, Header header) {
        Header realHeader = header;
        if (nonNull(header.get$ref())) {
            realHeader = componentResolver.headers().getOrThrow(header.get$ref());
        }

        AnnotationInfo schemaAnnotation = getSchemaAnnotation(realHeader.getSchema());
        return new AnnotationInfo(formatInnerAnnotation("Header(name = \"%s\", description = \"%s\", schema = %s)", name, normalizeDescription(realHeader.getDescription()), schemaAnnotation.annotation()))
            .withAddedImport("org.eclipse.microprofile.openapi.annotations.headers.Header")
            .withAddedAllImportsFrom(schemaAnnotation);
    }

    private String toParamName(String paramName) {
        requireNonNull(paramName);
        if (nonBlank(opts.pojoNameSuffix()) && paramName.endsWith(opts.pojoNameSuffix())) {
            paramName = stripTail(paramName, opts.pojoNameSuffix().length());
        }
        paramName = paramName.replace("-", "");

        // paramName may contain array-symbol, replace with plural "s"
        paramName = paramName.replace("[]", "s");
        return uncapitalize(paramName);
    }

    private Optional<ApiResponse> getSuccessResponse(ApiResponses responses) {
        requireNonNull(responses);
        return responses.keySet().stream()
            .filter(sc -> sc.startsWith("2"))
            .findFirst()
            .map(responses::get);
    }

    private ConstantValue getMediaTypeConstant(String contentType) {
        if (standardContentTypes.containsKey(contentType)) {
            contentType = standardContentTypes.get(contentType);
            return new ConstantValue(contentType, "jakarta.ws.rs.core.MediaType." + contentType);
        } else {
            return new ConstantValue(quote(contentType));
        }
    }
}
