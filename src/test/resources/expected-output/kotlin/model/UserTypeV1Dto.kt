package io.github.torand.openapi2java.test.model

import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "UserTypeV1", description = "User types")
enum class UserTypeV1Dto {
    Private, Business
}
