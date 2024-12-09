package io.github.torand.openapi2java.collectors;

import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;
import java.util.Optional;

import static io.github.torand.openapi2java.utils.CollectionHelper.nonEmpty;
import static io.github.torand.openapi2java.utils.Exceptions.illegalStateException;
import static java.util.Objects.nonNull;

public class SchemaResolver {
    private final Map<String, Schema> schemas;

    SchemaResolver(Map<String, Schema> schemas) {
        this.schemas = schemas;
    }

    public String getTypeName(String $ref) {
        return $ref.replace("#/components/schemas/", "");
    }

    public Optional<JsonSchema> get(String $ref) {
        return Optional.ofNullable((JsonSchema)schemas.get(getTypeName($ref)));
    }

    public JsonSchema getOrThrow(String $ref) {
        return get($ref).orElseThrow(illegalStateException("Schema %s not found", $ref));
    }

    public boolean isEnumType(String $ref) {
        return get($ref).map(s -> nonNull(s.getEnum())).orElse(false);
    }

    public boolean isObjectType(String $ref) {
        return get($ref).map(s -> nonEmpty(s.getTypes()) && s.getTypes().contains("object")).orElse(false);
    }

    public boolean isArrayType(String $ref) {
        return get($ref).map(s -> nonEmpty(s.getTypes()) && s.getTypes().contains("array")).orElse(false);
    }

    public boolean isCompoundType(String $ref) {
        return get($ref).map(s -> nonEmpty(s.getAllOf())).orElse(false);
    }

    /**
     * Indicates if schema represents a non-enumerated primitive JSON type, i.e. string, number, integer or boolean
     */
    public boolean isPrimitiveType(String $ref) {
        return !isEnumType($ref) && !isObjectType($ref) && !isArrayType($ref) && !isCompoundType($ref);
    }
}
