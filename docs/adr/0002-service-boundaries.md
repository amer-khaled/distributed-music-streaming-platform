# ADR-0002: Service Boundaries and Ownership

**Status:** Accepted  
**Date:** 2026-09-17  
**Related:** [SRS §7, §23](../SRS.md), [ADR-0001](0001-microservices-first.md)

## Context

A microservices-first system needs explicit bounded contexts so that teams (and future operators) know:

* What each service owns
* What it must not store or serve
* How other services obtain its data

Without clear ownership, services tend to share databases, duplicate domain models, or grow into distributed monoliths.

## Decision

The platform is partitioned into the following **domain services**. No service may read or write another service’s database.

### Platform edge

| Component | Responsibility | Does not own |
|-----------|----------------|--------------|
| **API Gateway** | External REST/JSON, routing, TLS termination (deployed), request auth validation / token introspection coordination, rate limiting entry point | Business domain logic, persistent domain data |
| **Keycloak** (IdP) | Identity provider: users’ credentials, OAuth2/OIDC tokens, realm/roles | Application profile data beyond identity claims |

> “Auth Service” in the SRS maps primarily to **Keycloak + gateway/resource-server integration**. We do **not** build a custom password-auth service unless a later ADR says otherwise.

### Core domain services

| Service | Owns | Key capabilities |
|---------|------|------------------|
| **user-service** | Listener/admin profile domain data | Profile CRUD, profile image reference, account status; sync/link to Keycloak subject (`sub`) |
| **music-service** | Artists, albums, songs (catalog metadata) | Catalog CRUD, audio/cover object references, catalog gRPC API |
| **playlist-service** | Playlists and playlist–song ordering | Playlist CRUD, visibility, reorder; validates songs via music-service gRPC |
| **social-service** | Likes and artist follows | Song/album/artist likes, follows; unique constraints; may call music/user via gRPC for existence checks |
| **streaming-service** | Audio delivery & playback session signals | Range-request streaming from object storage; access checks; emits playback events to Kafka |
| **history-service** | Listening history | Consume playback events; query recent/history APIs |
| **analytics-service** | Aggregated platform metrics | Consume events; produce aggregates (plays, skips, DAU-style counters) |
| **notification-service** | Notifications & preferences | Async notification processing; in-app (and later email/SSE); preference storage |
| **search-service** | Search index & query API | Consume catalog/playlist events; index OpenSearch; search REST via gateway |

### Likes / follows placement

SRS lists likes and follows as features but does not mandate a dedicated service. **Decision:** place them in **social-service** rather than inside user-service or music-service, so:

* Catalog services stay free of per-user graph write load
* User-service stays focused on profile identity linkage
* Playlist-service stays focused on playlist aggregates

If social-service remains thin after Milestone 5, a future ADR may merge it into user-service. That is preferred over scattering like/follow tables across multiple DBs.

### Explicit non-ownership rules

* Services **must not** join across another service’s tables.
* Foreign keys across service databases are **forbidden**. Store remote IDs as opaque references and validate via gRPC or trust eventual consistency from events.
* Object bytes (audio, images) live in **object storage**; services store only references/keys.
* Search index is **not** the source of truth; PostgreSQL (per owning service) is.

## Consequences

### Positive

* Clear team/module ownership and deployable units.
* Forces contract-first design (`.proto` and events).
* Aligns with SRS service ownership examples.

### Negative / Risks

* Cross-service reads require gRPC or denormalized read models.
* Existence checks (e.g., add song to playlist) add latency and failure modes.
* social-service is a judgment call; may be merged later.

### Follow-ups

* Define public gRPC packages per service under a shared `protos/` tree.
* Define Kafka topic ownership (which service is producer of record).
* Document ID strategy (UUIDs recommended) in a later ADR if needed.
