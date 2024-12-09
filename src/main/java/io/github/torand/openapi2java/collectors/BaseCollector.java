package io.github.torand.openapi2java.collectors;

import io.github.torand.openapi2java.Options;

import java.util.List;

import static io.github.torand.openapi2java.utils.StringHelper.nonBlank;
import static io.github.torand.openapi2java.utils.StringHelper.stripHead;
import static io.github.torand.openapi2java.utils.StringHelper.stripTail;

public abstract class BaseCollector {

    protected final Options opts;

    protected BaseCollector(Options opts) {
        this.opts = opts;
    }

    protected String normalizeDescription(String description) {
        return nonBlank(description) ? description.replaceAll("%", "%%") : "TBD";
    }

    protected String normalizePath(String path) {
        if (path.startsWith("/")) {
            path = stripHead(path, 1);
        }
        if (path.endsWith("/")) {
            path = stripTail(path, 1);
        }
        return path;
    }

    protected String formatClassRef(String className) {
        return "%s%sclass".formatted(className, opts.useKotlinSyntax ? "::" : ".");
    }

    protected String formatInnerAnnotation(String annotation, Object... args) {
        return (opts.useKotlinSyntax ? "" : "@") + annotation.formatted(args);
    }

    protected String formatAnnotationDefaultParam(List<String> value) {
        if (value.size() == 1) {
            return value.get(0);
        }
        if (opts.useKotlinSyntax) {
            return String.join(", ", value);
        } else {
            return "{" + String.join(", ", value) + "}";
        }
    }

    protected String formatAnnotationNamedParam(List<String> value) {
        if (opts.useKotlinSyntax) {
            return "[ " + String.join(", ", value) + " ]";
        } else {
            return value.size() == 1 ? value.get(0) : "{ " + String.join(", ", value) + " }";
        }
    }
}
