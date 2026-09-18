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
* **Discovery:** Eureka (`discovery-service`) for local/non-K8s; K8s DNS in cluster  
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
| `discovery-service` | Eureka service registry |
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

## Run service shells locally

Startup order: **discovery-service (Eureka)** → domain services (random ports) → **api-gateway :8080**.

```bash
mvn package -DskipTests

java -jar discovery-service/target/discovery-service-0.1.0-SNAPSHOT.jar &
java -jar apps/music-service/target/music-service-0.1.0-SNAPSHOT.jar &
# …start other domain services, then:
java -jar apps/api-gateway/target/api-gateway-0.1.0-SNAPSHOT.jar &

# Clients only call the gateway
curl http://localhost:8080/music-service/actuator/health
# Eureka dashboard: http://localhost:8761
```

Port map & gateway routing: [docs/ports.md](docs/ports.md) · [ADR-0006](docs/adr/0006-service-discovery-eureka.md)

## Status

* [PE-13](https://amrkhald777.atlassian.net/browse/PE-13) — Maven parent multi-module build ✅  
* [PE-14](https://amrkhald777.atlassian.net/browse/PE-14) — Thin Spring Boot shells (health + reserved ports) ✅  
* Service discovery (Eureka) — see ADR-0006 ✅  
* Next: Docker Compose infra ([PE-16](https://amrkhald777.atlassian.net/browse/PE-16)), protos ([PE-15](https://amrkhald777.atlassian.net/browse/PE-15))
