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

## Prerequisites

* **JDK 21+**
* **Maven 3.8+**

## How to build

From the repository root:

```bash
# Validate the multi-module reactor (parent + all modules)
mvn validate

# Compile and package everything
mvn -DskipTests package

# Build one service and its dependencies (example: music-service)
mvn -pl apps/music-service -am package
```

Coordinates: `com.musicstreaming:music-streaming-platform:0.1.0-SNAPSHOT`  
Parent: Spring Boot `3.5.5` · Java `21`

### Modules

| Module | Role |
|--------|------|
| `protos` | Shared Protocol Buffer / gRPC contracts |
| `platform/common` (`platform-common`) | Cross-cutting libraries (no domain logic) |
| `apps/api-gateway` | External REST edge |
| `apps/user-service` | User profiles |
| `apps/music-service` | Artists, albums, songs |
| `apps/playlist-service` | Playlists |
| `apps/social-service` | Likes and follows |
| `apps/streaming-service` | Audio streaming |
| `apps/history-service` | Listening history |
| `apps/analytics-service` | Playback analytics |
| `apps/notification-service` | Notifications |
| `apps/search-service` | Search / indexing |

## Status

Maven parent multi-module build is in place ([PE-13](https://amrkhald777.atlassian.net/browse/PE-13)). Next: service shells and Docker Compose (Wave 0 / [PE-14](https://amrkhald777.atlassian.net/browse/PE-14), [PE-16](https://amrkhald777.atlassian.net/browse/PE-16)).
