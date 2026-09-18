# Ports and gateway routing

## Fixed entry points

| Component | Port | Role |
|-----------|------|------|
| discovery-service (Eureka) | **8761** | Service registry UI / API |
| api-gateway | **8080** | Sole public HTTP entry (clients talk only here) |

## Domain services — dynamic ports

`user`, `music`, `playlist`, `social`, `streaming`, `history`, `analytics`, `notification`, and `search` services use:

```properties
server.port=0
grpc.server.port=0
```

They register with Eureka under `spring.application.name`. Instance HTTP port is whatever the OS assigned; gRPC port is in Eureka metadata `grpc.port`.

## Gateway routing

Spring Cloud Gateway discovery locator is enabled (`lower-case-service-id: true`).

Clients call:

```text
http://localhost:8080/{service-id}/**
  →  lb://{service-id}/**
```

Examples:

```bash
curl http://localhost:8080/music-service/actuator/health
curl http://localhost:8080/user-service/actuator/health/liveness
```

Dashboard: http://localhost:8761
