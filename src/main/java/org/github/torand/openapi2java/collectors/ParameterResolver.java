package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.Map;
import java.util.Optional;

public class ParameterResolver {
    private Map<String, Parameter> parameters;

    public ParameterResolver(Map<String, Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getParameterName(String $ref) {
        return $ref.replace("#/components/parameters/", "");
    }

    public Optional<Parameter> get(String $ref) {
        return Optional.ofNullable(parameters.get(getParameterName($ref)));
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
