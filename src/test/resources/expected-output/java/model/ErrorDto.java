package org.github.torand.test.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "ErrorDto", description="Error message response for failed requests")
public record ErrorDto (

    @Schema(description="The HTTP response status code", required = true)
    @NotNull
    Integer code,

    @Schema(description="The error message", required = true)
    @NotBlank
    String message
) {

}
