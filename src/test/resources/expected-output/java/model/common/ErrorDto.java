package io.github.torand.openapi2java.test.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "Error", description = "Error message response for failed requests")
public record ErrorDto (

    @Schema(description = "The HTTP response status code", required = true)
    @JsonProperty("code")
    @NotNull
    Integer code,

    @Schema(description = "The error message", required = true)
    @JsonProperty("message")
    @NotBlank
    String message
) {

}
