package io.github.torand.openapi2java.test

import jakarta.ws.rs.core.Application
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes

@SecuritySchemes(SecurityScheme(securitySchemeName = "oidc", type = SecuritySchemeType.OPENIDCONNECT, openIdConnectUrl = "https://iam.test.no/v2.0/.well-known/openid-configuration"))
class OpenApiDefinition : Application()
