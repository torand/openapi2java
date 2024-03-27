package org.github.torand.test.model

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotNull
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.time.LocalDateTime
import java.util.*

@Schema(name = "TicketV1Dto", description="A ticket or issue registered by a user")
@JvmRecord
data class TicketV1Dto (

    @field:Schema(description="Unique ticket identifier", required = true, format = "uuid")
    @field:NotNull
    val id: UUID,

    @field:Schema(description="TBD", required = true)
    @field:NotNull
    val type: TicketTypeV1Dto,

    @field:Schema(description="TBD", required = true)
    @field:NotNull
    val status: TicketStatusV1Dto,

    @field:Schema(description="Date and time of ticket creation", required = true, format = "date-time")
    @field:NotNull
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdTime: LocalDateTime,

    @field:Schema(description="Date and time of ticket resolution", required = true, format = "date-time")
    @field:NotNull
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val resolvedTime: LocalDateTime
)
