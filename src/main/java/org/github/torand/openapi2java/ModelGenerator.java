package org.github.torand.openapi2java;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.github.torand.openapi2java.collectors.SchemaResolver;
import org.github.torand.openapi2java.writers.EnumWriter;
import org.github.torand.openapi2java.writers.PojoWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

import static java.util.Objects.nonNull;

public class ModelGenerator {

    public void generate(OpenAPI openApiDoc, Options opts) {
        Path outputPath = Path.of(opts.getModelOutputDir());
        File f = outputPath.toFile();
        if (!f.exists()) {
            f.mkdirs();
        }

        SchemaResolver schemaResolver = new SchemaResolver(openApiDoc.getComponents().getSchemas());

        System.out.println("Generating model...");

        openApiDoc.getComponents().getSchemas().entrySet().forEach(entry -> {

            if (isEnum(entry.getValue())) {
                System.out.println("  Generating model enum %s".formatted(entry.getKey()));
                String enumFileName = entry.getKey() + ".java";
                File enumFile = new File(f, enumFileName);
                try (Writer writer = new FileWriter(enumFile)) {
                    EnumWriter enumWriter = new EnumWriter(writer, opts);
                    enumWriter.write(entry.getKey(), entry.getValue());
                } catch (IOException e) {
                    System.out.println("Failed to write file %s: %s".formatted(enumFileName, e.toString()));
                }
            }

            if (isClass(entry.getValue())) {
                System.out.println("  Generating model class %s".formatted(entry.getKey()));
                String classFileName = entry.getKey() + ".java";
                File classFile = new File(f, classFileName);
                try (Writer writer = new FileWriter(classFile)) {
                    PojoWriter pojoWriter = new PojoWriter(writer, schemaResolver, opts);
                    pojoWriter.write(entry.getKey(), entry.getValue());
                } catch (IOException e) {
                    System.out.println("Failed to write file %s: %s".formatted(classFileName, e.toString()));
                }
            }
        });
    }

    public static boolean isEnum(Schema schema) {
        return nonNull(schema.getTypes()) && schema.getTypes().contains("string") && nonNull(schema.getEnum());
    }

    public static boolean isClass(Schema schema) {
        return (nonNull(schema.getTypes()) && schema.getTypes().contains("object")) || nonNull(schema.getAllOf());
    }
}
