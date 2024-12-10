package no.tensio.coreit.test.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(name = "TicketDetailsV1", description = "Details for a ticket or issue registered by a user")
public record TicketDetailsV1Dto (

    @Schema(description = "Unique ticket identifier", format = "uuid")
    UUID id,

    @Schema(description = "TBD", required = true)
    @NotNull
    TicketTypeV1Dto type,

    @Schema(description = "TBD", required = true)
    @NotNull
    TicketStatusV1Dto status,

    @Schema(description = "Date and time of ticket creation", required = true, format = "date-time")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdTime,

    @Schema(description = "Date and time of ticket resolution", required = true, format = "date-time")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime resolvedTime,

    @Schema(description = "Form field values for the ticket", required = true)
    @Valid
    @NotNull
    List<@NotNull TicketAttributeV1Dto> attributes,

    @Schema(description = "Comments on a ticket made by reporter, case officer or other parties", required = true)
    @Valid
    @NotNull
    List<@NotNull TicketCommentV1Dto> comments,

    @Schema(description = "Media files or documents attached to ticket by reporter, case officer or other parties", required = true)
    @Valid
    @NotNull
    List<@NotNull TicketAttachmentV1Dto> attachments,

    @Schema(description = "Generic key/value metadata", required = true)
    @Valid
    @NotNull
    Map<@NotBlank String, @NotBlank String> metadata,

    @Schema(description = "Generic number, may be float or decimal?", required = true)
    @NotNull
    BigDecimal testNumber
) {

}
