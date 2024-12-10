package no.tensio.coreit.test

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
import java.net.URI
import no.tensio.coreit.test.UsersApi_Resteasy.Companion.ROOT_PATH
import no.tensio.coreit.test.model.NewUserProfileV1Dto
import no.tensio.coreit.test.model.UserProfileV1Dto
import no.tensio.coreit.test.model.common.ErrorDto
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.HEADER
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.PATH
import org.eclipse.microprofile.openapi.annotations.headers.Header
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.jboss.resteasy.reactive.RestResponse

@SecurityRequirement(name = "oidc")
@Tag(name = "Users", description = "Retrieving and modifying user profiles")
@RegisterRestClient(configKey = "users-api")
@ClientHeaderParam(name = AUTHORIZATION, value = [ "{authorization}" ])
@Path(ROOT_PATH)
interface UsersApi_Resteasy {

    @POST
    @Path("users")
    @Consumes(APPLICATION_JSON, "application/vnd.test.api.user-profile-v1+json")
    @Produces(APPLICATION_JSON, "application/vnd.test.api.user-profile-v1+json")
    @Operation(operationId = "registerUserProfile", summary = "Register new user profile")
    @APIResponse(responseCode = "201", description = "User profile was registered", headers = [ Header(name = "Location", description = "URI of registered user profile", schema = Schema(implementation = URI::class, format = "uri")) ], content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = UserProfileV1Dto::class)), Content(mediaType = "application/vnd.test.api.user-profile-v1+json", schema = Schema(implementation = UserProfileV1Dto::class)) ])
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    fun registerUserProfile(
        @Valid @NotNull newUserProfileV1: NewUserProfileV1Dto
    ): RestResponse<UserProfileV1Dto>

    @GET
    @Path("users/{userId}")
    @Produces(APPLICATION_JSON, "application/vnd.test.api.user-profile-v1+json")
    @Operation(operationId = "getUserProfile", summary = "Get a user profile")
    @Parameter(`in` = PATH, name = "userId", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = Schema(implementation = String::class))
    @Parameter(`in` = HEADER, name = ACCEPT_LANGUAGE, description = "Natural language and locale accepted by client", schema = Schema(implementation = String::class, defaultValue = "nb-NO"))
    @APIResponse(responseCode = "200", description = "OK", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = UserProfileV1Dto::class)), Content(mediaType = "application/vnd.test.api.user-profile-v1+json", schema = Schema(implementation = UserProfileV1Dto::class)) ])
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    fun getUserProfile(
        @PathParam("userId") @NotBlank userId: String,
        @HeaderParam(ACCEPT_LANGUAGE) acceptLanguage: String? = null
    ): RestResponse<UserProfileV1Dto>

    @POST
    @Path("users/{userId}/verify")
    @Consumes(APPLICATION_JSON, "application/vnd.test.api.user-profile-v1+json")
    @Produces(APPLICATION_JSON, "application/vnd.test.api.user-profile-v1+json")
    @Operation(operationId = "verifyMobileNumber", summary = "Verify user profile mobile number with verification code")
    @Parameter(`in` = PATH, name = "userId", description = "Unique user identifier (SHA1 fingerprint)", required = true, schema = Schema(implementation = String::class))
    @APIResponse(responseCode = "200", description = "Verification completed. Outcome in payload", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = UserProfileV1Dto::class)), Content(mediaType = "application/vnd.test.api.user-profile-v1+json", schema = Schema(implementation = UserProfileV1Dto::class)) ])
    @APIResponse(responseCode = "400", description = "Invalid input parameters supplied", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "401", description = "Authentication credentials are invalid or missing", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "403", description = "Authenticated client or user is not granted access to this resource", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "404", description = "The requested resource was not found", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    @APIResponse(responseCode = "500", description = "Internal server error while processing request", content = [ Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = ErrorDto::class)) ])
    fun verifyMobileNumber(
        @PathParam("userId") @NotBlank userId: String,
        @NotBlank string: String // Verification code entered by user
    ): RestResponse<UserProfileV1Dto>

    companion object {
        const val ROOT_PATH: String = "api"
        fun authorization() = "TODO"
    }
}
