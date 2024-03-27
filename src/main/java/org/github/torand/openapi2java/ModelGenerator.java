package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.github.torand.openapi2java.collectors.ComponentResolver;
import org.github.torand.openapi2java.collectors.EnumInfoCollector;
import org.github.torand.openapi2java.collectors.PojoInfoCollector;
import org.github.torand.openapi2java.collectors.SchemaResolver;
import org.github.torand.openapi2java.collectors.TypeInfoCollector;
import org.github.torand.openapi2java.model.EnumInfo;
import org.github.torand.openapi2java.model.PojoInfo;
import org.github.torand.openapi2java.model.TypeInfo;
import org.github.torand.openapi2java.writers.EnumWriter;
import org.github.torand.openapi2java.writers.PojoWriter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.nonNull;
import static org.github.torand.openapi2java.utils.CollectionHelper.isEmpty;
import static org.github.torand.openapi2java.utils.CollectionHelper.nonEmpty;
import static org.github.torand.openapi2java.utils.CollectionHelper.streamSafely;
import static org.github.torand.openapi2java.utils.StringHelper.nonBlank;
import static org.github.torand.openapi2java.utils.StringHelper.pluralSuffix;
import static org.github.torand.openapi2java.utils.StringHelper.stripTail;
import static org.github.torand.openapi2java.writers.WriterFactory.createEnumWriter;
import static org.github.torand.openapi2java.writers.WriterFactory.createPojoWriter;

public class ModelGenerator {

    private final Options opts;

    public ModelGenerator(Options opts) {
        this.opts = opts;
    }

    public void generate(OpenAPI openApiDoc) {
        ComponentResolver componentResolver = new ComponentResolver(openApiDoc);
        PojoInfoCollector pojoInfoCollector = new PojoInfoCollector(componentResolver.schemas(), opts);
        EnumInfoCollector enumInfoCollector = new EnumInfoCollector(opts);

        // Generate pojos and enums referenced by included tags only
        Set<String> relevantPojos = getRelevantPojos(openApiDoc, componentResolver);

        AtomicInteger enumCount = new AtomicInteger(0);
        AtomicInteger pojoCount = new AtomicInteger(0);

        openApiDoc.getComponents().getSchemas().entrySet().forEach(entry -> {
            String pojoName = entry.getKey() + opts.pojoNameSuffix;
            if (relevantPojos.contains(pojoName)) {
                if (isEnum(entry.getValue())) {
                    enumCount.incrementAndGet();
                    if (opts.verbose) {
                        System.out.println("Generating model enum %s".formatted(pojoName));
                    }

                    EnumInfo enumInfo = enumInfoCollector.getEnumInfo(pojoName, entry.getValue());

                    String enumFilename = pojoName + opts.getFileExtension();
                    try (EnumWriter enumWriter = createEnumWriter(enumFilename, opts)) {
                        enumWriter.write(enumInfo);
                    } catch (IOException e) {
                        System.out.println("Failed to write file %s: %s".formatted(enumFilename, e.toString()));
                    }
                }

                if (isClass(entry.getValue())) {
                    pojoCount.incrementAndGet();
                    if (opts.verbose) {
                        System.out.println("Generating model class %s".formatted(pojoName));
                    }

                    PojoInfo pojoInfo = pojoInfoCollector.getPojoInfo(pojoName, entry.getValue());

                    String pojoFilename = pojoName + opts.getFileExtension();
                    try (PojoWriter pojoWriter = createPojoWriter(pojoFilename, opts)) {
                        pojoWriter.write(pojoInfo);
                    } catch (IOException e) {
                        System.out.println("Failed to write file %s: %s".formatted(pojoFilename, e.toString()));
                    }
                }
            }
        });

        System.out.println("Generated %d enum%s, %d pojo%s in directory %s".formatted(enumCount.get(), pluralSuffix(enumCount.get()), pojoCount.get(), pluralSuffix(pojoCount.get()), opts.getModelOutputDir()));
    }

    private Set<String> getRelevantPojos(OpenAPI openApiDoc, ComponentResolver componentResolver) {
        Set<String> relevantPojos = new HashSet<>();
        openApiDoc.getPaths().forEach((path, action) -> {
            if (nonNull(action.getGet())) {
                relevantPojos.addAll(getRelevantPojosForOperation(action.getGet(), componentResolver));
            }
            if (nonNull(action.getPost())) {
                relevantPojos.addAll(getRelevantPojosForOperation(action.getPost(), componentResolver));
            }
            if (nonNull(action.getDelete())) {
                relevantPojos.addAll(getRelevantPojosForOperation(action.getDelete(), componentResolver));
            }
            if (nonNull(action.getPut())) {
                relevantPojos.addAll(getRelevantPojosForOperation(action.getPut(), componentResolver));
            }
            if (nonNull(action.getPatch())) {
                relevantPojos.addAll(getRelevantPojosForOperation(action.getPatch(), componentResolver));
            }
        });

        return relevantPojos;
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
                        relevantPojos.add(getPojoTypeName(realParameter.getSchema(), typeInfoCollector));
                    }

                    if (nonEmpty(realParameter.getContent())) {
                        realParameter.getContent().forEach((contentType, mediaType) -> {
                            if (nonNull(mediaType.getSchema())) {
                                relevantPojos.add(getPojoTypeName(mediaType.getSchema(), typeInfoCollector));
                            }
                        });
                    }
                });
            }
            if (nonNull(operation.getRequestBody())) {
                if (nonEmpty(operation.getRequestBody().getContent())) {
                    operation.getRequestBody().getContent().forEach((contentType, mediaType) -> {
                        if (nonNull(mediaType.getSchema())) {
                            relevantPojos.add(getPojoTypeName(mediaType.getSchema(), typeInfoCollector));
                        }
                    });
                }
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
                                relevantPojos.add(getPojoTypeName(mediaType.getSchema(), typeInfoCollector));
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
            String schemaRef = "#/components/schemas/" + stripTail(pojo, opts.pojoNameSuffix.length());
            schemaResolver.get(schemaRef).ifPresent(schema -> nestedPojos.addAll(getNestedSchemaTypes(schema, schemaResolver, typeInfoCollector)));
        });

        return nestedPojos;
    }

    private Set<String> getNestedSchemaTypes(Schema<?> parentSchema, SchemaResolver schemaResolver, TypeInfoCollector typeInfoCollector) {
        Set<String> schemaTypes = new HashSet<>();

        if (nonEmpty(parentSchema.getAllOf())) {
            parentSchema.getAllOf().forEach(subSchema -> schemaTypes.addAll(getNestedSchemaTypes(subSchema, schemaResolver, typeInfoCollector)));
        } else if (nonBlank(parentSchema.get$ref())) {
            schemaTypes.add(getPojoTypeName(parentSchema, typeInfoCollector));
            Schema<?> $refSchema = schemaResolver.getOrThrow(parentSchema.get$ref());
            schemaTypes.addAll(getNestedSchemaTypes($refSchema, schemaResolver, typeInfoCollector));
        } else if (nonEmpty(parentSchema.getProperties())) {
            parentSchema.getProperties().forEach((String k, Schema v) -> {
                schemaTypes.addAll(getNestedSchemaTypes(v, schemaResolver, typeInfoCollector));
            });
        } else if (nonNull(parentSchema.getItems())) {
            schemaTypes.addAll(getNestedSchemaTypes(parentSchema.getItems(), schemaResolver, typeInfoCollector));
        }

        return schemaTypes;
    }

    private String getPojoTypeName(Schema schema, TypeInfoCollector typeInfoCollector) {
        TypeInfo bodyType = typeInfoCollector.getTypeInfo((JsonSchema)schema);
        if (nonNull(bodyType.itemType)) {
            return bodyType.itemType.name;
        } else {
            return bodyType.name;
        }
    }

    private boolean isRelevantTag(Operation operation) {
        return isEmpty(opts.includeTags) || streamSafely(operation.getTags()).anyMatch(tag -> opts.includeTags.contains(tag));
    }

    private boolean isEnum(Schema schema) {
        return streamSafely(schema.getTypes()).anyMatch("string"::equals) && nonNull(schema.getEnum());
    }

    private boolean isClass(Schema schema) {
        return streamSafely(schema.getTypes()).anyMatch("object"::equals) || nonNull(schema.getAllOf());
    }
}
