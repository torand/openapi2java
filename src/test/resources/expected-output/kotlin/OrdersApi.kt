package io.github.torand.openapi2java.test

import io.github.torand.openapi2java.test.OrdersApi.Companion.ROOT_PATH
import io.github.torand.openapi2java.test.model.OrderItemV1Dto
import io.github.torand.openapi2java.test.model.OrderV1Dto
import io.github.torand.openapi2java.test.model.common.ErrorDto
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

@SecurityRequirement(name = "oidc")
@Tag(name = "Orders", description = "Retrieving and modifying orders")
@RegisterRestClient(configKey = "order-api")
@ClientHeaderParam(name = AUTHORIZATION, value = [ "{authorization}" ])
@Path(ROOT_PATH)
interface OrdersApi {

    @GET
    @Path("orders")
    @Produces(APPLICATION_JSON, "application/vnd.test.api.order-v1+json")
    @Operation(operationId = "getOrders", summary = "Get list of orders")
    @Parameter(`in` = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = Schema(implementation = String::class, defaultValue = "nb-NO"))
    @Parameter(`in` = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = Schema(implementation = String::class))
    @APIResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(type = ARRAY, implementation = OrderV1Dto::class)), Content(mediaType = "application/vnd.test.api.order-v1+json", schema = Schema(implementation = OrderV1Dto::class)) ])
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    fun getOrders(
        @HeaderParam(ACCEPT_LANGUAGE) acceptLanguage: String? = null,
        @HeaderParam("X-User-ID") @NotBlank xUserID: String
    ): Response

    @POST
    @Path("orders")
    @Consumes("application/vnd.test.api.order-v1+json")
    @Produces(APPLICATION_JSON, "application/vnd.test.api.order-v1+json")
    @Operation(operationId = "createOrder", summary = "Create new (empty) order")
    @Parameter(`in` = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = Schema(implementation = String::class))
    @APIResponse(responseCode = "201", description = "Order was created", headers = [ Header(name = "Location", description = "URI of created resource", schema = Schema(implementation = URI::class, format = "uri")) ], content = [ Content(mediaType = "application/vnd.test.api.order-v1+json", schema = Schema(implementation = OrderV1Dto::class)) ])
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    fun createOrder(
        @HeaderParam("X-User-ID") @NotBlank xUserID: String,
        @Valid @NotNull orderV1: OrderV1Dto
    ): Response

    @GET
    @Path("orders/{orderId}")
    @Produces(APPLICATION_JSON, "application/vnd.test.api.order-v1+json")
    @Operation(operationId = "getOrderInfo", summary = "Get information about an order")
    @Parameter(`in` = PATH, name = "orderId", description = "Unique order identifier", required = true, schema = Schema(implementation = String::class))
    @Parameter(`in` = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = Schema(implementation = String::class, defaultValue = "nb-NO"))
    @Parameter(`in` = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = Schema(implementation = String::class))
    @APIResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = OrderV1Dto::class)), Content(mediaType = "application/vnd.test.api.order-v1+json", schema = Schema(implementation = OrderV1Dto::class)) ])
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    fun getOrderInfo(
        @PathParam("orderId") @NotBlank orderId: String,
        @HeaderParam(ACCEPT_LANGUAGE) acceptLanguage: String? = null,
        @HeaderParam("X-User-ID") @NotBlank xUserID: String
    ): Response

    @POST
    @Path("orders/{orderId}/items")
    @Consumes("application/vnd.test.api.order-v1+json")
    @Produces(APPLICATION_JSON)
    @Operation(operationId = "registerOrderItem", summary = "Register new item in an order")
    @Parameter(`in` = PATH, name = "orderId", description = "Unique order identifier", required = true, schema = Schema(implementation = String::class))
    @Parameter(`in` = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = Schema(implementation = String::class, defaultValue = "nb-NO"))
    @Parameter(`in` = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = Schema(implementation = String::class))
    @APIResponse(responseCode = "201", description = "Order item was registered")
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    fun registerOrderItem(
        @PathParam("orderId") @NotBlank orderId: String,
        @HeaderParam(ACCEPT_LANGUAGE) acceptLanguage: String? = null,
        @HeaderParam("X-User-ID") @NotBlank xUserID: String,
        @Valid @NotNull orderItemV1: OrderItemV1Dto
    ): Response

    companion object {
        const val ROOT_PATH: String = "api"
        fun authorization() = "TODO"
    }
}
