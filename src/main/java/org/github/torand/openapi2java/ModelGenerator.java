package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.github.torand.openapi2java.collectors.ParameterResolver;
import org.github.torand.openapi2java.collectors.ResponseResolver;
import org.github.torand.openapi2java.collectors.SchemaResolver;
import org.github.torand.openapi2java.collectors.TypeInfoCollector;
import org.github.torand.openapi2java.model.TypeInfo;
import org.github.torand.openapi2java.writers.EnumWriter;
import org.github.torand.openapi2java.writers.PojoWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.github.torand.openapi2java.utils.StringHelper.pluralSuffix;

public class ModelGenerator {

    private final Options opts;

    public ModelGenerator(Options opts) {
        this.opts = opts;
    }

    public void generate(OpenAPI openApiDoc) {
        Path outputPath = Path.of(opts.getModelOutputDir());
        File f = outputPath.toFile();
        if (!f.exists()) {
            f.mkdirs();
        }

        SchemaResolver schemaResolver = new SchemaResolver(openApiDoc.getComponents().getSchemas());

        // Generate pojos referenced by included tags only
        Set<String> relevantPojos = getRelevantPojos(openApiDoc, schemaResolver);

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
                    String enumFileName = pojoName + ".java";
                    File enumFile = new File(f, enumFileName);
                    try (Writer writer = new FileWriter(enumFile)) {
                        EnumWriter enumWriter = new EnumWriter(writer, opts);
                        enumWriter.write(pojoName, entry.getValue());
                    } catch (IOException e) {
                        System.out.println("Failed to write file %s: %s".formatted(enumFileName, e.toString()));
                    }
                }

                if (isClass(entry.getValue())) {
                    pojoCount.incrementAndGet();
                    if (opts.verbose) {
                        System.out.println("Generating model class %s".formatted(pojoName));
                    }
                    String pojoFileName = pojoName + ".java";
                    File pojoFile = new File(f, pojoFileName);
                    try (Writer writer = new FileWriter(pojoFile)) {
                        PojoWriter pojoWriter = new PojoWriter(writer, schemaResolver, opts);
                        pojoWriter.write(pojoName, entry.getValue());
                    } catch (IOException e) {
                        System.out.println("Failed to write file %s: %s".formatted(pojoFileName, e.toString()));
                    }
                }
            }
        });

        System.out.println("Generated %d enum%s, %d pojo%s in directory %s".formatted(enumCount.get(), pluralSuffix(enumCount.get()), pojoCount.get(), pluralSuffix(pojoCount.get()), outputPath));
    }

    private Set<String> getRelevantPojos(OpenAPI openApiDoc, SchemaResolver schemaResolver) {
        ResponseResolver responseResolver = new ResponseResolver(openApiDoc.getComponents().getResponses());
        ParameterResolver parameterResolver = new ParameterResolver(openApiDoc.getComponents().getParameters());

        Set<String> relevantPojos = new HashSet<>();
        openApiDoc.getPaths().forEach((path, action) -> {
            if (nonNull(action.getGet())) {
                relevantPojos.addAll(getRelevantPojosForOperation(action.getGet(), schemaResolver, responseResolver, parameterResolver));
            }
            if (nonNull(action.getPost())) {
                relevantPojos.addAll(getRelevantPojosForOperation(action.getPost(), schemaResolver, responseResolver, parameterResolver));
            }
            if (nonNull(action.getDelete())) {
                relevantPojos.addAll(getRelevantPojosForOperation(action.getDelete(), schemaResolver, responseResolver, parameterResolver));
            }
            if (nonNull(action.getPut())) {
                relevantPojos.addAll(getRelevantPojosForOperation(action.getPut(), schemaResolver, responseResolver, parameterResolver));
            }
            if (nonNull(action.getPatch())) {
                relevantPojos.addAll(getRelevantPojosForOperation(action.getPatch(), schemaResolver, responseResolver, parameterResolver));
            }
        });

        return relevantPojos;
    }

    private Set<String> getRelevantPojosForOperation(Operation operation, SchemaResolver schemaResolver, ResponseResolver responseResolver, ParameterResolver parameterResolver) {
        TypeInfoCollector typeInfoCollector = new TypeInfoCollector(schemaResolver, opts);

        Set<String> relevantPojos = new HashSet<>();

        if (isNull(operation.getTags()) || operation.getTags().isEmpty() || isRelevantTag(operation)) {
            if (nonNull(operation.getParameters())) {
                operation.getParameters().forEach(parameter -> {
                    Parameter realParameter = parameter;
                    if (nonNull(parameter.get$ref())) {
                        realParameter = parameterResolver.get(parameter.get$ref())
                            .orElseThrow(() -> new IllegalStateException("Parameter %s not found".formatted(parameter.get$ref())));
                    }

                    if (nonNull(realParameter.getSchema())) {
                        relevantPojos.add(getPojoTypeName(realParameter.getSchema(), typeInfoCollector));
                    }

                    if (nonNull(realParameter.getContent())) {
                        realParameter.getContent().forEach((contentType, mediaType) -> {
                            if (nonNull(mediaType.getSchema())) {
                                relevantPojos.add(getPojoTypeName(mediaType.getSchema(), typeInfoCollector));
                            }
                        });
                    }
                });
            }
            if (nonNull(operation.getRequestBody())) {
                if (nonNull(operation.getRequestBody().getContent())) {
                    operation.getRequestBody().getContent().forEach((contentType, mediaType) -> {
                        if (nonNull(mediaType.getSchema())) {
                            relevantPojos.add(getPojoTypeName(mediaType.getSchema(), typeInfoCollector));
                        }
                    });
                }
            }
            if (nonNull(operation.getResponses())) {
                operation.getResponses().forEach((code, response) -> {
                    ApiResponse realResponse = response;
                    if (nonNull(response.get$ref())) {
                        realResponse = responseResolver.get(response.get$ref())
                            .orElseThrow(() -> new IllegalStateException("Response %s not found".formatted(response.get$ref())));
                    }

                    if (nonNull(realResponse.getContent())) {
                        realResponse.getContent().forEach((contentType, mediaType) -> {
                            if (nonNull(mediaType.getSchema())) {
                                relevantPojos.add(getPojoTypeName(mediaType.getSchema(), typeInfoCollector));
                            }
                        });
                    }
                });
            }
        }

        relevantPojos.addAll(getNestedPojos(relevantPojos, schemaResolver));

        return relevantPojos;
    }

    private Set<String> getNestedPojos(Set<String> relevantPojos, SchemaResolver schemaResolver) {
        TypeInfoCollector typeInfoCollector = new TypeInfoCollector(schemaResolver, opts);

        Set<String> nestedPojos = new HashSet<>();
        relevantPojos.forEach(pojo -> {
                String schemaRef = "#/components/schemas/" + pojo.substring(0, pojo.length()-opts.pojoNameSuffix.length());
                schemaResolver.get(schemaRef).ifPresent(schema -> nestedPojos.addAll(getNestedSchemaTypes(schema, schemaResolver, typeInfoCollector)));
            }
        );

        return nestedPojos;
    }

    private Set<String> getNestedSchemaTypes(Schema<?> schema, SchemaResolver schemaResolver, TypeInfoCollector typeInfoCollector) {
        Set<String> schemaTypes = new HashSet<>();

        if (nonNull(schema.getAllOf())) {
            schema.getAllOf().forEach(subSchema -> schemaTypes.addAll(getNestedSchemaTypes(subSchema, schemaResolver, typeInfoCollector)));
        } else if (nonNull(schema.get$ref())) {
            schemaTypes.add(getPojoTypeName(schema, typeInfoCollector));
            Schema<?> $refSchema = schemaResolver.get(schema.get$ref())
                .orElseThrow(() -> new IllegalStateException("Schema not found: " + schema.get$ref()));
            schemaTypes.addAll(getNestedSchemaTypes($refSchema, schemaResolver, typeInfoCollector));
        } else if (nonNull(schema.getProperties())) {
            schema.getProperties().forEach((String k, Schema v) -> {
                schemaTypes.addAll(getNestedSchemaTypes(v, schemaResolver, typeInfoCollector));
            });
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
        return opts.includeTags.isEmpty() || operation.getTags().stream().anyMatch(tag -> opts.includeTags.contains(tag));
    }

    private boolean isEnum(Schema schema) {
        return nonNull(schema.getTypes()) && schema.getTypes().contains("string") && nonNull(schema.getEnum());
    }

    private boolean isClass(Schema schema) {
        return (nonNull(schema.getTypes()) && schema.getTypes().contains("object")) || nonNull(schema.getAllOf());
    }
}
