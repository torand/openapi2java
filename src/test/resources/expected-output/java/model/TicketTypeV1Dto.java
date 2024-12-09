package io.github.torand.test.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "TicketTypeV1Dto", description="Type of ticket")
public enum TicketTypeV1Dto {
    Change, Incident
}
