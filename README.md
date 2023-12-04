openapi2java
============

## Overview

Maven plugin to generate Java model and REST clients from an OpenAPI specification.

The current version supports the OpenAPI 3.1 specification only, using either YAML or JSON formats. The plugin outputs Java 17+
compatible source code using annotations from the following libraries:

* [Microprofile Rest Client](https://download.eclipse.org/microprofile/microprofile-rest-client-2.0/microprofile-rest-client-spec-2.0.html)
* [Microprofile OpenAPI](https://download.eclipse.org/microprofile/microprofile-open-api-2.0/microprofile-openapi-spec-2.0.html)
* [Jakarta Bean Validation](https://jakarta.ee/specifications/bean-validation/)
* [Jakarta RESTful Web Services](https://jakarta.ee/specifications/restful-ws/)
* [Jackson](https://github.com/FasterXML/jackson)

The OpenAPI document is parsed using the excellent [Swagger Parser](https://github.com/swagger-api/swagger-parser/) library. 

## Output

Pojos for the representation model are output to a 'model' subdirectory.
A REST client interface is output for each tag in the OpenAPI file.

## Usage in a Maven POM file

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.github.torand</groupId>
      <artifactId>openapi2java</artifactId>
      <version>1.0.0-SNAPSHOT</version>
      <executions>
        <execution>
         <id>generate</id>
         <phase>validate</phase>
         <goals>
            <goal>generate</goal>
          </goals>
          <configuration>
            <openApiFile>openapi.json</openApiFile>  
            <outputDir>target/openapi2java</outputDir>
            <rootPackage>org.github.johndoe.myapi</rootPackage>  
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

## Run from the command line

```bash
$ mvn org.github.torand:openapi2java:1.0.0-SNAPSHOT:generate \
  -DopenApiFile=openapi.json \
  -DoutputDir=target/openapi2java \
  -DrootPackage=org.github.johndoe.myapi
```

## Configuration

