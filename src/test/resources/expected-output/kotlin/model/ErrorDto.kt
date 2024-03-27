package org.github.torand.test.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "ErrorDto", description="Error message response for failed requests")
@JvmRecord
data class ErrorDto (

    @field:Schema(description="The HTTP response status code", required = true)
    @field:NotNull
    val code: Integer,

    @field:Schema(description="The error message", required = true)
    @field:NotBlank
    val message: String
)
