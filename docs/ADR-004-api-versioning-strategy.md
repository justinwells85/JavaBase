# ADR-004: API Versioning Strategy

## Status

Accepted

## Context

As microservices evolve, APIs will need to change. We need a consistent versioning strategy across all services that:

- Allows breaking changes without disrupting existing clients
- Provides clear migration paths
- Is easy for developers to implement consistently
- Supports gradual rollout of new versions

## Decision

Use URL-based versioning with the following conventions:

1. **Version in URL path**: `/api/v1/tasks`, `/api/v2/tasks`
2. **Version in package structure**: `controller/v1/`, `dto/v1/`
3. **Base controller class** provides version constants and headers
4. **Multiple versions can coexist** in the same service
5. **Deprecation policy**: 90-day sunset period for deprecated versions

### Implementation

```java
// Base controller with version constants
public abstract class VersionedController {
    public static final String API_V1 = "/api/v1";
    // Add API_V2 when needed
}

// Controller uses version constant
@RestController
@RequestMapping(VersionedController.API_V1 + "/tasks")
public class TaskController extends VersionedController { }
```

### Response Headers

All API responses include:
- `API-Version: v1` - The version that served this request
- `Deprecation: true` (if applicable) - Indicates deprecated endpoint
- `Sunset: 2025-03-30` (if applicable) - When version will be removed

## Consequences

### Positive

- **Clear and Explicit** - Versioning visible in URLs
- **Easy for Clients** - Simple to understand and use
- **Package Structure** - Obvious which code belongs to which version
- **Coexistence** - Multiple versions can run simultaneously
- **Easy Cleanup** - Delete packages when version is retired

### Negative

- **Code Duplication** - When maintaining multiple versions
- **URL Changes** - Clients must update URLs when upgrading
- **Discipline Required** - Must not break existing versions

## Alternatives Considered

1. **Header-based versioning** (`Accept: application/vnd.api.v1+json`)
   - Rejected: Less visible, harder to test with browser/Swagger

2. **Query parameter versioning** (`?version=1`)
   - Rejected: Can conflict with business query params

3. **No versioning**
   - Rejected: Breaking changes would impact all clients simultaneously

## Migration Process

When creating v2:

1. Create `controller/v2/` and `dto/v2/` packages
2. Copy and modify from v1
3. Add `API_V2` constant to `VersionedController`
4. Mark v1 with `@Deprecated` annotation
5. Add deprecation headers to v1 responses
6. Document migration guide
7. After sunset period, remove v1 code
