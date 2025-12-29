# JavaBase API Documentation

## Overview

The JavaBase API provides RESTful endpoints for task management. All endpoints use JSON for request and response bodies.

**Base URL**: `http://localhost:8080/api/v1`

**Interactive Documentation**: [Swagger UI](http://localhost:8080/swagger-ui.html)

## Headers

### Request Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Content-Type` | Yes | Must be `application/json` for POST/PUT requests |
| `X-Correlation-Id` | No | UUID for request tracing (auto-generated if not provided) |
| `Idempotency-Key` | No | UUID for idempotent requests (recommended for POST/PUT/DELETE) |

### Response Headers

| Header | Description |
|--------|-------------|
| `API-Version` | Current API version (e.g., "v1") |
| `X-Correlation-Id` | Request correlation ID for tracing |
| `X-Idempotency-Replayed` | "true" if response was replayed from cache |

## Endpoints

### Create Task

Creates a new task.

```
POST /api/v1/tasks
```

**Request Body**:
```json
{
  "title": "Complete design document",
  "description": "Write comprehensive design doc for JavaBase",
  "assignee": "justin",
  "dueDate": "2024-12-30T17:00:00Z"
}
```

**Response**: `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Complete design document",
  "description": "Write comprehensive design doc for JavaBase",
  "status": "PENDING",
  "assignee": "justin",
  "dueDate": "2024-12-30T17:00:00",
  "createdAt": "2024-12-29T10:15:30",
  "updatedAt": "2024-12-29T10:15:30"
}
```

### Get Task

Retrieves a task by ID.

```
GET /api/v1/tasks/{id}
```

**Response**: `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Complete design document",
  "status": "PENDING",
  ...
}
```

**Error Response**: `404 Not Found`
```json
{
  "timestamp": "2024-12-29T10:15:30",
  "correlationId": "abc-123-def-456",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: xyz",
  "path": "/api/v1/tasks/xyz"
}
```

### Update Task

Updates an existing task.

```
PUT /api/v1/tasks/{id}
```

**Request Body**:
```json
{
  "title": "Updated title",
  "status": "IN_PROGRESS"
}
```

**Response**: `200 OK`

### Delete Task

Soft deletes a task.

```
DELETE /api/v1/tasks/{id}
```

**Response**: `204 No Content`

### List Tasks

Retrieves tasks with optional filtering and pagination.

```
GET /api/v1/tasks
```

**Query Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| `status` | string | Filter by status (PENDING, IN_PROGRESS, COMPLETED, CANCELLED) |
| `assignee` | string | Filter by assignee |
| `page` | int | Page number (0-based, default: 0) |
| `size` | int | Page size (default: 20) |

**Response**: `200 OK`
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Task 1",
      ...
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 45,
  "totalPages": 3
}
```

## Task Status

| Status | Description |
|--------|-------------|
| `PENDING` | Task created but not started |
| `IN_PROGRESS` | Task is being worked on |
| `COMPLETED` | Task finished successfully |
| `CANCELLED` | Task cancelled or deleted |

## Error Responses

All errors follow this format:

```json
{
  "timestamp": "2024-12-29T10:15:30",
  "correlationId": "abc-123-def-456",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/tasks",
  "fieldErrors": [
    {
      "field": "title",
      "message": "Title is required",
      "rejectedValue": null
    }
  ]
}
```

### HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 204 | No Content (successful delete) |
| 400 | Bad Request (validation error) |
| 404 | Not Found |
| 405 | Method Not Allowed |
| 409 | Conflict (idempotency) |
| 500 | Internal Server Error |

## Idempotency

For POST, PUT, and DELETE requests, include an `Idempotency-Key` header:

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{"title": "My Task"}'
```

If the same request is sent again with the same key:
- The original response is returned
- The `X-Idempotency-Replayed: true` header is added
- No duplicate task is created

Keys are valid for 24 hours.

## Health Endpoints

```
GET /actuator/health
```

Returns service health status.

```
GET /actuator/health/liveness
GET /actuator/health/readiness
```

Kubernetes-style probe endpoints.
