Important Differences between OpenAPI Versions 3.0.x and 3.1.x
==============================================================
 
- Examples
  * 3.0: "example" with single value.
  * 3.1: "examples" with array of values.

- Description of reference objects
  * 3.0: No "description" property. Might get "description" from referenced schema instead.
  * 3.1: Property "description" allowed. Overrides "description" from referenced schema.

- Nullable properties
  * 3.0: Supported using "nullable" property.
  * 3.1: "nullable" no longer supported. Instead, use ```{"type": ["<type-name>", "null"], ...}```.

- Nullable reference objects
  * 3.0: Not allowed. Use ```{"oneOf": [{"type":"object", "nullable":true}, {"$ref": "..."}]}```.
  * 3.1: "nullable" no longer supported. Instead, use ```{"oneOf": [{"$ref": "..."}, {"type": "null"}]}```.

- Schema: property "default"
  * 3.0: Value must be same type as schema.
  * 3.1: Value must be string.

- Security scheme "mutualTLS"
  * 3.0: Not supported.
  * 3.1: Supported.

- exclusiveMinimum/exclusiveMaximum
  * 3.0: A boolean value. To be combined with "minimum" and "maximum".
  * 3.1: A number value. Not to be combined with "minimum" and "maximum".

- File upload
  * 3.0: Use "format" (byte, binary, or base64) to describe file payloads.
  * 3.1: As part of JSON Schema compliance, contentEncoding and contentMediaType should be used for such specification.
