# ADR-0003: Communication Patterns (REST, gRPC, Kafka)

**Status:** Accepted  
**Date:** 2026-09-17  
**Related:** [SRS §4, §8, §9](../SRS.md)

## Context

The platform has three distinct communication needs:

1. Client ↔ platform
2. Service ↔ service when an immediate answer is required
3. Service ↔ service when work can be deferred or fan-out

Mixing these (e.g., REST between all services, or Kafka for request/response) creates fragile coupling and poor operability.

## Decision

### External: REST + JSON (HTTPS)

* Clients talk only to the **API Gateway** (and IdP endpoints as required for OAuth/OIDC).
* Public APIs are versioned under `/api/v1/...`.
* OpenAPI documents external contracts.
* Internal gRPC ports are **not** exposed publicly.

### Synchronous internal: gRPC + Protobuf

Use gRPC when the caller needs a timely, authoritative answer, for example:

| Caller | Callee | Example |
|--------|--------|---------|
| playlist-service | music-service | `GetSong` before adding to playlist |
| streaming-service | music-service | Resolve audio object key / song status |
| social-service | music-service | Validate artist/album/song exists |
| notification-service | user-service | Resolve notification recipient profile |
| api-gateway | domain services | Prefer gRPC from gateway to services (gateway translates REST ↔ gRPC) |

Rules:

* Contracts live in versioned `.proto` files.
* Deadlines/timeouts on every call.
* Propagate `traceId` / correlation metadata via interceptors.
* Retries only for idempotent RPCs; no blind retries on creates.
* Circuit breaking where appropriate (Milestone 10 hardening).

### Asynchronous internal: Kafka

Use Kafka when the producer does not need the consumer’s result to complete the user request, for example:

| Producer | Event / topic (indicative) | Consumers |
|----------|----------------------------|-----------|
| streaming-service | `playback-events` | history, analytics, notification |
| music-service | `catalog-events` | search, notification |
| playlist-service | `playlist-events` | search, analytics |
| user-service | `user-events` | notification, search (if needed) |
| social-service | `social-events` | analytics, notification |

Rules:

* Document topic naming, keys, partitions, consumer groups, DLQ, and idempotency (Milestone 7).
* Prefer event-carried state transfer for search indexing (eventual consistency).
* Do **not** use Kafka as a synchronous request/response bus.

### Anti-patterns (rejected by default)

* REST as default service-to-service protocol
* Direct DB access across services
* Sharing mutable domain jars as the integration mechanism
* Dual-writing to DB and search in one request without an outbox/events strategy (search indexing must be async)

## Consequences

### Positive

* Matches SRS communication principles.
* Clear mental model for designers and reviewers.
* Gateway becomes the only public translation layer.

### Negative / Risks

* Gateway must be competent at auth, routing, and protocol translation.
* Proto and event schema evolution discipline is mandatory.
* Local debugging spans multiple processes.

### Follow-ups

* Shared error model for REST (`code`, `message`, `timestamp`, `traceId`).
* gRPC status ↔ HTTP status mapping at the gateway.
* Outbox pattern ADR when reliable event publishing is implemented.
