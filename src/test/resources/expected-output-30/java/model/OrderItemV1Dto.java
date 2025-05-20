package io.github.torand.openapi2java.test.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "OrderItemV1", description = "An order item")
public record OrderItemV1Dto (

    @Schema(description = "TBD", required = true)
    @JsonProperty("product")
    @Valid
    @NotNull
    ProductV1Dto product,

    @Schema(description = "The quantity ordered", required = true)
    @JsonProperty("quantity")
    @NotNull
    @Min(1)
    Integer quantity
) {

}
