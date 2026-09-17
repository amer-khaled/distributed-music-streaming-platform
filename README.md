# Distributed Music Streaming Platform

Production-oriented music streaming backend (Spring Boot microservices).

## Documentation

| Doc | Description |
|-----|-------------|
| [Software Requirements Specification](docs/SRS.md) | Product and engineering requirements |
| [Architecture overview](docs/architecture/overview.md) | First-cut topology and service map |
| [Architecture Decision Records](docs/adr/README.md) | Significant design decisions |

## Architecture snapshot

* **External:** REST/JSON via API Gateway  
* **Sync internal:** gRPC + Protobuf  
* **Async:** Apache Kafka  
* **Data:** PostgreSQL (database per service), Redis, MinIO, OpenSearch  
* **Identity:** Keycloak (OAuth2/OIDC)  
* **Approach:** Microservices-first — see [ADR-0001](docs/adr/0001-microservices-first.md)

## Build

Java 21 + Maven 3.8+.

```bash
mvn validate
mvn -pl apps/music-service -am package
```

| Module | Role |
|--------|------|
| `protos` | Shared `.proto` / gRPC contracts |
| `platform/common` | Cross-cutting libraries (no domain logic) |
| `apps/*` | Deployable microservices |

## Status

Architecture docs + Maven parent/multi-module skeleton. Service shells and Compose come next (Wave 0 / PE-13–PE-14).
