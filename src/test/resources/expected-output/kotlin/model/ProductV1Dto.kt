package io.github.torand.openapi2java.test.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.github.torand.openapi2java.test.model.common.EmptyObjectDto
import io.github.torand.openapi2java.test.serialization.ProductNoSerializer
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "ProductV1", description = "A product available in the web shop")
@JvmRecord
data class ProductV1Dto (

    @field:Schema(description = "Product number", required = true)
    @JsonProperty("number")
    @field:NotBlank
    @field:JsonSerialize(using = ProductNoSerializer::class)
    val number: String,

    @field:Schema(description = "Product name", required = true)
    @JsonProperty("name")
    @field:NotBlank
    @field:Size(min = 3)
    val name: String,

    @field:Schema(description = "Product category", required = true)
    @JsonProperty("category")
    @field:NotNull
    val category: ProductCategoryV1Dto,

    @field:Schema(description = "Product price (NOK)", required = true, format = "float")
    @JsonProperty("price")
    @field:NotNull
    val price: Float,

    @Deprecated("To be removed in next version")
    @field:Schema(description = "Product comment", deprecated = true)
    @JsonProperty("comment")
    val comment: String? = null,

    @field:Schema(description = "TBD")
    @JsonProperty("empty")
    @field:Valid
    val empty: EmptyObjectDto? = null
)
