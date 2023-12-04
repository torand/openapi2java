package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Map;
import java.util.Optional;

public class ResponseResolver {
    private Map<String, ApiResponse> responses;

    public ResponseResolver(Map<String, ApiResponse> responses) {
        this.responses = responses;
    }

    public String getResponseName(String $ref) {
        return $ref.replace("#/components/responses/", "");
    }

    public Optional<ApiResponse> get(String $ref) {
        return Optional.ofNullable(responses.get(getResponseName($ref)));
    }
}
