package io.github.torand.openapi2java.test.model.common

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "AddressV1", description = "TBD")
@JvmRecord
data class AddressV1Dto (

    @field:Schema(description = "Street name and number", required = true)
    @JsonProperty("streetAddress")
    @field:NotBlank
    val streetAddress: String,

    @field:Schema(description = "Zip code of postal area", required = true)
    @JsonProperty("zipCode")
    @field:NotBlank
    val zipCode: String,

    @field:Schema(description = "City name", required = true)
    @JsonProperty("city")
    @field:NotBlank
    val city: String,

    @field:Schema(description = "ISO 3166-alpha-3 three-letter country code. Examples: NOR, SWE, DEU", required = true, pattern = "^[A-Z]{3}$")
    @JsonProperty("countryCode")
    @field:NotBlank
    @field:Pattern(regexp = "^[A-Z]{3}$")
    val countryCode: String
)
