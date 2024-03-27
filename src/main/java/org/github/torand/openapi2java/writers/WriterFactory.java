package org.github.torand.openapi2java.writers;

import org.github.torand.openapi2java.Options;
import org.github.torand.openapi2java.writers.java.JavaEnumWriter;
import org.github.torand.openapi2java.writers.java.JavaOpenApiDefWriter;
import org.github.torand.openapi2java.writers.java.JavaPojoWriter;
import org.github.torand.openapi2java.writers.java.JavaResourceWriter;
import org.github.torand.openapi2java.writers.kotlin.KotlinEnumWriter;
import org.github.torand.openapi2java.writers.kotlin.KotlinOpenApiDefWriter;
import org.github.torand.openapi2java.writers.kotlin.KotlinPojoWriter;
import org.github.torand.openapi2java.writers.kotlin.KotlinResourceWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public final class WriterFactory {
    private WriterFactory() {}

    public static EnumWriter createEnumWriter(String filename, Options opts) throws IOException {
        Writer fileWriter = createFileWriter(filename, opts.getModelOutputDir());
        return opts.useKotlinSyntax ? new KotlinEnumWriter(fileWriter, opts) : new JavaEnumWriter(fileWriter, opts);
    }

    public static PojoWriter createPojoWriter(String filename, Options opts) throws IOException {
        Writer fileWriter = createFileWriter(filename, opts.getModelOutputDir());
        return opts.useKotlinSyntax ? new KotlinPojoWriter(fileWriter, opts) : new JavaPojoWriter(fileWriter, opts);
    }

    public static ResourceWriter createResourceWriter(String filename, Options opts) throws IOException {
        Writer fileWriter = createFileWriter(filename, opts.outputDir);
        return opts.useKotlinSyntax ? new KotlinResourceWriter(fileWriter, opts) : new JavaResourceWriter(fileWriter, opts);
    }

    public static OpenApiDefWriter createOpenApiDefWriter(String filename, Options opts) throws IOException {
        Writer fileWriter = createFileWriter(filename, opts.outputDir);
        return opts.useKotlinSyntax ? new KotlinOpenApiDefWriter(fileWriter, opts) : new JavaOpenApiDefWriter(fileWriter, opts);
    }

    private static Writer createFileWriter(String filename, String directory) throws IOException {
        Path outputPath = Path.of(directory);
        File outputPathFile = outputPath.toFile();
        if (!outputPathFile.exists()) {
            outputPathFile.mkdirs();
        }

        File outputFile = new File(outputPathFile, filename);
        return new FileWriter(outputFile);
    }
}
