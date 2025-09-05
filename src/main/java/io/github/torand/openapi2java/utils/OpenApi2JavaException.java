package io.github.torand.openapi2java.utils;

/**
 * Generic runtime exception thrown by this plugin,
 */
public class OpenApi2JavaException extends RuntimeException {

    /**
     * Creates a runtime exception.
     * @param message the message.
     */
    public OpenApi2JavaException(String message) {
        super(message);
    }

    /**
     * Creates a runtime exception.
     * @param message the message.
     * @param cause the inner cause.
     */
    public OpenApi2JavaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a runtime exception.
     * @param cause the inner cause.
     */
    public OpenApi2JavaException(Throwable cause) {
        super(cause);
    }
}
