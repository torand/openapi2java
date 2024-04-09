package org.github.torand.test

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.HttpHeaders.ACCEPT_LANGUAGE
import jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import jakarta.ws.rs.core.Response
import java.net.URI
import java.util.UUID
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.HEADER
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.PATH
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY
import org.eclipse.microprofile.openapi.annotations.headers.Header
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.github.torand.test.model.ErrorDto
import org.github.torand.test.model.TicketAttachmentV1Dto
import org.github.torand.test.model.TicketCommentV1Dto
import org.github.torand.test.model.TicketDetailsV1Dto
import org.github.torand.test.model.TicketV1Dto

const val ROOT_PATH: String = "api"

@SecurityRequirement(name = "basic")
@Tag(name = "Tickets", description = "Retrieving and modifying tickets")
@RegisterRestClient(configKey = "tickets")
@ClientHeaderParam(name = AUTHORIZATION, value = [ "{basicAuth}" ])
@Path(ROOT_PATH)
interface TicketsApi {

    @GET
    @Path("tickets")
    @Produces(APPLICATION_JSON, "application/vnd.test.api.ticket-v1+json")
    @Operation(operationId = "getTickets", summary = "Get list of tickets")
    @Parameter(`in` = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = Schema(implementation = String::class))
    @Parameter(`in` = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = Schema(implementation = String::class))
    @APIResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(type = ARRAY, implementation = TicketV1Dto::class)), Content(mediaType = "application/vnd.test.api.ticket-v1+json", schema = Schema(implementation = TicketV1Dto::class)) ])
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    fun getTickets(
        @HeaderParam(ACCEPT_LANGUAGE) acceptLanguage: String?,
        @HeaderParam("X-User-ID") @NotBlank xUserID: String
    ): Response

    @POST
    @Path("tickets")
    @Consumes(APPLICATION_JSON, "application/vnd.test.api.ticket-v1+json")
    @Produces(APPLICATION_JSON, "application/vnd.test.api.ticket-v1+json")
    @Operation(operationId = "registerTicket", summary = "Register new ticket")
    @Parameter(`in` = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = Schema(implementation = String::class))
    @APIResponse(responseCode = "201", description = "Ticket was registered", headers = [ Header(name = "Location", description = "URI of registered ticket", schema = Schema(implementation = URI::class, format = "uri")) ], content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = TicketV1Dto::class)), Content(mediaType = "application/vnd.test.api.ticket-v1+json", schema = Schema(implementation = TicketV1Dto::class)) ])
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    fun registerTicket(
        @HeaderParam("X-User-ID") @NotBlank xUserID: String,
        @Valid @NotNull ticketV1: TicketV1Dto
    ): Response

    @GET
    @Path("tickets/{ticketId}")
    @Produces(APPLICATION_JSON, "application/vnd.test.api.ticket-v1+json")
    @Operation(operationId = "getTicketInfo", summary = "Get information about a ticket")
    @Parameter(`in` = PATH, name = "ticketId", description = "Unique ticket identifier (ticketing system specific)", required = true, schema = Schema(implementation = String::class))
    @Parameter(`in` = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = Schema(implementation = String::class))
    @Parameter(`in` = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = Schema(implementation = String::class))
    @APIResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = TicketDetailsV1Dto::class)), Content(mediaType = "application/vnd.test.api.ticket-v1+json", schema = Schema(implementation = TicketDetailsV1Dto::class)) ])
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    fun getTicketInfo(
        @PathParam("ticketId") @NotBlank ticketId: String,
        @HeaderParam(ACCEPT_LANGUAGE) acceptLanguage: String?,
        @HeaderParam("X-User-ID") @NotBlank xUserID: String
    ): Response

    @POST
    @Path("tickets/{ticketId}/comments")
    @Consumes(APPLICATION_JSON, "application/vnd.test.api.ticket-v1+json")
    @Produces(APPLICATION_JSON)
    @Operation(operationId = "registerTicketComment", summary = "Register new comment on a ticket")
    @Parameter(`in` = PATH, name = "ticketId", description = "Unique ticket identifier (ticketing system specific)", required = true, schema = Schema(implementation = String::class))
    @Parameter(`in` = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = Schema(implementation = String::class))
    @Parameter(`in` = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = Schema(implementation = String::class))
    @APIResponse(responseCode = "201", description = "Ticket comment was registered")
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    fun registerTicketComment(
        @PathParam("ticketId") @NotBlank ticketId: String,
        @HeaderParam(ACCEPT_LANGUAGE) acceptLanguage: String?,
        @HeaderParam("X-User-ID") @NotBlank xUserID: String,
        @Valid @NotNull ticketCommentV1: TicketCommentV1Dto
    ): Response

    @POST
    @Path("tickets/{ticketId}/attachments")
    @Consumes(APPLICATION_JSON, "application/vnd.test.api.ticket-v1+json")
    @Produces(APPLICATION_JSON, "application/vnd.test.api.ticket-v1+json")
    @Operation(operationId = "registerTicketAttachment", summary = "Register new attachment on a ticket")
    @Parameter(`in` = PATH, name = "ticketId", description = "Unique ticket identifier (ticketing system specific)", required = true, schema = Schema(implementation = String::class))
    @Parameter(`in` = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = Schema(implementation = String::class))
    @Parameter(`in` = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = Schema(implementation = String::class))
    @APIResponse(responseCode = "201", description = "Ticket attachment was registered", headers = [ Header(name = "Location", description = "URI of registered attachment", schema = Schema(implementation = URI::class, format = "uri")), Header(name = "X-Test-Header", description = "A test header", schema = Schema(implementation = UUID::class, format = "uuid")) ], content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = TicketAttachmentV1Dto::class)), Content(mediaType = "application/vnd.test.api.ticket-v1+json", schema = Schema(implementation = TicketAttachmentV1Dto::class)) ])
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    fun registerTicketAttachment(
        @PathParam("ticketId") @NotBlank ticketId: String,
        @HeaderParam(ACCEPT_LANGUAGE) acceptLanguage: String?,
        @HeaderParam("X-User-ID") @NotBlank xUserID: String,
        @Valid @NotNull ticketAttachmentV1: TicketAttachmentV1Dto
    ): Response

    @POST
    @Path("tickets/{ticketId}/attachments/{attachmentId}/content")
    @Consumes("application/octet-stream")
    @Produces(APPLICATION_JSON)
    @Operation(operationId = "uploadTicketAttachmentContent", summary = "Upload content of an attachment")
    @Parameter(`in` = PATH, name = "ticketId", description = "Unique ticket identifier (ticketing system specific)", required = true, schema = Schema(implementation = String::class))
    @Parameter(`in` = PATH, name = "attachmentId", description = "Unique attachment identifier (ticketing system specific)", required = true, schema = Schema(implementation = String::class))
    @Parameter(`in` = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = Schema(implementation = String::class))
    @Parameter(`in` = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = Schema(implementation = String::class))
    @APIResponse(responseCode = "201", description = "Ticket attachment content was registered")
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    fun uploadTicketAttachmentContent(
        @PathParam("ticketId") @NotBlank ticketId: String,
        @PathParam("attachmentId") @NotBlank attachmentId: String,
        @HeaderParam(ACCEPT_LANGUAGE) acceptLanguage: String?,
        @HeaderParam("X-User-ID") @NotBlank xUserID: String
    ): Response

    fun basicAuth() {
        return "TODO"
    }: String
}
