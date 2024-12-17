package io.github.torand.openapi2java.test.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    UUID id,

    @Schema(description = "TBD", required = true)
    @Valid
    @NotNull
    UserProfileV1Dto placedBy,

    @Schema(description = "TBD", required = true)
    @NotNull
    OrderStatusV1Dto status,

    @Schema(description = "Date and time of ticket creation", required = true, format = "date-time")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdTime,

    @Schema(description = "Order items", required = true)
    @Valid
    @NotNull
    @Size(min = 1)
    List<@NotNull OrderItemV1Dto> items,

    @Schema(description = "Additional comment from customer")
    String comment
) {

}
