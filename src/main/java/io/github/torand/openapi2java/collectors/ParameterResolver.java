package io.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.Map;
import java.util.Optional;

import static io.github.torand.openapi2java.utils.Exceptions.illegalStateException;

public class ParameterResolver {
    private final Map<String, Parameter> parameters;

    ParameterResolver(Map<String, Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getParameterName(String $ref) {
        return $ref.replace("#/components/parameters/", "");
    }

    public Optional<Parameter> get(String $ref) {
        return Optional.ofNullable(parameters.get(getParameterName($ref)));
    }

    public Parameter getOrThrow(String $ref) {
        return get($ref).orElseThrow(illegalStateException("Parameter %s not found", $ref));
    }

    public boolean isHeaderParameter(String $ref) {
        return get($ref).map(p -> "header".equals(p.getIn())).orElse(false);
    }

    public boolean isPathParameter(String $ref) {
        return get($ref).map(p -> "path".equals(p.getIn())).orElse(false);
    }

    public boolean isQueryParameter(String $ref) {
        return get($ref).map(p -> "query".equals(p.getIn())).orElse(false);
    }
}
