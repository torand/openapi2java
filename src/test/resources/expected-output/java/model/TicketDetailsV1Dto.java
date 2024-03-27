package org.github.torand.test.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "TicketDetailsV1Dto", description="Details for a ticket or issue registered by a user")
public record TicketDetailsV1Dto (

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
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdTime,

    @Schema(description="Date and time of ticket resolution", required = true, format = "date-time")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime resolvedTime,

    @Schema(description="Form field values for the ticket", required = true)
    @Valid
    @NotEmpty
    List<@NotNull TicketAttributeV1Dto> attributes,

    @Schema(description="Comments on a ticket made by reporter, case officer or other parties", required = true)
    @Valid
    @NotEmpty
    List<@NotNull TicketCommentV1Dto> comments,

    @Schema(description="Media files or documents attached to ticket by reporter, case officer or other parties", required = true)
    @Valid
    @NotEmpty
    List<@NotNull TicketAttachmentV1Dto> attachments
) {

}
