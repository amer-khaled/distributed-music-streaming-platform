# ADR-0006: Service Discovery with Eureka

**Status:** Accepted  
**Date:** 2026-09-18  
**Related:** [ADR-0001](0001-microservices-first.md), [ADR-0003](0003-communication-patterns.md), [ADR-0004](0004-first-cut-topology.md)

## Context

With many independently deployed services, hard-coding `localhost` host/port pairs does not scale and breaks as soon as instances move (Compose, multiple replicas, Kubernetes).

Clients (especially the API gateway and gRPC callers such as playlist → music) need a stable **logical service name** and a registry that tracks healthy instances.

## Decision

1. Use **Netflix Eureka** (via Spring Cloud Netflix) as the service registry for **local development and non-Kubernetes environments**.
2. Add a dedicated **`discovery-service`** (Eureka Server) on port **8761**.
3. Every domain service and the API gateway **register as Eureka clients** using `spring.application.name` as the registry key.
4. **Domain services use random ports** (`server.port=0`, `grpc.server.port=0`). Only **discovery-service (8761)** and **api-gateway (8080)** stay fixed for local/dev entry.
5. **api-gateway** is Spring Cloud Gateway with Eureka **discovery locator** so clients call `http://gateway:8080/{service-id}/**` and the gateway load-balances to registered instances.
6. Publish **gRPC listen port** in Eureka instance metadata (`grpc.port`) so gRPC clients can resolve both host and port from discovery.
7. In **Kubernetes**, prefer platform DNS / Services (`music-service.default.svc.cluster.local`) and treat Eureka as optional/dev-oriented; a later ADR may switch clients to Spring Cloud Kubernetes if needed.

### Naming

Registry IDs match Maven/`spring.application.name` values, e.g. `music-service`, `playlist-service`, `api-gateway`.

### Health

Eureka registration uses Spring Boot Actuator health. Liveness/readiness probes remain the source of truth for orchestrators; Eureka reflects instance availability for client-side discovery.

## Consequences

### Positive

* Services address each other by name, not by brittle host/port tables.
* Horizontal scaling of a service becomes a registry update.
* Aligns with Spring Boot / Spring Cloud ergonomics for Wave 0+.

### Negative / Risks

* Extra moving part in local topology (discovery must start first).
* Eureka is less common as the *production* control plane on Kubernetes than native DNS.
* Brief registration lag after startup (eventual visibility in the registry).

### Mitigations

* Document startup order: discovery → domain services → gateway.
* Keep `docs/ports.md` as the **default local port map**; discovery is the runtime authority.
* Tests disable Eureka client (`eureka.client.enabled=false`) so unit/context tests do not require a live registry.

## Alternatives considered

| Option | Why not (for now) |
|--------|-------------------|
| Consul | Excellent, but heavier ops for Wave 0; revisit if multi-datacenter or non-Java clients need it |
| ZooKeeper | Poor fit as primary app-level discovery for this stack |
| DNS-only / hard-coded Compose hostnames | Fine inside Compose network, insufficient for multi-instance client-side load balancing locally |
| K8s-only discovery from day one | Production target, but does not help bare `java -jar` local shells |
