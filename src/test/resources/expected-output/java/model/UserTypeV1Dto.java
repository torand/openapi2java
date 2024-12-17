package io.github.torand.openapi2java.test.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "UserTypeV1", description = "User types")
public enum UserTypeV1Dto {
    Private, Business
}
