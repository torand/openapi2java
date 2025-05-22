package io.github.torand.openapi2java.test;

import io.github.torand.openapi2java.test.model.NewUserProfileV1Dto;
import io.github.torand.openapi2java.test.model.OrderItemV1Dto;
import io.github.torand.openapi2java.test.model.OrderV1Dto;
import io.github.torand.openapi2java.test.model.ProductV1Dto;
import io.github.torand.openapi2java.test.model.UserProfileV1Dto;
import io.github.torand.openapi2java.test.model.common.ErrorDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

import java.io.File;
import java.net.URI;
import java.util.List;

import static io.github.torand.openapi2java.test.CompoundApi.ROOT_PATH;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT_LANGUAGE;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.HEADER;
import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.PATH;
import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;
import static org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY;

@SecurityRequirement(name = "oidc")
@RegisterRestClient(configKey = "compound-api")
@ClientHeaderParam(name = AUTHORIZATION, value = "{authorization}")
@Path(ROOT_PATH)
public interface CompoundApi {

    String ROOT_PATH = "api";

    @GET
    @Path("orders")
    @Produces({APPLICATION_JSON, "application/vnd.test.api.order-v1+json"})
    @Operation(operationId = "getOrders", summary = "Get list of orders")
    @Parameter(in = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = @Schema(implementation = String.class, defaultValue = "nb-NO"))
    @Parameter(in = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = @Schema(implementation = String.class))
    @APIResponse(responseCode = "200", description = "OK", content = { @Content(mediaType = APPLICATION_JSON, schema = @Schema(type = ARRAY, implementation = OrderV1Dto.class)), @Content(mediaType = "application/vnd.test.api.order-v1+json", schema = @Schema(implementation = OrderV1Dto.class)) })
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    Response getOrders(
        @HeaderParam(ACCEPT_LANGUAGE) String acceptLanguage,
        @HeaderParam("X-User-ID") @NotBlank String xUserID
    );

    @POST
    @Path("orders")
    @Consumes("application/vnd.test.api.order-v1+json")
    @Produces({APPLICATION_JSON, "application/vnd.test.api.order-v1+json"})
    @Operation(operationId = "createOrder", summary = "Create new (empty) order")
    @Parameter(in = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = @Schema(implementation = String.class))
    @APIResponse(responseCode = "201", description = "Order was created", headers = @Header(name = "Location", description = "URI of created resource", schema = @Schema(implementation = URI.class, format = "uri")), content = @Content(mediaType = "application/vnd.test.api.order-v1+json", schema = @Schema(implementation = OrderV1Dto.class)))
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    Response createOrder(
        @HeaderParam("X-User-ID") @NotBlank String xUserID,
        @Valid @NotNull OrderV1Dto orderV1
    );

    @GET
    @Path("orders/{orderId}")
    @Produces({APPLICATION_JSON, "application/vnd.test.api.order-v1+json"})
    @Operation(operationId = "getOrderInfo", summary = "Get information about an order")
    @Parameter(in = PATH, name = "orderId", description = "Unique order identifier", required = true, schema = @Schema(implementation = String.class))
    @Parameter(in = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = @Schema(implementation = String.class, defaultValue = "nb-NO"))
    @Parameter(in = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = @Schema(implementation = String.class))
    @APIResponse(responseCode = "200", description = "OK", content = { @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = OrderV1Dto.class)), @Content(mediaType = "application/vnd.test.api.order-v1+json", schema = @Schema(implementation = OrderV1Dto.class)) })
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    Response getOrderInfo(
        @PathParam("orderId") @NotBlank String orderId,
        @HeaderParam(ACCEPT_LANGUAGE) String acceptLanguage,
        @HeaderParam("X-User-ID") @NotBlank String xUserID
    );

    @POST
    @Path("orders/{orderId}/items")
    @Consumes("application/vnd.test.api.order-v1+json")
    @Produces(APPLICATION_JSON)
    @Operation(operationId = "registerOrderItem", summary = "Register new item in an order")
    @Parameter(in = PATH, name = "orderId", description = "Unique order identifier", required = true, schema = @Schema(implementation = String.class))
    @Parameter(in = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = @Schema(implementation = String.class, defaultValue = "nb-NO"))
    @Parameter(in = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = @Schema(implementation = String.class))
    @APIResponse(responseCode = "201", description = "Order item was registered")
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    Response registerOrderItem(
        @PathParam("orderId") @NotBlank String orderId,
        @HeaderParam(ACCEPT_LANGUAGE) String acceptLanguage,
        @HeaderParam("X-User-ID") @NotBlank String xUserID,
        @Valid @NotNull OrderItemV1Dto orderItemV1
    );

    @GET
    @Path("products")
    @Produces({APPLICATION_JSON, "application/vnd.test.api.product-v1+json"})
    @Operation(operationId = "getProducts", summary = "Get list of products")
    @Parameter(in = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = @Schema(implementation = String.class, defaultValue = "nb-NO"))
    @Parameter(in = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = @Schema(implementation = String.class))
    @Parameter(in = QUERY, name = "offset", description = "Offset of first result in returned page", schema = @Schema(implementation = Integer.class, defaultValue = "0"))
    @Parameter(in = QUERY, name = "limit", description = "Number of results in returned page", schema = @Schema(implementation = Integer.class, defaultValue = "10"))
    @APIResponse(responseCode = "200", description = "OK", content = { @Content(mediaType = APPLICATION_JSON, schema = @Schema(type = ARRAY, implementation = ProductV1Dto.class)), @Content(mediaType = "application/vnd.test.api.product-v1+json", schema = @Schema(implementation = ProductV1Dto.class)) })
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    Response getProducts(
        @HeaderParam(ACCEPT_LANGUAGE) String acceptLanguage,
        @HeaderParam("X-User-ID") @NotBlank String xUserID,
        @QueryParam("offset") @Min(0) Integer offset,
        @QueryParam("limit") @Min(1) Integer limit
    );

    @POST
    @Path("products")
    @Consumes("application/vnd.test.api.product-v1+json")
    @Produces({APPLICATION_JSON, "application/vnd.test.api.product-v1+json"})
    @Operation(operationId = "registerProduct", summary = "Register a new product")
    @APIResponse(responseCode = "201", description = "Product was registered", headers = @Header(name = "Location", description = "URI of created resource", schema = @Schema(implementation = URI.class, format = "uri")), content = @Content(mediaType = "application/vnd.test.api.product-v1+json", schema = @Schema(implementation = ProductV1Dto.class)))
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    Response registerProduct(
        @Valid @NotNull ProductV1Dto productV1
    );

    @POST
    @Path("products/{productNo}/image")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_JSON)
    @Operation(operationId = "uploadProductImage", summary = "Upload product image")
    @Parameter(in = PATH, name = "productNo", description = "Unique product number", required = true, schema = @Schema(implementation = String.class))
    @Parameter(in = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = @Schema(implementation = String.class, defaultValue = "nb-NO"))
    @Parameter(in = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = @Schema(implementation = String.class))
    @APIResponse(responseCode = "201", description = "Product image was registered")
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    Response uploadProductImage(
        @PathParam("productNo") @NotBlank String productNo,
        @HeaderParam(ACCEPT_LANGUAGE) String acceptLanguage,
        @HeaderParam("X-User-ID") @NotBlank String xUserID,
        @NotEmpty byte[] bytes
    );

    @GET
    @Path("products/{productNo}/datasheet")
    @Produces({APPLICATION_JSON, APPLICATION_OCTET_STREAM})
    @Operation(operationId = "downloadProductDatasheet", summary = "Download product datasheet")
    @Parameter(in = PATH, name = "productNo", description = "Unique product number", required = true, schema = @Schema(implementation = String.class))
    @Parameter(in = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = @Schema(implementation = String.class, defaultValue = "nb-NO"))
    @Parameter(in = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = @Schema(implementation = String.class))
    @APIResponse(responseCode = "200", description = "OK", headers = @Header(name = "Content-Disposition", description = "File information ([documentation](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition))", schema = @Schema(implementation = String.class)), content = @Content(mediaType = APPLICATION_OCTET_STREAM, schema = @Schema(implementation = byte[].class, format = "binary")))
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    Response downloadProductDatasheet(
        @PathParam("productNo") @NotBlank String productNo,
        @HeaderParam(ACCEPT_LANGUAGE) String acceptLanguage,
        @HeaderParam("X-User-ID") @NotBlank String xUserID
    );

    @POST
    @Path("products/{productNo}/datasheet")
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    @Operation(operationId = "uploadProductDatasheet", summary = "Upload product data sheet")
    @Parameter(in = PATH, name = "productNo", description = "Unique product number", required = true, schema = @Schema(implementation = String.class))
    @Parameter(in = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = @Schema(implementation = String.class, defaultValue = "nb-NO"))
    @Parameter(in = HEADER, name = "X-User-ID", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = @Schema(implementation = String.class))
    @APIResponse(responseCode = "201", description = "Product datasheet was registered")
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    Response uploadProductDatasheet(
        @PathParam("productNo") @NotBlank String productNo,
        @HeaderParam(ACCEPT_LANGUAGE) String acceptLanguage,
        @HeaderParam("X-User-ID") @NotBlank String xUserID,
        @RestForm("filename") @PartType(TEXT_PLAIN) @NotBlank String filename,
        @RestForm("description") @PartType(TEXT_PLAIN) @NotBlank String description,
        @RestForm("file") @PartType(APPLICATION_OCTET_STREAM) @NotNull File file
    );

    @POST
    @Path("users")
    @Consumes("application/vnd.test.api.user-profile-v1+json")
    @Produces({APPLICATION_JSON, "application/vnd.test.api.user-profile-v1+json"})
    @Operation(operationId = "createUserProfile", summary = "Create new user profile")
    @APIResponse(responseCode = "201", description = "User profile was created", headers = @Header(name = "Location", description = "URI of created resource", schema = @Schema(implementation = URI.class, format = "uri")), content = @Content(mediaType = "application/vnd.test.api.user-profile-v1+json", schema = @Schema(implementation = UserProfileV1Dto.class)))
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    Response createUserProfile(
        @Valid @NotNull NewUserProfileV1Dto newUserProfileV1
    );

    @GET
    @Path("users/{userId}")
    @Produces({APPLICATION_JSON, "application/vnd.test.api.user-profile-v1+json"})
    @Operation(operationId = "getUserProfile", summary = "Get a user profile")
    @Parameter(in = PATH, name = "userId", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = @Schema(implementation = String.class))
    @Parameter(in = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = @Schema(implementation = String.class, defaultValue = "nb-NO"))
    @APIResponse(responseCode = "200", description = "OK", content = { @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = UserProfileV1Dto.class)), @Content(mediaType = "application/vnd.test.api.user-profile-v1+json", schema = @Schema(implementation = UserProfileV1Dto.class)) })
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    Response getUserProfile(
        @PathParam("userId") @NotBlank String userId,
        @HeaderParam(ACCEPT_LANGUAGE) String acceptLanguage
    );

    @POST
    @Path("users/{userId}/verify")
    @Consumes("application/vnd.test.api.user-profile-v1+json")
    @Produces({APPLICATION_JSON, "application/vnd.test.api.user-profile-v1+json"})
    @Operation(operationId = "verifyMobileNumber", summary = "Verify user profile mobile number with verification code")
    @Parameter(in = PATH, name = "userId", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = @Schema(implementation = String.class))
    @APIResponse(responseCode = "200", description = "Verification completed. Outcome in payload", content = @Content(mediaType = "application/vnd.test.api.user-profile-v1+json", schema = @Schema(implementation = UserProfileV1Dto.class)))
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = ErrorDto.class)))
    Response verifyMobileNumber(
        @PathParam("userId") @NotBlank String userId,
        @NotBlank String string // Verification code entered by user
    );

    @SuppressWarnings("unused") // Used by @ClientHeaderParam
    default String authorization() {
        return "TODO";
    }
}
