# ADR-0004: First-Cut Topology and Delivery Waves

**Status:** Accepted  
**Date:** 2026-09-17  
**Related:** [ADR-0001](0001-microservices-first.md), [ADR-0002](0002-service-boundaries.md), [architecture overview](../architecture/overview.md)

## Context

Microservices-first means process boundaries exist early, but implementing every feature in every service simultaneously is unrealistic. We need a **first-cut topology**: which deployables and infrastructure exist in local/dev from the start, and which capabilities deepen over milestones.

## Decision

### First-cut deployable topology

All of the following are first-class services/components in the monorepo and Compose stack (scaffolded early; depth varies by wave):

```text
                         ┌──────────────┐
                         │    Client    │
                         └──────┬───────┘
                                │ HTTPS / REST
                         ┌──────▼───────┐
                         │ API Gateway  │
                         └──────┬───────┘
                                │ gRPC
        ┌───────────┬───────────┼───────────┬───────────┐
        ▼           ▼           ▼           ▼           ▼
   user-service music-service playlist  social   streaming
                              -service  -service  -service
        │           │           │           │           │
        └───────────┴───────────┴───────────┴───────────┘
                    │                 │
                    │                 ├──► history-service
                    │ Kafka           ├──► analytics-service
                    │                 ├──► notification-service
                    │                 └──► search-service
                    │
         ┌──────────┼──────────┬────────────┬────────────┐
         ▼          ▼          ▼            ▼            ▼
    PostgreSQL   Redis      Kafka        MinIO      OpenSearch
    (per svc)             (+ optional              (search)
                           Schema Registry later)
         │
         └── Keycloak (IdP)
```

### Shared infrastructure (Compose from foundation)

| Component | Role in first cut |
|-----------|-------------------|
| PostgreSQL | One server, **separate database per service** |
| Redis | Caching / rate-limit state (wired when Milestone 6 lands) |
| Kafka (+ ZooKeeper or KRaft) | Event bus (wired when producers exist) |
| MinIO | Object storage for audio/images |
| OpenSearch | Search backend (wired when search-service indexes) |
| Keycloak | OIDC IdP |
| Observability stack | Can land later (Milestone 11); reserve ports/config early if useful |

### Delivery waves (depth, not new boundaries)

Boundaries are fixed early; implementation depth follows SRS milestones:

| Wave | Focus | Services that become “real” |
|------|-------|-------------------------------|
| **W0 — Foundation** | Repo, shared build, protos, Compose, CI skeleton, health endpoints | All services as thin Spring Boot shells + gateway stub |
| **W1 — Identity** | Keycloak, JWT resource servers, user profile | gateway, user-service, Keycloak |
| **W2 — Catalog** | Artists/albums/songs + music gRPC | music-service |
| **W3 — Storage & stream** | Upload refs, range streaming, playback events (publish) | music-service, streaming-service, MinIO |
| **W4 — Playlists & social** | Playlists, likes, follows + cross-service gRPC | playlist-service, social-service |
| **W5 — Cache & limits** | Redis cache-aside, rate limits | gateway + read-heavy services |
| **W6 — Async consumers** | History, analytics; DLQ/idempotency | history-service, analytics-service, Kafka hardening |
| **W7 — Search** | Indexer + search API | search-service, OpenSearch |
| **W8 — Notifications** | Preferences + delivery | notification-service |
| **W9 — Production eng** | Timeouts, breakers, load/failure tests | all |
| **W10 — Observability & K8s** | OTel, Prometheus/Grafana/Loki/Tempo, K8s, CD | platform |

Thin shells in W0 must still:

* Build and start in Compose
* Expose health (liveness/readiness)
* Own an empty or placeholder DB + Flyway baseline
* Reserve gRPC/HTTP ports
* Depend on shared proto artifacts where contracts already exist

### Repository layout (first cut)

```text
/
├── docs/
├── protos/                 # shared .proto contracts
├── platform/               # optional shared libraries (not domain logic)
├── apps/
│   ├── api-gateway/
│   ├── user-service/
│   ├── music-service/
│   ├── playlist-service/
│   ├── social-service/
│   ├── streaming-service/
│   ├── history-service/
│   ├── analytics-service/
│   ├── notification-service/
│   └── search-service/
├── deploy/
│   ├── docker-compose.yml
│   └── k8s/                # later
└── .gitignore
```

Exact Gradle/Maven multi-module layout will be decided at scaffolding time; this ADR locks **service names and topology**, not build-tool details.

## Consequences

### Positive

* Topology matches the SRS target without waiting for a rewrite.
* Waves keep scope honest while preserving microservice boundaries.
* Compose stack teaches the real operational shape early.

### Negative / Risks

* Many empty-ish services early can feel like noise.
* Port and dependency sprawl in local dev.
* Requires discipline so thin shells do not accumulate fake “temporary” shortcuts (shared DB, REST between services).

### Mitigations

* Strict checklist for shells (health, own DB, no cross-DB).
* Proto contracts reviewed before feature waves that need them.
* Prefer generating stubs from protos over hand-wavy placeholders.
