package org.github.torand.openapi2java.writers;

import org.github.torand.openapi2java.model.OpenApiDefInfo;

import java.io.IOException;

public interface OpenApiDefWriter extends AutoCloseable {
    void write(OpenApiDefInfo openApiDefInfo);

    @Override
    void close() throws IOException;
}
