# NLGov ADR Compliance Reviewer

Review REST resource classes for compliance with NLGov API Design Rules.

## Scope

Scan Kotlin resource classes (`*Resource.kt`) in `services/*/src/main/kotlin/` for the following rules:

## Checklist

### 1. Field Naming — lowerCamelCase
- All DTO/entity field names MUST use lowerCamelCase
- Grep for `@field:Schema`, `val`/`var` declarations in DTO/entity classes
- Flag any snake_case or PascalCase fields

### 2. API-Version Header
- All resource responses MUST include `API-Version` header
- Check for a JAX-RS `ContainerResponseFilter` or `@ServerResponseFilter` that adds this header
- Flag if no such filter exists

### 3. Error Responses — application/problem+json (RFC 9457)
- All error responses MUST use `application/problem+json` content type
- Check `ExceptionMapper` implementations return Problem JSON
- Required fields: `type`, `title`, `status`, `detail`, `instance`
- Flag any error responses returning plain JSON or text

### 4. No Trailing Slashes
- `@Path` annotations MUST NOT end with `/`
- Grep for `@Path(".*/")`
- Flag any matches

### 5. HTTP Status Codes
- `POST` creating a resource -> 201 Created with Location header
- `DELETE` -> 204 No Content (or 200 if returning deleted entity)
- `PUT`/`PATCH` -> 200 OK
- `GET` collection -> 200 OK
- Flag incorrect status codes

### 6. Query vs Path Parameters
- Path parameters for resource identity: `/berichten/{berichtId}`
- Query parameters for filtering/pagination: `?pagina=1&aantal=20`
- Flag query parameters used for resource identity

## Output

For each resource class, report:
- File path and class name
- List of violations with rule reference and line number
- Severity: ERROR (must fix) or WARNING (should fix)
- Suggested fix for each violation

## How to Run

Use Glob to find all `*Resource.kt` files, then Read each one and check against the rules above. Also check for `ExceptionMapper` and `ContainerResponseFilter` implementations.
