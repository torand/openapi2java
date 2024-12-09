package io.github.torand.openapi2java.writers;

import io.github.torand.openapi2java.model.EnumInfo;

import java.io.IOException;

public interface EnumWriter extends AutoCloseable {
    void write(EnumInfo enumInfo);

    @Override
    void close() throws IOException;
}
