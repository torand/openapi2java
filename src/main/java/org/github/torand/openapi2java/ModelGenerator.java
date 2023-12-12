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
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.nonNull;
import static org.github.torand.openapi2java.utils.StringHelper.pluralSuffix;

public class ModelGenerator {

    public void generate(OpenAPI openApiDoc, Options opts) {
        Path outputPath = Path.of(opts.getModelOutputDir());
        File f = outputPath.toFile();
        if (!f.exists()) {
            f.mkdirs();
        }

        SchemaResolver schemaResolver = new SchemaResolver(openApiDoc.getComponents().getSchemas());

        AtomicInteger enumCount = new AtomicInteger(0);
        AtomicInteger pojoCount = new AtomicInteger(0);

        openApiDoc.getComponents().getSchemas().entrySet().forEach(entry -> {
            String pojoName = entry.getKey() + opts.pojoNameSuffix;

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
        });

        System.out.println("Generated %d enum%s, %d pojo%s in directory %s".formatted(enumCount.get(), pluralSuffix(enumCount.get()), pojoCount.get(), pluralSuffix(pojoCount.get()), outputPath));
    }

    private static boolean isEnum(Schema schema) {
        return nonNull(schema.getTypes()) && schema.getTypes().contains("string") && nonNull(schema.getEnum());
    }

    private static boolean isClass(Schema schema) {
        return (nonNull(schema.getTypes()) && schema.getTypes().contains("object")) || nonNull(schema.getAllOf());
    }
}
