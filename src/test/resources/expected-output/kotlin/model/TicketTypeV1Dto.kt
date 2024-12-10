package no.tensio.coreit.test.model

import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "TicketTypeV1", description = "Type of ticket")
enum class TicketTypeV1Dto {
    Change, Incident
}
