package no.tensio.coreit.test.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.tensio.coreit.test.MobileNo;
import no.tensio.coreit.test.model.common.AddressV1Dto;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "NewUserProfileV1", description = "A user profile to be created")
public record NewUserProfileV1Dto (

    @Schema(description = "First name of user", required = true)
    @NotBlank
    String firstName,

    @Schema(description = "Last name of user", required = true)
    @NotBlank
    String lastName,

    @Schema(description = "Address of user", required = true)
    @Valid
    @NotNull
    AddressV1Dto address,

    @Schema(description = "Email address of user", format = "email")
    @Email
    String emailAddress,

    @Schema(description = "Mobile number of user", required = true, pattern = "^[0-9]{10,15}$")
    @NotBlank
    @Pattern(regexp = "^[0-9]{10,15}$")
    @MobileNo
    String mobileNumber,

    @Schema(description = "Indicates whether mobile number is successfully authenticated using a verification code", required = true)
    @NotNull
    Boolean mobileNumberVerified,

    @Schema(description = "Date and time of user profile creation", required = true, format = "date-time")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdTime,

    @Schema(description = "Date and time of last user login", format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime lastLoginTime,

    @Schema(description = "National identity number of user", required = true, pattern = "^[0-9]{11}$")
    @NotBlank
    @Pattern(regexp = "^[0-9]{11}$")
    String nationalIdentityNumber
) {

}
