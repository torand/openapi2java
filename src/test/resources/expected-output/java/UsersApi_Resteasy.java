package io.github.torand.openapi2java.test;

import io.github.torand.openapi2java.test.model.NewUserProfileV1Dto;
import io.github.torand.openapi2java.test.model.UserProfileV1Dto;
import io.github.torand.openapi2java.test.model.common.ErrorDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

import java.net.URI;

import static io.github.torand.openapi2java.test.UsersApi_Resteasy.ROOT_PATH;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT_LANGUAGE;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.HEADER;
import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.PATH;

@SecurityRequirement(name = "oidc")
@Tag(name = "Users", description = "Retrieving and modifying user profiles")
@RegisterRestClient(configKey = "users-api")
@ClientHeaderParam(name = AUTHORIZATION, value = "{authorization}")
@Path(ROOT_PATH)
public interface UsersApi_Resteasy {

    String ROOT_PATH = "api";

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
    RestResponse<UserProfileV1Dto> createUserProfile(
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
    RestResponse<UserProfileV1Dto> getUserProfile(
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
    RestResponse<UserProfileV1Dto> verifyMobileNumber(
        @PathParam("userId") @NotBlank String userId,
        @NotBlank String string // Verification code entered by user
    );

    @SuppressWarnings("unused") // Used by @ClientHeaderParam
    default String authorization() {
        return "TODO";
    }
}
