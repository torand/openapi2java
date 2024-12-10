package no.tensio.coreit.test.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "TicketAttachmentV1", description = "A media file or document attached to a ticket by reporter, case officer or other parties")
public record TicketAttachmentV1Dto (

    @Schema(description = "Unique ticket attachment identifier", format = "uuid")
    UUID id,

    @Schema(description = "Filename of attachment", required = true)
    @NotBlank
    String filename,

    @Schema(description = "Date and time of ticket attachment upload", required = true, format = "date-time")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime uploadedTime,

    @Schema(description = "Description of attachment", required = true)
    @NotBlank
    String description,

    @Schema(description = "IANA media type descriptor for attachment content", required = true)
    @NotBlank
    String contentType,

    @Schema(description = "Size of attachment in number of bytes", required = true)
    @NotNull
    Integer contentLength
) {

}
