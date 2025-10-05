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
package io.github.torand.openapi2java.generators;

import io.github.torand.openapi2java.collectors.*;
import io.github.torand.openapi2java.model.EnumInfo;
import io.github.torand.openapi2java.model.PojoInfo;
import io.github.torand.openapi2java.model.TypeInfo;
import io.github.torand.openapi2java.utils.OpenApi2JavaException;
import io.github.torand.openapi2java.writers.EnumWriter;
import io.github.torand.openapi2java.writers.PojoWriter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.github.torand.javacommons.collection.CollectionHelper.isEmpty;
import static io.github.torand.javacommons.collection.CollectionHelper.nonEmpty;
import static io.github.torand.javacommons.lang.Exceptions.illegalStateException;
import static io.github.torand.javacommons.lang.StringHelper.nonBlank;
import static io.github.torand.javacommons.lang.StringHelper.stripTail;
import static io.github.torand.javacommons.stream.StreamHelper.streamSafely;
import static io.github.torand.openapi2java.collectors.SchemaResolver.isObjectType;
import static io.github.torand.openapi2java.utils.StringUtils.pluralSuffix;
import static io.github.torand.openapi2java.writers.WriterFactory.createEnumWriter;
import static io.github.torand.openapi2java.writers.WriterFactory.createPojoWriter;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

/**
 * Generates source code for models (pojos).
 */
public class ModelGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ModelGenerator.class);

    private final Options opts;

    public ModelGenerator(Options opts) {
        this.opts = opts;
    }

    public void generate(OpenAPI openApiDoc) {
        ComponentResolver componentResolver = new ComponentResolver(openApiDoc);

        // Generate pojos and enums referenced by included tags only
        Set<String> relevantPojos = getRelevantPojos(openApiDoc, componentResolver);

        AtomicInteger enumCount = new AtomicInteger(0);
        AtomicInteger pojoCount = new AtomicInteger(0);

        openApiDoc.getComponents().getSchemas().forEach((name, schema) -> {
            String pojoName = name + opts.pojoNameSuffix();
            if (relevantPojos.contains(pojoName)) {

                if (isEnum(schema)) {
                    generateEnumFile(pojoName, schema);
                    enumCount.incrementAndGet();
                }

                if (isClass(schema)) {
                    generatePojoFile(pojoName, schema, componentResolver.schemas());
                    pojoCount.incrementAndGet();
                }
            }
        });

        if (logger.isInfoEnabled()) {
            logger.info("Generated {} enum{}, {} pojo{} in directory {}", enumCount.get(), pluralSuffix(enumCount.get()), pojoCount.get(), pluralSuffix(pojoCount.get()), opts.getModelOutputDir(null));
        }
    }

    private void generateEnumFile(String name, Schema<?> schema) {
        if (opts.verbose()) {
            logger.info("Generating model enum {}", name);
        }

        EnumInfoCollector enumInfoCollector = new EnumInfoCollector(opts);
        EnumInfo enumInfo = enumInfoCollector.getEnumInfo(name, schema);

        String enumFilename = name + opts.getFileExtension();
        try (EnumWriter enumWriter = createEnumWriter(enumFilename, opts, enumInfo.modelSubdir())) {
            enumWriter.write(enumInfo);
        } catch (IOException e) {
            throw new OpenApi2JavaException("Failed to write file %s".formatted(enumFilename), e);
        }
    }

    private void generatePojoFile(String name, Schema<?> schema, SchemaResolver schemaResolver) {
        if (opts.verbose()) {
            logger.info("Generating model class {}", name);
        }

        PojoInfoCollector pojoInfoCollector = new PojoInfoCollector(schemaResolver, opts);
        PojoInfo pojoInfo = pojoInfoCollector.getPojoInfo(name, schema);

        String pojoFilename = name + opts.getFileExtension();
        try (PojoWriter pojoWriter = createPojoWriter(pojoFilename, opts, pojoInfo.modelSubdir())) {
            pojoWriter.write(pojoInfo);
        } catch (IOException e) {
            throw new OpenApi2JavaException("Failed to write file %s".formatted(pojoFilename), e);
        }
    }

    private Set<String> getRelevantPojos(OpenAPI openApiDoc, ComponentResolver componentResolver) {
        record PathOperation(String path, String method, Operation operation) {}

        return openApiDoc.getPaths().entrySet().stream()
            .flatMap(entry -> {
                var path = entry.getKey();
                var pathItem = entry.getValue();

                return Stream.of(
                    new PathOperation(path, "GET", pathItem.getGet()),
                    new PathOperation(path, "PUT", pathItem.getPut()),
                    new PathOperation(path, "POST", pathItem.getPost()),
                    new PathOperation(path, "DELETE", pathItem.getDelete()),
                    new PathOperation(path, "PATCH", pathItem.getPatch())
                ).filter(po -> po.operation != null);
            })
            .map(pathOperation -> {
                try {
                    if (opts.verbose()) {
                        logger.info("Getting relevant Pojos for {} {}", pathOperation.method, pathOperation.path);
                    }

                    return getRelevantPojosForOperation(pathOperation.operation(), componentResolver);
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException("Failed to get relevant Pojos for %s %s"
                        .formatted(pathOperation.method, pathOperation.path), e);
                }
            })
            .flatMap(Collection::stream)
            .collect(toSet());
    }

    private Set<String> getRelevantPojosForOperation(Operation operation, ComponentResolver componentResolver) {
        TypeInfoCollector typeInfoCollector = new TypeInfoCollector(componentResolver.schemas(), opts);

        Set<String> relevantPojos = new HashSet<>();

        if (isEmpty(operation.getTags()) || isRelevantTag(operation)) {
            if (nonEmpty(operation.getParameters())) {
                operation.getParameters().forEach(parameter -> {
                    Parameter realParameter = parameter;
                    if (nonBlank(parameter.get$ref())) {
                        realParameter = componentResolver.parameters().getOrThrow(parameter.get$ref());
                    }

                    if (nonNull(realParameter.getSchema())) {
                        getPojoTypeName(realParameter.getSchema(), typeInfoCollector).ifPresent(relevantPojos::add);
                    }

                    if (nonEmpty(realParameter.getContent())) {
                        realParameter.getContent().forEach((contentType, mediaType) -> {
                            if (nonNull(mediaType.getSchema())) {
                                getPojoTypeName(mediaType.getSchema(), typeInfoCollector).ifPresent(relevantPojos::add);
                            }
                        });
                    }
                });
            }
            if (nonNull(operation.getRequestBody()) && nonEmpty(operation.getRequestBody().getContent())) {
                operation.getRequestBody().getContent().forEach((contentType, mediaType) -> {
                    if (nonNull(mediaType.getSchema())) {
                        getPojoTypeName(mediaType.getSchema(), typeInfoCollector).ifPresent(relevantPojos::add);
                    }
                });
            }
            if (nonEmpty(operation.getResponses())) {
                operation.getResponses().forEach((code, response) -> {
                    ApiResponse realResponse = response;
                    if (nonBlank(response.get$ref())) {
                        realResponse = componentResolver.responses().getOrThrow(response.get$ref());
                    }

                    if (nonEmpty(realResponse.getContent())) {
                        realResponse.getContent().forEach((contentType, mediaType) -> {

                            if (nonNull(mediaType.getSchema())) {
                                getPojoTypeName(mediaType.getSchema(), typeInfoCollector).ifPresent(relevantPojos::add);
                            }
                        });
                    }
                });
            }
        }

        relevantPojos.addAll(getNestedPojos(relevantPojos, componentResolver.schemas()));

        return relevantPojos;
    }

    private Set<String> getNestedPojos(Set<String> parentPojos, SchemaResolver schemaResolver) {
        TypeInfoCollector typeInfoCollector = new TypeInfoCollector(schemaResolver, opts);

        Set<String> nestedPojos = new HashSet<>();
        parentPojos.forEach(pojo -> {
            String schemaRef = "#/components/schemas/" + stripTail(pojo, opts.pojoNameSuffix().length());
            schemaResolver.get(schemaRef).ifPresent(schema -> nestedPojos.addAll(getNestedSchemaTypes(schema, schemaResolver, typeInfoCollector)));
        });

        return nestedPojos;
    }

    private Set<String> getNestedSchemaTypes(Schema<?> parentSchema, SchemaResolver schemaResolver, TypeInfoCollector typeInfoCollector) {
        Set<String> schemaTypes = new HashSet<>();

        if (nonEmpty(parentSchema.getAllOf())) {
            parentSchema.getAllOf().forEach(subSchema -> schemaTypes.addAll(getNestedSchemaTypes(subSchema, schemaResolver, typeInfoCollector)));
        } else if (nonEmpty(parentSchema.getOneOf())) {
            List<Schema<?>> oneOfSchemas = (List<Schema<?>>)(Object)parentSchema.getOneOf();
            Schema<?> subSchema = typeInfoCollector.getNonNullableSubSchema(oneOfSchemas)
                .orElseThrow(illegalStateException("Schema 'oneOf' must contain a non-nullable sub-schema"));
            schemaTypes.addAll(getNestedSchemaTypes(subSchema, schemaResolver, typeInfoCollector));
        } else if (nonBlank(parentSchema.get$ref())) {
            getPojoTypeName(parentSchema, typeInfoCollector).ifPresent(schemaTypes::add);
            Schema<?> refSchema = schemaResolver.getOrThrow(parentSchema.get$ref());
            schemaTypes.addAll(getNestedSchemaTypes(refSchema, schemaResolver, typeInfoCollector));
        } else if (nonEmpty(parentSchema.getProperties())) {
            parentSchema.getProperties().forEach((propName, propSchema) ->
                schemaTypes.addAll(getNestedSchemaTypes(propSchema, schemaResolver, typeInfoCollector))
            );
        } else if (nonNull(parentSchema.getItems())) {
            schemaTypes.addAll(getNestedSchemaTypes(parentSchema.getItems(), schemaResolver, typeInfoCollector));
        }

        return schemaTypes;
    }

    private Optional<String> getPojoTypeName(Schema<?> schema, TypeInfoCollector typeInfoCollector) {
        if (isObjectType(schema)) {
            return Optional.empty(); // Inline type, not a component
        }
        TypeInfo bodyType = typeInfoCollector.getTypeInfo(schema);
        if (nonNull(bodyType.itemType())) {
            return Optional.of(bodyType.itemType().name());
        } else {
            return Optional.of(bodyType.name());
        }
    }

    private boolean isRelevantTag(Operation operation) {
        return isEmpty(opts.includeTags()) || streamSafely(operation.getTags()).anyMatch(tag -> opts.includeTags().contains(tag));
    }

    private boolean isEnum(Schema<?> schema) {
        return streamSafely(schema.getTypes()).anyMatch("string"::equals) && nonNull(schema.getEnum());
    }

    private boolean isClass(Schema<?> schema) {
        return streamSafely(schema.getTypes()).anyMatch("object"::equals) || nonNull(schema.getAllOf());
    }
}
