package org.github.torand.test.model

import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "TicketStatusV1Dto", description="Processing status of a ticket")
enum class TicketStatusV1Dto {
    Registered, InProgress, Completed
}
