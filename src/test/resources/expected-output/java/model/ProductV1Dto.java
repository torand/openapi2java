package io.github.torand.openapi2java.test.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.torand.test.serialization.ProductNoSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "ProductV1", description = "A product available in the web shop")
public record ProductV1Dto (

    @Schema(description = "Product number", required = true)
    @NotBlank
    @JsonSerialize(using = ProductNoSerializer.class)
    String number,

    @Schema(description = "Product name", required = true)
    @NotBlank
    @Size(min = 3)
    String name,

    @Schema(description = "Product category", required = true)
    @NotNull
    ProductCategoryV1Dto category,

    @Schema(description = "Product price (NOK)", required = true, format = "float")
    @NotNull
    Float price,

    /// @deprecated To be removed in next version
    @Deprecated
    @Schema(description = "Product comment", deprecated = true)
    String comment
) {

}
