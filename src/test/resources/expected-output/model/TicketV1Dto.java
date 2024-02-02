package org.github.torand.test.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "TicketV1Dto", description="A ticket or issue registered by a user")
public record TicketV1Dto (

    @Schema(description="Unique ticket identifier", required = true, format = "uuid")
    @NotNull
    UUID id,

    @Schema(description="TBD", required = true)
    @NotNull
    TicketTypeV1Dto type,

    @Schema(description="TBD", required = true)
    @NotNull
    TicketStatusV1Dto status,

    @Schema(description="Date and time of ticket creation", required = true, format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull
    LocalDateTime createdTime,

    @Schema(description="Date and time of ticket resolution", required = true, format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull
    LocalDateTime resolvedTime
) {

}
