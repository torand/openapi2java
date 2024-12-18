package io.github.torand.openapi2java.test.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "AddressV1", description = "TBD")
public record AddressV1Dto (

    @Schema(description = "Street name and number", required = true)
    @JsonProperty("streetAddress")
    @NotBlank
    String streetAddress,

    @Schema(description = "Zip code of postal area", required = true)
    @JsonProperty("zipCode")
    @NotBlank
    String zipCode,

    @Schema(description = "City name", required = true)
    @JsonProperty("city")
    @NotBlank
    String city,

    @Schema(description = "ISO 3166-alpha-3 three-letter country code. Examples: NOR, SWE, DEU", required = true, pattern = "^[A-Z]{3}$")
    @JsonProperty("countryCode")
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$")
    String countryCode
) {

}
