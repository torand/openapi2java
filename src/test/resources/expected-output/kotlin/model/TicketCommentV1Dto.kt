package org.github.torand.test.model

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "TicketCommentV1Dto", description="A comment on a ticket made by reporter, case officer or other parties")
@JvmRecord
data class TicketCommentV1Dto (

    @field:Schema(description="Unique ticket comment identifier", required = true, format = "uuid")
    @field:NotNull
    val id: UUID,

    @field:Schema(description="Author of this comment", required = true)
    @field:NotBlank
    val author: String,

    @field:Schema(description="Date and time of ticket comment creation", required = true, format = "date-time")
    @field:NotNull
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdTime: LocalDateTime,

    @field:Schema(description="Subject of this comment", required = true)
    @field:NotBlank
    val subject: String,

    @field:Schema(description="Main content of this comment", required = true)
    @field:NotBlank
    val description: String,

    @field:Schema(description="Indicates whether comment is visible for case officer only", required = true)
    @field:NotNull
    val internal: Boolean
)
