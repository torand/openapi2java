package io.github.torand.openapi2java.test.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.torand.openapi2java.test.model.common.EmptyObjectDto;
import io.github.torand.openapi2java.test.serialization.ProductNoDeserializer;
import io.github.torand.openapi2java.test.serialization.ProductNoSerializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "ProductV1", description = "A product available in the web shop")
public record ProductV1Dto (

    @Schema(description = "Product number", required = true)
    @JsonProperty("number")
    @NotBlank
    @JsonSerialize(using = ProductNoSerializer.class)
    @JsonDeserialize(using = ProductNoDeserializer.class)
    String number,

    @Schema(description = "Product name", required = true)
    @JsonProperty("name")
    @NotBlank
    @Size(min = 3)
    String name,

    @Schema(description = "Product category", required = true)
    @JsonProperty("category")
    @NotNull
    ProductCategoryV1Dto category,

    @Schema(description = "Product price (NOK)", required = true, format = "float")
    @JsonProperty("price")
    @NotNull
    Float price,

    /// @deprecated To be removed in next version
    @Deprecated
    @Schema(description = "Product comment", deprecated = true)
    @JsonProperty("comment")
    String comment,

    @Schema(description = "TBD")
    @JsonProperty("empty")
    @Valid
    EmptyObjectDto empty
) {

}
