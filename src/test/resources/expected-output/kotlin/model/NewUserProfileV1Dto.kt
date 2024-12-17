package io.github.torand.openapi2java.test.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.github.torand.openapi2java.test.annotation.MobileNo
import io.github.torand.openapi2java.test.model.common.AddressV1Dto
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDateTime
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "NewUserProfileV1", description = "A user profile to be created")
@JvmRecord
data class NewUserProfileV1Dto (

    @field:Schema(description = "First name of user", required = true)
    @field:NotBlank
    val firstName: String,

    @field:Schema(description = "Last name of user", required = true)
    @field:NotBlank
    val lastName: String,

    @field:Schema(description = "Address of user", required = true)
    @field:Valid
    @field:NotNull
    val address: AddressV1Dto,

    @field:Schema(description = "Email address of user", format = "email")
    @field:Email
    val emailAddress: String? = null,

    @field:Schema(description = "Mobile number of user", required = true, pattern = "^[0-9]{10,15}$")
    @field:NotBlank
    @field:Pattern(regexp = "^[0-9]{10,15}$")
    @field:MobileNo
    val mobileNumber: String,

    @field:Schema(description = "Indicates whether mobile number is successfully authenticated using a verification code", required = true)
    @field:NotNull
    val mobileNumberVerified: Boolean,

    @field:Schema(description = "Type of user", required = true)
    @field:NotNull
    val type: UserTypeV1Dto,

    @field:Schema(description = "Date and time of user profile creation", required = true, format = "date-time")
    @field:NotNull
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdTime: LocalDateTime,

    @field:Schema(description = "Date and time of last user login", format = "date-time")
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val lastLoginTime: LocalDateTime? = null,

    @field:Schema(description = "National identity number of user", required = true, pattern = "^[0-9]{11}$")
    @field:NotBlank
    @field:Pattern(regexp = "^[0-9]{11}$")
    val nationalIdentityNumber: String
)
