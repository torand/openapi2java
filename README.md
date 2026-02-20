OpenAPI2Java
============

[![CI](https://github.com/torand/openapi2java/actions/workflows/continuous-integration.yml/badge.svg)](https://github.com/torand/openapi2java/actions/workflows/continuous-integration.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.torand/openapi2java.svg?label=maven%20central)](https://central.sonatype.com/artifact/io.github.torand/openapi2java)
[![Javadoc](https://img.shields.io/badge/javadoc-online-green)](https://torand.github.io/openapi2java/apidocs/)
[![Coverage](https://coveralls.io/repos/github/torand/openapi2java/badge.svg?branch=main)](https://coveralls.io/github/torand/openapi2java?branch=main)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=torand_openapi2java&metric=alert_status)](https://sonarcloud.io/summary/overall?id=torand_openapi2java)
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-orange)](LICENSE)

A Maven plugin to generate Java models and REST clients from an [OpenAPI](https://spec.openapis.org/oas/v3.1.0) specification.

## Table of Contents

- [Overview](#overview)
- [Usage](#usage)
- [Configuration](#configuration)
- [Type Mapping](#type-mapping)
- [Constraint Mapping](#constraint-mapping)
- [Guidelines](#guidelines)
- [Limitations](#Limitations)
- [Contributing](#contributing)
- [License](#license)

## Overview

Include this Maven plugin in any Java project implementing a REST client (or server) to enable a [Contract First](https://dzone.com/articles/designing-rest-api-what-is-contract-first) build workflow.
The current version supports OpenAPI 3.0.x and 3.1.x specifications, using either YAML or JSON formats.

The OpenAPI specification file is read, parsed and validated using the excellent [Swagger Parser](https://github.com/swagger-api/swagger-parser/) library.
POJOs (classes or enums) for the representation model are output to a 'model' subdirectory using Java or Kotlin syntax.
A REST client interface is output for each tag in the OpenAPI file.
A single OpenApiDefinition file is output with annotations describing the security schemes.

The generated source code is compatible with Java 17+ and optionally includes annotations from the following libraries:

* [Microprofile Rest Client](https://download.eclipse.org/microprofile/microprofile-rest-client-2.0/microprofile-rest-client-spec-2.0.html)
* [Microprofile OpenAPI](https://download.eclipse.org/microprofile/microprofile-open-api-2.0/microprofile-openapi-spec-2.0.html)
* [Jakarta Bean Validation](https://jakarta.ee/specifications/bean-validation/)
* [Jakarta RESTful Web Services](https://jakarta.ee/specifications/restful-ws/)
* [Jackson](https://github.com/FasterXML/jackson)

## Usage

The package is available from the [Maven Central Repository](https://central.sonatype.com/artifact/io.github.torand/openapi2java).

### Include in a Maven POM File

```xml
<build>
  <plugins>
    <plugin>
      <groupId>io.github.torand</groupId>
      <artifactId>openapi2java</artifactId>
      <version>1.3.0</version>
      <executions>
        <execution>
         <id>generate</id>
         <phase>generate-sources</phase>
         <goals>
            <goal>generate</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <openApiFile>openapi.json</openApiFile>
        <outputDir>target/openapi2java</outputDir>
        <rootPackage>io.github.torand.myapi</rootPackage>
      </configuration>
    </plugin>
  </plugins>
</build>
```

### Run from the Command Line

```bash
$ mvn io.github.torand:openapi2java:1.3.0:generate \
  -DopenApiFile=openapi.json \
  -DoutputDir=target/openapi2java \
  -DrootPackage=io.github.torand.myapi
```

## Configuration

| Parameter                            | Default            | Description                                                                              |
|--------------------------------------|--------------------|------------------------------------------------------------------------------------------|
| openApiFile                          |                    | Filename of OpenAPI-file to generate Java code from                                      |
| outputDir                            | Project build dir  | Directory to write Java code files to                                                    |
| rootPackage                          |                    | Root package path of output Java classes                                                 |
| rootUrlPath                          | "api"              | Root context path of REST resources                                                      |
| resourceNameSuffix                   | "Api"              | Suffix for resource (MP Rest Client) interface names                                     |
| resourceNameOverride                 | "" (i.e. use tags) | Specify to generate a single resource interface, instead of one per tag                  |
| resourceConfigKeyOverride            | ""                 | Specify to use same MP Rest Client config key for all resource interfaces                |
| resourceClientHeadersFactoryOverride | ""                 | Specify to use same MP Rest Client header factory class for all resource interfaces      |
| resourceProvidersOverride            |                    | Specify to use same MP Rest Client providers for all resource interfaces                 |
| pojoNameSuffix                       | "Dto"              | Suffix for POJO (model) class names                                                      |
| pojosAsRecords                       | true               | Whether to output Java records instead of Java classes for models                        |
| includeTags                          | "" (i.e. all)      | Tags (comma separated) to output resource interfaces for                                 |
| generateResourceInterfaces           | true               | Whether to generate resource interfaces                                                  |
| generateOpenApiDefClass              | true               | Whether to generate OpenAPI definition class                                             |
| addJsonPropertyAnnotations           | true               | Whether to generate model files with JSON property annotations                           |
| addJakartaBeanValidationAnnotations  | true               | Whether to generate model files with Jakarta Bean Validation annotations                 |
| addMpOpenApiAnnotations              | true               | Whether to generate files with Microprofile OpenAPI annotations                          |
| addMpRestClientAnnotations           | true               | Whether to generate resource files with Microprofile Rest Client annotations             |
| useKotlinSyntax                      | false              | Whether to generate files with Kotlin syntax                                             |
| useResteasyResponse                  | false              | Whether to use RESTEasy's `RestResponse<>` as return type for generated resource methods |
| useOidcClientAnnotation              | false              | Whether to add the Quarkus OIDC client annotation to the resource interfaces             |
| indentWithTab                        | false              | Whether to output indents with the tab character                                         |
| indentSize                           | 4                  | Number of spaces in one indentation level. Relevant only when 'indentWithTab' is false.  |
| verbose                              | false              | Whether to log extra details                                                             |

## Type Mapping

Schema types and formats map to the following Java and Kotlin types in generated source code:

| Type                                         | Format            | Java type               | Kotlin type             |
|----------------------------------------------|-------------------|-------------------------|-------------------------|
| "array"                                      | N/A               | java.util.List          | java.util.List          |
| "array" with "uniqueItems" = true            | N/A               | java.util.Set           | java.util.Set           |
| "boolean"                                    | N/A               | Boolean                 | Boolean                 |
| "integer"                                    |                   | Integer                 | Int                     |
| "integer"                                    | "int32"           | Integer                 | Int                     |
| "integer"                                    | "int64"           | Long                    | Long                    |
| "number"                                     |                   | java.math.BigDecimal    | java.math.BigDecimal    |
| "number"                                     | "double"          | Double                  | Double                  |
| "number"                                     | "float"           | Float                   | Float                   |
| "object"                                     | N/A               | [^1]                    | [^1]                    |
| "object" with "additionalProperties" = {...} | N/A               | java.util.Map           | java.util.Map           |
| "string"                                     |                   | String                  | String                  |
| "string"                                     | "uri"             | java.net.URI            | java.net.URI            |
| "string"                                     | "uuid"            | java.util.UUID          | java.util.UUID          |
| "string"                                     | "duration"[^2]    | java.time.Duration      | java.time.Duration      |
| "string"                                     | "date"[^3]        | java.time.LocalDate     | java.time.LocalDate     |
| "string"                                     | "date-time"[^4]   | java.time.LocalDateTime | java.time.LocalDateTime |
| "string"                                     | "binary"          | byte[]                  | ByteArray               |
| "string"                                     | All other formats | String                  | String                  |

[^1]: Inline objects not supported.
[^2]: Expects string in the [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html) duration format.
[^3]: Expects string in the [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html) local date format.
[^4]: Expects string in the [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html) local date time format (without milliseconds).

## Constraint Mapping

Schema restriction properties map to the following Jakarta Bean Validation annotations (when enabled):

| Type      | Restriction                         | Annotation                |
|-----------|-------------------------------------|---------------------------|
| "array"   |                                     | @Valid                    |
| "array"   | Not nullable                        | @Valid @NotNull           |
| "array"   | "minItems": n                       | @Valid @Size(min = n)     |
| "array"   | "maxItems": n                       | @Valid @Size(max = n)     |
| "boolean" | Not nullable                        | @NotNull                  |
| "integer" | Not nullable                        | @NotNull                  |
| "integer" | "minimum": n                        | @Min(n)                   |
| "integer" | "maximum": n                        | @Max(n)                   |
| "number"  | Not nullable                        | @NotNull                  |
| "number"  | "minimum": n[^5]                    | @Min(n)                   |
| "number"  | "maximum": n[^5]                    | @Max(n)                   |
| "object"  |                                     | @Valid                    |
| "object"  | Not nullable                        | @Valid @NotNull           |
| "string"  | Not nullable                        | @NotBlank                 |
| "string"  | Not nullable and "format": "binary" | @NotEmpty                 |
| "string"  | "pattern": "expr"                   | @Pattern(regexp = "expr") |
| "string"  | "minLength": n                      | @Size(min = n)            |
| "string"  | "maxLength": n                      | @Size(max = n)            |
| "string"  | "format": "email"                   | @Email                    |

[^5]: When "format" is unspecified (i.e. BigDecimal).

## Guidelines

### General

As OpenAPI 3.1.x schemas are based on the [JSON Schema](https://json-schema.org/) standard (3.0.x to a lesser degree), they can be expressed in a relaxed, abstract manner.
JSON Schema, in essence, is [a constraint system, not a data definition system](https://modern-json-schema.com/json-schema-is-a-constraint-system).
This makes a powerful tool for validation, but often complicates code generation. As a general rule, to produce meaningful POJOs, strict schemas are necessary.
Hence, the "type" property is mandatory.

### References

Only local references are supported at the moment:

```json
{
  "$ref": "#/components/schemas/Address"
}
```

References outside the OpenAPI specification file are currently not resolved by the plugin.

### Customizing Code Generation

The code generation can be customized by using the following extension properties in the OpenAPI specification:

| Extension property          | Type             | Allowed where                                              | Description                                                                                                                                                                        |
|-----------------------------|------------------|------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| x-restclient-configkey      | String           | In a tag                                                   | Config key used for getting MP Rest Client config                                                                                                                                  |
| x-restclient-headers        | Object (Map)     | In a Tag                                                   | Map of custom client header names and their values                                                                                                                                 |
| x-restclient-headersfactory | String           | In a Tag                                                   | Fully qualified classname of an MP Rest Client header factory                                                                                                                      |
| x-restclient-providers      | Array of strings | In a Tag                                                   | Array of fully qualified classnames of MP Rest Client providers                                                                                                                    |
| x-json-serializer           | String           | In a schema                                                | Fully qualified classname of a Jackson serializer class for the schema                                                                                                             |
| x-json-format               | String           | In a schema of type "string", format "date" or date-time"  | Custom [date/time format pattern](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/SimpleDateFormat.html) for Jackson java.time.* deserializers/serializers. |
| x-validation-constraint     | String           | In a schema                                                | Fully qualified classname of an annotation class to validate the schema                                                                                                            |
| x-nullable                  | Boolean          | In a schema type definition                                | If `true` the type of the schema/property can be `null`                                                                                                                            |
| x-model-subdir              | String           | In a component schema                                      | Subdirectory to place the generated DTO model class                                                                                                                                |
| x-deprecation-message       | String           | Everywhere `deprecated` can be used                        | Describing why something is deprecated, and what to use instead                                                                                                                    |

### Mandatory Properties (Nullability)

Mandatory properties are (optionally) decorated with @NonNull and similar Jakarta Bean Validation annotations during code generation.
For a schema property to be considered mandatory, i.e. present and with a non-null value, it must be mentioned in the "required" list
AND NOT have a "nullable" indicator.

#### OpenAPI 3.0.x

The standard way to represent mandatory properties in OpenAPI 3.0.x is as follows:

```json
{
  "schemas": {
    "Person": {
      "type": "object",
      "properties": {
        "name": {
          "type": "string"
        },
        "address": {
          "$ref": "#/components/schemas/Address"
        }
      },
      "required": [ "name", "address" ]
    }
  }
}
```

Correspondingly, the standard way to represent non-mandatory (nullable) properties is like this:

```json
{
  "schemas": {
    "Person": {
      "type": "object",
      "properties": {
        "name": {
          "type": "string",
          "nullable": true
        },
        "address": {
          "oneOf": [
            {
              "$ref": "#/components/schemas/Address"
            },
            {
              "type": "object",
              "nullable": true
            }
          ]
        }
      },
      "required": []
    }
  }
}
```

Note the use of "OneOf" to express a nullable object reference.

#### OpenAPI 3.1.x

The standard way to represent mandatory properties in OpenAPI 3.1.x is as follows:

```json
{
  "schemas": {
    "Person": {
      "type": "object",
      "properties": {
        "name": {
          "type": "string"
        },
        "address": {
          "$ref": "#/components/schemas/Address"
        }
      },
      "required": [ "name", "address" ]
    }
  }
}
```

Correspondingly, the standard way to represent non-mandatory (nullable) properties is like this:

```json
{
  "schemas": {
    "Person": {
      "type": "object",
      "properties": {
        "name": {
          "type": [ "string", "null" ]
        },
        "address": {
          "oneOf": [
            {
              "$ref": "#/components/schemas/Address"
            },
            {
              "type": "null"
            }
          ]
        }
      },
      "required": []
    }
  }
}
```

Note the use of "OneOf" to express a nullable object reference.

For convenience, a non-standard [schema extension](#customizing-code-generation) is available to express nullability uniformly regardless of property type:

```json
{
  "schemas": {
    "Person": {
      "type": "object",
      "properties": {
        "name": {
          "type": "string",
          "x-nullable": true
        },
        "address": {
          "$ref": "#/components/schemas/Address",
          "x-nullable": true
        }
      },
      "required": []
    }
  }
}
```

### Inheritance

Inheritance is not supported, per se, by OpenAPI schemas (that rely on the [JSON Schema](https://json-schema.org/) standard). However, inheritance can be "simulated" with composition by referencing a base schema in an "allOf" clause:

```json
{
  "schemas": {
    "Vehicle": {
      "type": "object",
      "properties": {
        "brand": {
          "type": "string"
        }
      },
      "required": [ "brand" ]
    },
    "Car": {
      "allOf": [
        {
          "$ref": "#/components/schemas/Vehicle"
        },
        {
          "type": "object",
          "properties": {
            "doors": {
              "type": "integer"
            }
          },
          "required": [ "doors" ]
        }
      ]
    },
    "MotorCycle": {
      "allOf": [
        {
          "$ref": "#/components/schemas/Vehicle"
        },
        {
          "type": "object",
          "properties": {
            "sidekick": {
              "type": "boolean"
            }
          },
          "required": [ "sidekick" ]
        }
      ]
    }
  }
}
```

This produces the following Java records:

```java
public record VehicleDto (
    @NotBlank String brand
) {}

public record CarDto (
    @NotBlank String brand,
    @NotNull Integer doors
) {}

public record MotorCycleDto (
    @NotBlank String brand,
    @NotNull Boolean sidekick
) {}
```

## Limitations

* The OpenAPI specification must be contained in a single file. To bundle a multi-file OpenAPI specification, use a tool like [Redocly](https://redocly.com/) e.a.
* The plugin assumes the same request body schema for all content types consumed. While OpenAPI allows different body schema for different content types, this plugin uses the request body schema of the first content type specified for all.
* Supports "oneOf" with two subschemas only, one of which must be {"type": "null"}.
* Allows a single security requirement only, both at specification root and operation level.
* Supports single file upload requests only, using a "multipart/form-data" payload (in addition to zero or more additional metadata parts). Assumes file part is named "file".
  All other parts are considered primitive or complex metadata properties. Complex (type object) metadata parts are supported using "$ref" only.

The following schema constructs (based on the [JSON Schema](https://json-schema.org/) standard) are currently not supported,
and for the most part silently ignored during code generation:

* Restrictions on the "number" type: "multipleOf".
* Properties with "const".
* "string" properties with: "contentMediaType", "contentEncoding", "contentSchema".
* Dynamic objects: "if", "then", "unevaluatedProperties".
* Nested inline objects. Creating a separate schema and referencing it with "$ref" is recommended.
* Extended schema validation features: "patternProperties", "propertyNames", "minProperties", "maxProperties".
* Restrictions on arrays: tuple validation with "prefixItems".
* Dynamic arrays: "unevaluatedItems", "contains", "minContains", "maxContains".
* Documentation: "readOnly", "writeOnly".
* Property schema composition: "anyOf", "not".
* Conditional subschemas: "dependentRequired", "dependentSchemas", "if"-"then"-"else".
* Structuring: "$anchor", "$defs", recursion using "$ref".

## Contributing

1. Fork it (https://github.com/torand/openapi2java/fork)
2. Create your feature branch (git checkout -b feature/fooBar)
3. Commit your changes (git commit -am 'Add some fooBar')
4. Push to the branch (git push origin feature/fooBar)
5. Create a new Pull Request

## License

This project is licensed under the [Apache-2.0 License](LICENSE).