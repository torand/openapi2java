package org.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Map;
import java.util.Optional;

import static org.github.torand.openapi2java.utils.Exceptions.illegalStateException;

public class ResponseResolver {
    private final Map<String, ApiResponse> responses;

    ResponseResolver(Map<String, ApiResponse> responses) {
        this.responses = responses;
    }

    public String getResponseName(String $ref) {
        return $ref.replace("#/components/responses/", "");
    }

    public Optional<ApiResponse> get(String $ref) {
        return Optional.ofNullable(responses.get(getResponseName($ref)));
    }

    public ApiResponse getOrThrow(String $ref) {
        return get($ref).orElseThrow(illegalStateException("Response %s not found", $ref));
    }
}
