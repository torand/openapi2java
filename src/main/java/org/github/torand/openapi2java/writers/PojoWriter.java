package org.github.torand.openapi2java.writers;

import org.github.torand.openapi2java.model.PojoInfo;

import java.io.IOException;

public interface PojoWriter extends AutoCloseable {
    void write(PojoInfo pojoInfo);

    @Override
    void close() throws IOException;
}
