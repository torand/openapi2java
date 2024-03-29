package org.github.torand.openapi2java.writers;

import org.github.torand.openapi2java.model.ResourceInfo;

import java.io.IOException;

public interface ResourceWriter extends AutoCloseable {
    void write(ResourceInfo resourceInfo);

    @Override
    void close() throws IOException;
}
