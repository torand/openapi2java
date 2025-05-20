package io.github.torand.openapi2java.test.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.UUID
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "OrderV1", description = "An order registered by a user")
@JvmRecord
data class OrderV1Dto (

    @field:Schema(description = "Unique order identifier", format = "uuid")
    @JsonProperty("id")
    val id: UUID? = null,

    @field:Schema(description = "TBD", required = true)
    @JsonProperty("placedBy")
    @field:Valid
    @field:NotNull
    val placedBy: UserProfileV1Dto,

    @field:Schema(description = "TBD", required = true)
    @JsonProperty("status")
    @field:NotNull
    val status: OrderStatusV1Dto,

    @field:Schema(description = "TBD", required = true, format = "date-time")
    @JsonProperty("createdTime")
    @field:NotNull
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdTime: LocalDateTime,

    @field:Schema(description = "Order items", required = true)
    @JsonProperty("items")
    @field:Valid
    @field:NotNull
    @field:Size(min = 1)
    val items: List<@NotNull OrderItemV1Dto>,

    @field:Schema(description = "Additional comment from customer")
    @JsonProperty("comment")
    val comment: String? = null
)
