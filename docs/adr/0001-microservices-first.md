# ADR-0001: Microservices-First Delivery

**Status:** Accepted  
**Date:** 2026-09-17  
**Related:** [SRS §5](../SRS.md), [ADR-0004](0004-first-cut-topology.md)

## Context

The SRS describes a target distributed architecture and notes that services may be introduced incrementally to avoid premature complexity. The team prefers to **begin directly with microservices** rather than a modular monolith that is later split.

The learning and engineering goals of the project emphasize distributed systems, gRPC, Kafka, independent deployability, and failure handling across service boundaries. Starting as a monolith would delay those concerns until late in the schedule.

## Decision

We will adopt a **microservices-first** approach from day one:

1. The repository is structured as a multi-service monorepo (one deployable unit per bounded context).
2. Service process boundaries exist from Milestone 1 scaffolding onward.
3. Inter-service communication uses gRPC (sync) and Kafka (async) as specified in the SRS — not REST between services.
4. Each service owns its data store (see [ADR-0005](0005-database-per-service.md)).
5. **Incremental delivery still applies** to feature depth, reliability hardening, and infrastructure maturity — not to whether services are separate processes.

“Microservices-first” does **not** mean every SRS capability is fully implemented in wave 1. It means the deployable boundaries and contracts exist early, with thin or stub implementations where needed.

## Consequences

### Positive

* Early practice with gRPC contracts, service discovery, and distributed failure modes.
* Clear ownership boundaries force explicit APIs instead of hidden cross-module coupling.
* Aligns CI/CD, Docker, and Kubernetes work with the production target shape.
* Matches the project’s educational and production-oriented goals.

### Negative / Risks

* Higher local-dev and CI complexity (many containers, shared infra).
* Distributed transactions and consistency issues appear earlier.
* Cross-cutting concerns (auth, tracing, errors, config) must be standardized early.
* Risk of over-splitting if boundaries are wrong.

### Mitigations

* Shared libraries for cross-cutting concerns (auth adapters, observability, error model) — not shared domain logic.
* Docker Compose for reproducible local topology.
* Explicit delivery waves ([ADR-0004](0004-first-cut-topology.md)) so not every service is feature-complete at once.
* Prefer fewer, clearer bounded contexts over fine-grained “nano-services.”
* Document and revisit boundaries when coupling or chatty gRPC patterns appear.

## Alternatives Considered

| Option | Why not chosen |
|--------|----------------|
| Modular monolith first, extract later | Delays distributed-systems learning; extraction cost is often underestimated |
| Start with 2–3 services only, grow later | Acceptable compromise, but team prefers full boundary map early; wave-based depth covers the risk |
| One service per endpoint / CRUD entity | Over-fragmentation; rejected in favor of domain-aligned services ([ADR-0002](0002-service-boundaries.md)) |
