package io.github.torand.test.model

import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "TicketTypeV1Dto", description="Type of ticket")
enum class TicketTypeV1Dto {
    Change, Incident
}
