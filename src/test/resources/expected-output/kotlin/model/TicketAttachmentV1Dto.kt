package io.github.torand.test.model

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "TicketAttachmentV1Dto", description="A media file or document attached to a ticket by reporter, case officer or other parties")
@JvmRecord
data class TicketAttachmentV1Dto (

    @field:Schema(description="Unique ticket attachment identifier", required = true, format = "uuid")
    @field:NotNull
    val id: UUID,

    @field:Schema(description="Filename of attachment", required = true)
    @field:NotBlank
    val filename: String,

    @field:Schema(description="Date and time of ticket attachment upload", required = true, format = "date-time")
    @field:NotNull
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val uploadedTime: LocalDateTime,

    @field:Schema(description="Description of attachment", required = true)
    @field:NotBlank
    val description: String,

    @field:Schema(description="IANA media type descriptor for attachment content", required = true)
    @field:NotBlank
    val contentType: String,

    @field:Schema(description="Size of attachment in number of bytes", required = true)
    @field:NotNull
    val contentLength: Integer
)
