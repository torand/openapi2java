package org.github.torand.openapi2java.writers;

import org.github.torand.openapi2java.Options;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public final class WriterFactory {
    private WriterFactory() {}

    public static EnumWriter createEnumWriter(String filename, Options opts) throws IOException {
        Writer fileWriter = createFileWriter(filename, opts.getModelOutputDir());
        return new EnumWriter(fileWriter, opts);
    }

    public static PojoWriter createPojoWriter(String filename, Options opts) throws IOException {
        Writer fileWriter = createFileWriter(filename, opts.getModelOutputDir());
        return new PojoWriter(fileWriter, opts);
    }

    public static ResourceWriter createResourceWriter(String filename, Options opts) throws IOException {
        Writer fileWriter = createFileWriter(filename, opts.outputDir);
        return new ResourceWriter(fileWriter, opts);
    }

    public static OpenApiDefWriter createOpenApiDefWriter(String filename, Options opts) throws IOException {
        Writer fileWriter = createFileWriter(filename, opts.outputDir);
        return new OpenApiDefWriter(fileWriter, opts);
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
