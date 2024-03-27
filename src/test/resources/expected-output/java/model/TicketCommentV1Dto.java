package org.github.torand.test.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "TicketCommentV1Dto", description="A comment on a ticket made by reporter, case officer or other parties")
public record TicketCommentV1Dto (

    @Schema(description="Unique ticket comment identifier", required = true, format = "uuid")
    @NotNull
    UUID id,

    @Schema(description="Author of this comment", required = true)
    @NotBlank
    String author,

    @Schema(description="Date and time of ticket comment creation", required = true, format = "date-time")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdTime,

    @Schema(description="Subject of this comment", required = true)
    @NotBlank
    String subject,

    @Schema(description="Main content of this comment", required = true)
    @NotBlank
    String description,

    @Schema(description="Indicates whether comment is visible for case officer only", required = true)
    @NotNull
    Boolean internal
) {

}
