Changelog
=========

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- ...

### Changed
- ...

### Deprecated
- ...

### Removed
- ...

### Fixed
- ...

## [1.3.0] - 2025-10-07

### Added
- New tag extension 'x-restclient-headers' to define Microprofile client header params for a resource interface.
- New tag extension 'x-restclient-headersfactory' to define Microprofile client headers factory for a resource interface.
- New tag extension 'x-restclient-providers' to define Microprofile client providers for a resource interface.
- New config parameter 'resourceClientHeadersFactoryOverride' to specify common Microprofile client header factory class for all resource interfaces.
- New config parameter 'useOidcClientAnnotation' to generate Quarkus OIDC client annotation for all resource interfaces.
- New config parameter 'resourceProvidersOverride' to specify common set of Microprofile client providers for all resource interfaces.
- New config parameter 'resourceConfigKeyOverride' to specify the same Microprofile client config-key for all resource interfaces.
- Support 'style' og 'exploded' for Parameter annotations in resource interfaces.

### Changed
- Bump dependency io.swagger.parser.v3:swagger-parser to v2.1.34

### Fixed
- Bean validation annotations on primitive subtypes of compound pojo property and resource method parameter types now generated.

## [1.2.3] - 2025-06-18

### Fixed
- Config parameter 'resourceNameOverride' now applied even for OpenAPI specifications with tags.

## [1.2.2] - 2025-05-30

### Changed
- Bump dependency io.swagger.parser.v3:swagger-parser to v2.1.29

## [1.2.0] - 2025-05-23

### Added
- New config parameter 'resourceNameOverride' to support OpenAPI specifications with no tags.
### Changed
- Config parameter 'generateResourceClasses' renamed to 'generateResourceInterfaces'.
 
## [1.1.0] - 2025-05-20

### Added
- Support for OpenAPI 3.0.x specifications.

### Changed
- Bump dependency io.swagger.parser.v3:swagger-parser to v2.1.28

## [1.0.1] - 2025-02-05

### Changed
- Bump dependency io.swagger.parser.v3:swagger-parser to v2.1.25

## [1.0.0] - 2024-12-18

### Added
- Initial version
