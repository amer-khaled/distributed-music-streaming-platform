# ADR-0005: Database-per-Service

**Status:** Accepted  
**Date:** 2026-09-17  
**Related:** [ADR-0002](0002-service-boundaries.md), [SRS §20, §23](../SRS.md)

## Context

If multiple services share one schema, microservice boundaries become illusory: independent deployability, ownership, and failure isolation collapse. The SRS requires that services do not access each other’s databases.

## Decision

1. **Database-per-service:** each domain service has its own PostgreSQL database (logical DB on a shared Postgres instance is acceptable in development).
2. **Flyway migrations** live inside each service and version only that service’s schema.
3. **No cross-database foreign keys.** Remote entities are referenced by ID only.
4. **Shared Postgres server in Compose/dev** is an infrastructure convenience, not a shared data model.
5. Production may keep separate databases on shared or dedicated instances; the application contract remains database-per-service.

### Indicative databases

| Service | Database name (indicative) |
|---------|----------------------------|
| user-service | `user_db` |
| music-service | `music_db` |
| playlist-service | `playlist_db` |
| social-service | `social_db` |
| streaming-service | `streaming_db` (if any persistent session/state is required; prefer minimal) |
| history-service | `history_db` |
| analytics-service | `analytics_db` |
| notification-service | `notification_db` |
| search-service | primary store is OpenSearch; optional small Postgres for jobs/outbox if needed |

Keycloak uses its own database (`keycloak_db`) managed by the IdP, not by application services.

## Consequences

### Positive

* Enforces service ownership.
* Allows independent schema evolution and scaling.
* Matches production-oriented microservice practice.

### Negative / Risks

* No cross-service SQL joins; UIs needing aggregates must orchestrate via gateway or dedicated read models.
* Referential integrity across services is application-level (gRPC checks + eventual consistency).
* More connection pools and migration pipelines to operate.

### Mitigations

* Validate references synchronously when user actions require strong checks (e.g., playlist add → `GetSong`).
* Use Kafka + idempotent consumers for derived data (history, search, analytics).
* Consider transactional outbox in a later ADR for reliable event publishing.
