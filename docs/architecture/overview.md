# Architecture Overview

**Status:** First cut  
**Date:** 2026-09-17  
**Source of requirements:** [SRS](../SRS.md)  
**Decisions:** [ADR index](../adr/README.md)

## Intent

Build a production-oriented music streaming backend as a **microservices system from day one**, with REST at the edge, gRPC for synchronous internals, and Kafka for asynchronous work.

Incremental delivery applies to **feature and infrastructure depth**, not to collapsing services into a monolith. See [ADR-0001](../adr/0001-microservices-first.md).

## Service map

| Service | Domain | Sync API | Async role |
|---------|--------|----------|------------|
| api-gateway | Edge / BFF-style routing | REST in, gRPC out | Rate-limit coordination |
| user-service | Profiles | gRPC (+ REST via gateway) | `user-events` producer |
| music-service | Artists, albums, songs | gRPC catalog | `catalog-events` producer |
| playlist-service | Playlists | gRPC; calls music | `playlist-events` producer |
| social-service | Likes, follows | gRPC; calls music/user | `social-events` producer |
| streaming-service | Range streaming | HTTP stream via gateway; gRPC to music | `playback-events` producer |
| history-service | Listening history | Query via gateway | `playback-events` consumer |
| analytics-service | Aggregates | Query via gateway (admin/artist later) | Multi-topic consumer |
| notification-service | Notifications | Query/preferences via gateway | Multi-topic consumer |
| search-service | Search | Query via gateway | Catalog/playlist consumer → OpenSearch |
| Keycloak | Identity | OIDC | — |

Ownership detail: [ADR-0002](../adr/0002-service-boundaries.md).

## Communication

```text
Client ──REST/JSON──► API Gateway ──gRPC──► Domain services
Domain service ──gRPC──► Domain service   (immediate need)
Domain service ──Kafka─► Domain services  (async / fan-out)
```

Service instances are resolved via **Eureka** (`discovery-service`) using `spring.application.name`. gRPC listen ports are published as Eureka metadata `grpc.port`. See [ADR-0006](../adr/0006-service-discovery-eureka.md).

Full rules: [ADR-0003](../adr/0003-communication-patterns.md).

## First-cut topology

```text
Client
  │ HTTPS/REST
  ▼
API Gateway
  │ gRPC
  ├─► user-service
  ├─► music-service
  ├─► playlist-service
  ├─► social-service
  ├─► streaming-service  ──► MinIO (audio bytes)
  ├─► history-service
  ├─► analytics-service
  ├─► notification-service
  └─► search-service ──► OpenSearch

Kafka bus: playback, catalog, playlist, user, social, notification topics
PostgreSQL: one logical DB per service
Redis: cache + rate-limit state
Keycloak: IdP
```

Delivery waves and repo layout: [ADR-0004](../adr/0004-first-cut-topology.md).  
Data isolation: [ADR-0005](../adr/0005-database-per-service.md).

## Example flows

### Add song to playlist

```text
Client → Gateway → playlist-service
                      │ gRPC GetSong
                      ▼
                 music-service
                      │
                      ▼
              playlist DB write
                      │
                      ▼ Kafka playlist-events (optional consumers: search, analytics)
```

### Stream and record history

```text
Client → Gateway → streaming-service → music-service (metadata/object key)
                      │
                      ▼ MinIO range read
                      │
                      ▼ Kafka playback-events
                            ├─► history-service
                            ├─► analytics-service
                            └─► notification-service (if applicable)
```

### Catalog change → search

```text
music-service DB commit → catalog-events → search-service → OpenSearch
```

Search is eventually consistent; PostgreSQL in music/playlist services remains source of truth.

## Non-goals (initial release)

Per SRS: recommendations/ML, social feed, lyrics, podcasts, live/video, payments, licensing.

## Next engineering step

Scaffold Milestone 1 / Wave 0:

* Monorepo build (Gradle or Maven multi-module)
* Thin Spring Boot services + gateway
* Shared `protos/`
* Docker Compose for Postgres, Kafka, Redis, MinIO, Keycloak, OpenSearch
* Baseline Flyway per service
* Initial CI pipeline
