package org.github.torand.openapi2java.utils;

import java.util.function.Supplier;

public class Exceptions {
    private Exceptions() {}

    public static Supplier<IllegalStateException> illegalStateException(String message, Object... args) {
        return () -> new IllegalStateException(message.formatted(args));
    }
}
