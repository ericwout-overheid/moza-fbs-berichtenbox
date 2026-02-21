# OpenAPI Spec Drift Checker

Compare OpenAPI specifications in `openapi/` with actual Kotlin implementations to detect drift.

## Scope

For each OpenAPI spec YAML/JSON file in `openapi/`, verify the Kotlin implementation matches.

## Checks

### 1. Endpoint Coverage
- Read all `paths` from the OpenAPI spec
- For each path + method, verify a corresponding `@Path` + `@GET`/`@POST`/`@PUT`/`@DELETE`/`@PATCH` exists in a resource class
- Flag endpoints defined in spec but missing in code (not implemented)
- Flag endpoints in code but missing from spec (undocumented)

### 2. Schema Field Matching
- For each schema in `components/schemas`, find the corresponding DTO/entity Kotlin class
- Compare field names — they must match exactly (lowerCamelCase)
- Compare field types (string -> String, integer -> Int/Long, boolean -> Boolean, array -> List)
- Flag mismatches in names, types, or missing fields

### 3. Response Status Codes
- For each operation in the spec, check the `responses` section
- Verify the Kotlin implementation returns the same status codes
- Flag mismatches (e.g., spec says 201 but code returns 200)

### 4. API-Version Header
- Check if `API-Version` header is documented in spec response headers
- Check if it's implemented in code (via filter or annotation)
- Flag if present in one but not the other

### 5. Request/Response Content Types
- Verify `application/json` for normal responses
- Verify `application/problem+json` for error responses
- Check that spec and code agree on content types

## Steps

1. Use Glob to find all `openapi/*.yaml` and `openapi/*.json` files
2. Read each spec file
3. Use Glob to find corresponding resource classes in `services/*/src/main/kotlin/`
4. Read the resource classes and DTO classes
5. Compare spec vs implementation for each check above

## Output

For each spec file, report:
- Spec file path
- Corresponding service/module
- List of drift findings:
  - Type: MISSING_ENDPOINT, EXTRA_ENDPOINT, FIELD_MISMATCH, STATUS_MISMATCH, HEADER_MISMATCH
  - Severity: ERROR or WARNING
  - Spec reference (path + line if possible)
  - Code reference (file + line)
  - Description of the mismatch
