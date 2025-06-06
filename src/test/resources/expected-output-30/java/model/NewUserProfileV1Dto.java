package io.github.torand.openapi2java.test.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.torand.openapi2java.test.annotation.MobileNo;
import io.github.torand.openapi2java.test.model.common.AddressV1Dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "NewUserProfileV1", description = "A user profile to be created")
public record NewUserProfileV1Dto (

    @Schema(description = "First name of user", required = true)
    @JsonProperty("firstName")
    @NotBlank
    String firstName,

    @Schema(description = "Last name of user", required = true)
    @JsonProperty("lastName")
    @NotBlank
    String lastName,

    @Schema(description = "TBD", required = true)
    @JsonProperty("address")
    @Valid
    @NotNull
    AddressV1Dto address,

    @Schema(description = "Email address of user", format = "email")
    @JsonProperty("emailAddress")
    @Email
    String emailAddress,

    @Schema(description = "TBD", required = true, pattern = "^[0-9]{10,15}$")
    @JsonProperty("mobileNumber")
    @NotBlank
    @Pattern(regexp = "^[0-9]{10,15}$")
    @MobileNo
    String mobileNumber,

    @Schema(description = "Indicates whether mobile number is successfully authenticated using a verification code", required = true)
    @JsonProperty("mobileNumberVerified")
    @NotNull
    Boolean mobileNumberVerified,

    @Schema(description = "TBD", required = true)
    @JsonProperty("type")
    @NotNull
    UserTypeV1Dto type,

    @Schema(description = "TBD", required = true, format = "date-time")
    @JsonProperty("createdTime")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdTime,

    @Schema(description = "Date and time of last user login", format = "date-time")
    @JsonProperty("lastLoginTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime lastLoginTime,

    @Schema(description = "TBD", required = true, pattern = "^[0-9]{11}$")
    @JsonProperty("nationalIdentityNumber")
    @NotBlank
    @Pattern(regexp = "^[0-9]{11}$")
    String nationalIdentityNumber
) {

}
