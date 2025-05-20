package io.github.torand.openapi2java.test.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(name = "OrderV1", description = "An order registered by a user")
public record OrderV1Dto (

    @Schema(description = "Unique order identifier", format = "uuid")
    @JsonProperty("id")
    UUID id,

    @Schema(description = "TBD", required = true)
    @JsonProperty("placedBy")
    @Valid
    @NotNull
    UserProfileV1Dto placedBy,

    @Schema(description = "TBD", required = true)
    @JsonProperty("status")
    @NotNull
    OrderStatusV1Dto status,

    @Schema(description = "TBD", required = true, format = "date-time")
    @JsonProperty("createdTime")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdTime,

    @Schema(description = "Order items", required = true)
    @JsonProperty("items")
    @Valid
    @NotNull
    @Size(min = 1)
    List<@NotNull OrderItemV1Dto> items,

    @Schema(description = "Additional comment from customer")
    @JsonProperty("comment")
    String comment
) {

}
