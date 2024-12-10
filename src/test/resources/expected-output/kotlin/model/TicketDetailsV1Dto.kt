package no.tensio.coreit.test.model

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "TicketDetailsV1", description = "Details for a ticket or issue registered by a user")
@JvmRecord
data class TicketDetailsV1Dto (

    @field:Schema(description = "Unique ticket identifier", format = "uuid")
    val id: UUID? = null,

    @field:Schema(description = "TBD", required = true)
    @field:NotNull
    val type: TicketTypeV1Dto,

    @field:Schema(description = "TBD", required = true)
    @field:NotNull
    val status: TicketStatusV1Dto,

    @field:Schema(description = "Date and time of ticket creation", required = true, format = "date-time")
    @field:NotNull
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdTime: LocalDateTime,

    @field:Schema(description = "Date and time of ticket resolution", required = true, format = "date-time")
    @field:NotNull
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val resolvedTime: LocalDateTime,

    @field:Schema(description = "Form field values for the ticket", required = true)
    @field:Valid
    @field:NotNull
    val attributes: List<@NotNull TicketAttributeV1Dto>,

    @field:Schema(description = "Comments on a ticket made by reporter, case officer or other parties", required = true)
    @field:Valid
    @field:NotNull
    val comments: List<@NotNull TicketCommentV1Dto>,

    @field:Schema(description = "Media files or documents attached to ticket by reporter, case officer or other parties", required = true)
    @field:Valid
    @field:NotNull
    val attachments: List<@NotNull TicketAttachmentV1Dto>,

    @field:Schema(description = "Generic key/value metadata", required = true)
    @field:Valid
    @field:NotNull
    val metadata: Map<@NotBlank String, @NotBlank String>,

    @field:Schema(description = "Generic number, may be float or decimal?", required = true)
    @field:NotNull
    val testNumber: BigDecimal
)
