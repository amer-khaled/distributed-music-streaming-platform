# Music Streaming Platform

## Software Requirements Specification (SRS)

**Version:** 1.1  
**Status:** Draft  
**Project Type:** Production-oriented backend engineering project

---

# 1. Introduction

## 1.1 Purpose

The Music Streaming Platform is a production-oriented music streaming system inspired by platforms such as Spotify.

The system will allow users to:

* Register and authenticate.
* Browse artists, albums, and songs.
* Search for music.
* Stream audio.
* Create and manage playlists.
* Like songs, albums, and artists.
* Follow artists.
* Maintain listening history.
* Receive notifications.
* Track playback events and platform analytics.

The primary purpose of the project is to build and operate a realistic backend system that demonstrates:

* Modern backend engineering.
* Distributed systems.
* Inter-service communication.
* Scalability.
* Reliability.
* Security.
* Observability.
* DevOps and infrastructure engineering.

---

# 2. Scope

## 2.1 In Scope

The initial system will include:

* User management.
* Authentication and authorization.
* Artist management.
* Album management.
* Song/catalog management.
* Audio-file management.
* Audio streaming.
* Playlist management.
* Likes and follows.
* Search.
* Listening history.
* Playback analytics.
* Notifications.
* Event-driven communication.
* gRPC-based service-to-service communication.
* Redis caching.
* Object storage.
* Observability.
* Automated testing.
* Docker.
* CI/CD.
* Kubernetes deployment.

## 2.2 Explicitly Out of Scope

The following are not part of the initial release:

* Personalized music recommendations.
* Machine-learning recommendation systems.
* AI-generated playlists.
* Social feed.
* Lyrics.
* Podcast management.
* Live audio.
* Video streaming.
* Real-money payment processing.
* Music licensing management.

Recommendation functionality may be considered in a future version.

---

# 3. Goals

The project shall demonstrate the ability to design, implement, deploy, and operate a production-oriented distributed backend.

The project should provide practical experience with:

* Java.
* Spring Boot.
* REST APIs.
* gRPC.
* Protocol Buffers.
* PostgreSQL.
* Redis.
* Apache Kafka.
* OpenSearch/Elasticsearch.
* Object storage.
* Distributed systems.
* Concurrency.
* Fault tolerance.
* Observability.
* Docker.
* Kubernetes.
* CI/CD.
* Automated testing.

The project shall prioritize engineering quality and realistic architectural decisions over the number of features.

---

# 4. Architecture Principles

The system shall follow these communication principles.

## 4.1 External Communication

Client-facing APIs shall use:

**REST + JSON over HTTPS**

Example:

```text
Client
  |
  | HTTPS / REST
  v
API Gateway
```

---

## 4.2 Synchronous Service-to-Service Communication

Internal synchronous communication shall use:

**gRPC + Protocol Buffers**

Example:

```text
Playlist Service
      |
      | gRPC
      v
Music Service
```

gRPC shall be used when one service requires an immediate response from another service.

Examples:

* Playlist Service → Music Service
* Streaming Service → Music Service
* User Service → Artist Service
* Notification Service → User Service

---

## 4.3 Asynchronous Communication

Apache Kafka shall be used for asynchronous communication and event propagation.

Example:

```text
Streaming Service
       |
       | SONG_PLAYED
       v
     Kafka
       |
   ┌───┼───────────┐
   v   v           v
History Analytics Notification
```

Kafka shall be preferred when the producer does not need an immediate response from downstream consumers.

---

## 4.4 Communication Decision

The project shall maintain a clear distinction:

```text
External API
      ↓
 REST/JSON

Internal synchronous communication
      ↓
 gRPC/Protobuf

Internal asynchronous communication
      ↓
 Kafka
```

REST shall not be used as the default communication mechanism between internal services.

Kafka shall not be used for operations that require an immediate synchronous response unless there is a specific architectural reason.

---

# 5. System Architecture

The target architecture shall evolve toward:

```text
                         ┌──────────────┐
                         │    Client    │
                         └──────┬───────┘
                                │
                           HTTPS / REST
                                │
                         ┌──────▼───────┐
                         │ API Gateway  │
                         └──────┬───────┘
                                │
                          Internal gRPC
                                │
       ┌────────────────────────┼────────────────────────┐
       │             │          │          │             │
       ▼             ▼          ▼          ▼             ▼
     Auth          User       Music     Playlist      Streaming
    Service       Service    Service    Service       Service
       │             │          │          │             │
       │             │          │          │             │
       └─────────────┴──────────┴──────────┴─────────────┘
                              │
                            gRPC
                              │
                 ┌────────────┴────────────┐
                 │                         │
              PostgreSQL                 Redis
                 │
                 │
                 └────────────────────┐
                                      │
                                    Kafka
                                      │
                   ┌──────────────────┼──────────────────┐
                   ▼                  ▼                  ▼
                History           Analytics        Notification
                Service            Service           Service

                                      │
                                      ▼
                               Object Storage
                                  S3 / MinIO

                                      │
                                      ▼
                                Search Service
                              OpenSearch/Elastic

                                      │
                                      ▼
                               Observability
                         ┌─────────────────────────┐
                         │ OpenTelemetry           │
                         │ Prometheus              │
                         │ Grafana                 │
                         │ Loki                    │
                         │ Tempo / Jaeger          │
                         └─────────────────────────┘
```

This architecture is a target state. Services shall be introduced and separated incrementally rather than creating unnecessary distributed complexity at the beginning.

> **Project decision:** The team prefers a **microservices-first** delivery approach. See [ADR-0001](adr/0001-microservices-first.md). Incremental delivery still applies to *feature depth* and *infra maturity*, not to starting as a monolith.

---

# 6. User Types

## 6.1 Listener

A normal platform user.

A listener can:

* Create an account.
* Authenticate.
* Browse music.
* Search music.
* Stream songs.
* Create playlists.
* Modify playlists.
* Like songs.
* Like albums.
* Follow artists.
* View listening history.
* Manage account settings.
* Receive notifications.

## 6.2 Artist

An artist can:

* Manage their artist profile.
* Upload albums.
* Upload songs.
* Manage album metadata.
* Upload audio files.
* View basic statistics for their content.

## 6.3 Administrator

An administrator can:

* Manage users.
* Manage artists.
* Manage albums.
* Manage songs.
* Moderate content.
* Manage platform configuration.
* View platform-level analytics.

---

# 7. Functional Requirements

# 7.1 Authentication and Identity

The system shall provide secure user authentication.

### Requirements

* Users shall be able to register.
* Users shall be able to authenticate.
* Users shall be able to log out.
* Users shall be able to refresh authentication sessions.
* The system shall support role-based authorization.
* Protected APIs shall require authentication.
* Authorization shall be enforced server-side.
* Authentication shall support OAuth2/OIDC.
* The system shall integrate with an identity provider.

### Candidate Technology

**Keycloak**

The application should not implement password management unless there is a specific learning requirement.

---

## 7.2 User Management

A user profile shall contain information such as:

```text
id
username
displayName
email
profileImage
createdAt
updatedAt
status
```

Users shall be able to:

* View their profile.
* Update their profile.
* Change profile image.
* View their playlists.
* View liked content.
* View followed artists.

---

## 7.3 Artist Management

Artist profiles shall contain information such as:

```text
id
name
description
image
createdAt
updatedAt
status
```

Users shall be able to:

* View artist profiles.
* Follow artists.
* Unfollow artists.
* View artist albums.
* View artist songs.

Artists shall be able to manage their own catalog.

---

## 7.4 Album Management

An album shall belong to an artist.

An album shall contain:

```text
id
artistId
title
description
coverImage
releaseDate
albumType
createdAt
updatedAt
```

Supported album types may include:

```text
ALBUM
SINGLE
EP
```

Users shall be able to:

* Browse albums.
* View album details.
* View album songs.
* Like/unlike albums.

---

## 7.5 Song Management

A song shall contain:

```text
id
albumId
artistId
title
description
duration
trackNumber
genre
releaseDate
audioFileReference
coverImageReference
status
createdAt
updatedAt
```

The system shall support:

* Creating songs.
* Updating song metadata.
* Removing/deactivating songs.
* Associating songs with albums.
* Uploading audio files.
* Uploading cover images.

---

## 7.6 Audio Storage

Audio files shall not be stored directly in PostgreSQL.

The system shall use object storage.

```text
Song
 |
 └── audioFileReference
          |
          └── Object Storage
```

The database shall store metadata and references rather than large binary audio files.

Development may use:

**MinIO**

Production may use an S3-compatible provider.

---

## 7.7 Audio Streaming

The platform shall support audio streaming.

The streaming API shall support HTTP range requests.

Example:

```http
GET /api/v1/songs/{songId}/stream
Range: bytes=0-1048575
```

The system shall:

* Validate access.
* Locate the audio file.
* Support range requests.
* Avoid loading the entire audio file into application memory.
* Stream data efficiently.
* Handle interrupted connections.
* Handle invalid ranges.
* Handle missing audio files.

The streaming service shall be designed so large audio files do not unnecessarily consume application memory.

---

## 7.8 Playlist Management

Users shall be able to create playlists.

A playlist shall contain:

```text
id
ownerId
name
description
coverImage
visibility
createdAt
updatedAt
```

Supported visibility:

```text
PRIVATE
PUBLIC
```

Users shall be able to:

* Create playlists.
* Rename playlists.
* Delete playlists.
* Add songs.
* Remove songs.
* Reorder songs.
* View playlists.
* Change playlist visibility.

Playlist ordering shall be preserved.

---

## 7.9 Likes

Users shall be able to like and unlike:

* Songs.
* Albums.
* Artists.

Duplicate likes shall not be allowed.

Example:

```text
(userId, songId) UNIQUE
```

---

## 7.10 Artist Following

Users shall be able to follow and unfollow artists.

Duplicate follow relationships shall not be allowed.

Example:

```text
(userId, artistId) UNIQUE
```

---

## 7.11 Search

The platform shall provide search functionality.

Users shall be able to search:

* Songs.
* Artists.
* Albums.
* Playlists.

Example:

```http
GET /api/v1/search?q=bohemian
```

Search shall support:

* Partial matches.
* Tokenization.
* Relevance ranking.
* Filtering by content type.
* Pagination.

A dedicated search engine shall be used instead of relying exclusively on SQL `LIKE` queries.

Candidate technology:

**OpenSearch / Elasticsearch**

---

## 7.12 Listening History

The system shall record users' listening activity.

A listening record may contain:

```text
id
userId
songId
startedAt
completedAt
durationPlayed
source
device
```

Users shall be able to:

* View recently played songs.
* View listening history.
* Remove history entries where supported.

Listening events should be processed asynchronously where practical.

---

## 7.13 Playback Events

The system shall generate events for playback activity.

Examples:

```text
SONG_PLAY_STARTED
SONG_PLAY_COMPLETED
SONG_PLAY_PAUSED
SONG_PLAY_SKIPPED
```

Events shall contain sufficient information for downstream consumers.

Example:

```json
{
  "eventId": "...",
  "eventType": "SONG_PLAY_STARTED",
  "userId": "...",
  "songId": "...",
  "timestamp": "...",
  "deviceId": "..."
}
```

Playback events shall be published through Kafka.

---

## 7.14 Analytics

The system shall collect platform events for analytics.

Examples:

* Number of plays.
* Completed plays.
* Skips.
* Most-played songs.
* Most-followed artists.
* Playlist activity.
* Daily active listeners.

Analytics processing shall not unnecessarily block synchronous user requests.

---

## 7.15 Notifications

The platform shall support notifications.

Possible notification events include:

* New release from a followed artist.
* Playlist-related activity.
* Account events.
* System notifications.

The notification system shall support asynchronous processing.

Possible delivery mechanisms:

```text
In-app
Email
WebSocket / SSE
```

Users shall be able to manage notification preferences.

---

# 8. gRPC Requirements

Internal services shall expose gRPC APIs where synchronous communication is required.

## 8.1 Protocol Buffers

Service contracts shall be defined using `.proto` files.

Example:

```proto
syntax = "proto3";

service MusicService {

    rpc GetSong(GetSongRequest)
        returns (SongResponse);

    rpc GetAlbum(GetAlbumRequest)
        returns (AlbumResponse);

    rpc GetArtist(GetArtistRequest)
        returns (ArtistResponse);
}
```

---

## 8.2 gRPC Requirements

The system shall:

* Use Protocol Buffers for service contracts.
* Version gRPC APIs where necessary.
* Maintain backward-compatible protobuf changes.
* Use deadlines/timeouts.
* Propagate request metadata where appropriate.
* Propagate correlation/trace identifiers.
* Use appropriate gRPC status codes.
* Implement centralized error handling.
* Support authentication between services.
* Support observability through gRPC interceptors.
* Avoid exposing internal gRPC endpoints directly to public clients unless explicitly required.

---

## 8.3 gRPC Reliability

Internal gRPC calls shall define:

* Connection timeout.
* Request deadline.
* Retry policy where safe.
* Maximum message size.
* Error handling.
* Circuit-breaking behavior where appropriate.

Retries shall not be blindly applied to non-idempotent operations.

---

## 8.4 Example Service Interaction

When adding a song to a playlist:

```text
Client
  |
  | POST /playlists/{id}/songs
  ↓
API Gateway
  |
  ↓
Playlist Service
  |
  | gRPC: GetSong(songId)
  ↓
Music Service
  |
  ↓
Playlist Service
  |
  ↓
PostgreSQL
```

The playlist service should verify that the requested song exists before creating the relationship.

---

# 9. Kafka Requirements

Kafka shall be used for asynchronous communication.

Potential topics:

```text
playback-events
playlist-events
user-events
artist-events
notification-events
```

The system shall document:

* Topic naming.
* Partition strategy.
* Message keys.
* Consumer groups.
* Ordering requirements.
* Retry strategy.
* Dead-letter strategy.
* Idempotency.
* Schema evolution.

---

# 10. Caching Requirements

Redis shall be used for selected high-read or latency-sensitive data.

Potential cached data:

```text
Song metadata
Artist metadata
Album metadata
Popular content
User sessions
Rate-limit state
```

The project shall define:

* Cache key structure.
* TTL.
* Eviction behavior.
* Cache invalidation.
* Cache-aside strategy.
* Behavior when Redis is unavailable.

---

# 11. Rate Limiting

The API shall implement rate limiting.

Different limits may apply to:

```text
Authentication
Search
Playlist operations
Streaming
Administrative APIs
```

Redis may be used for shared rate-limit state when multiple application instances are deployed.

---

# 12. Non-Functional Requirements

## 12.1 Performance

The system should:

* Use database connection pooling.
* Use caching where appropriate.
* Avoid loading entire audio files into memory.
* Use pagination.
* Process non-critical operations asynchronously.
* Avoid N+1 queries.
* Support concurrent listeners.

Performance shall eventually be validated through load testing.

---

## 12.2 Scalability

Stateless application services shall support horizontal scaling.

Example:

```text
             Load Balancer
                  |
        ┌─────────┼─────────┐
        ↓         ↓         ↓
     Music-1   Music-2   Music-3
```

The system shall not rely on local instance state for functionality requiring persistence across instances.

---

## 12.3 Reliability

The system shall define behavior for:

* Database failures.
* Redis failures.
* Kafka failures.
* gRPC timeouts.
* Service failures.
* Duplicate Kafka messages.
* Consumer crashes.
* Object-storage failures.

The system should use:

* Timeouts.
* Retries where appropriate.
* Circuit breakers where appropriate.
* Health checks.
* Graceful degradation.
* Dead-letter queues.
* Graceful shutdown.

---

## 12.4 Security

The system shall:

* Use HTTPS in deployed environments.
* Authenticate protected requests.
* Authorize operations based on roles and ownership.
* Validate input.
* Protect against common API vulnerabilities.
* Avoid exposing internal infrastructure details.
* Secure service-to-service communication.
* Keep secrets outside source code.

Secrets shall be provided through environment/configuration management.

---

## 12.5 Observability

The system shall provide:

### Logs

Structured logs containing:

```text
timestamp
service
level
traceId
spanId
requestId
message
```

### Metrics

Examples:

```text
HTTP request count
HTTP latency
HTTP error rate
JVM metrics
Database connection pool
Kafka consumer lag
Kafka processing failures
Redis hit/miss ratio
Active streams
gRPC request count
gRPC latency
gRPC error rate
```

### Distributed Tracing

Requests crossing multiple services shall be traceable.

Example:

```text
Gateway
   ↓
Playlist Service
   ↓ gRPC
Music Service
   ↓
PostgreSQL
```

Candidate technologies:

* OpenTelemetry.
* Prometheus.
* Grafana.
* Loki.
* Tempo/Jaeger.

---

# 13. Testing Requirements

The project shall contain multiple levels of testing.

## 13.1 Unit Tests

Business logic and isolated components shall be unit tested.

## 13.2 Integration Tests

Integration tests shall cover infrastructure such as:

* PostgreSQL.
* Redis.
* Kafka.
* Object storage.
* Search.
* gRPC communication where appropriate.

Testcontainers should be used where practical.

## 13.3 API Tests

REST APIs shall be tested for:

* Correct responses.
* Validation.
* Authentication.
* Authorization.
* Error handling.

## 13.4 gRPC Tests

gRPC services shall be tested for:

* Contract correctness.
* Successful RPC calls.
* Invalid requests.
* Error/status handling.
* Timeout behavior.
* Authentication/metadata.
* Backward compatibility where applicable.

## 13.5 End-to-End Tests

Important workflows shall be tested end-to-end.

Example:

```text
Register
   ↓
Authenticate
   ↓
Search song
   ↓
Create playlist
   ↓
Add song
   ↓
Stream song
   ↓
Playback event
   ↓
Listening history
```

## 13.6 Load Testing

The system shall eventually be tested with realistic concurrent traffic.

Candidate technology:

**k6**

---

# 14. Infrastructure Requirements

All services shall be containerized.

Development infrastructure shall be reproducible using Docker Compose initially.

The system shall eventually support Kubernetes.

Potential Kubernetes resources:

```text
Deployment
Service
ConfigMap
Secret
Ingress
HorizontalPodAutoscaler
PersistentVolume
NetworkPolicy
```

Services shall expose:

```text
Liveness endpoint
Readiness endpoint
Metrics endpoint
```

---

# 15. CI/CD Requirements

Every repository change shall trigger automated validation.

The pipeline should follow:

```text
Git Push
   ↓
Build
   ↓
Unit Tests
   ↓
Integration Tests
   ↓
Static Analysis
   ↓
Package
   ↓
Docker Build
   ↓
Container Registry
   ↓
Deployment
```

Deployment shall be blocked when required checks fail.

---

# 16. Deployment Environments

The system shall support:

```text
Development
      ↓
Staging
      ↓
Production
```

Environment-specific configuration shall not be hard-coded.

---

# 17. Disaster Recovery

The project shall define:

* PostgreSQL backup strategy.
* Database restoration procedure.
* Object-storage backup strategy.
* Kafka recovery considerations.
* Recovery Point Objective (RPO).
* Recovery Time Objective (RTO).

Initial RPO/RTO values may be defined as project targets and adjusted after testing.

---

# 18. Failure Engineering

The project shall intentionally test failure scenarios.

Examples:

```text
PostgreSQL unavailable
Redis unavailable
Kafka unavailable
Kafka consumer crashes
Duplicate Kafka event
gRPC timeout
gRPC service unavailable
Slow database query
Object storage unavailable
Network timeout
Expired authentication token
Large audio file
High concurrent streaming traffic
```

The behavior for each failure should be documented.

---

# 19. API Requirements

External APIs shall use REST/JSON.

Endpoints shall be versioned.

Example:

```text
/api/v1/users
/api/v1/artists
/api/v1/albums
/api/v1/songs
/api/v1/playlists
/api/v1/search
```

APIs shall:

* Follow REST principles where applicable.
* Use consistent HTTP status codes.
* Use consistent error responses.
* Support pagination.
* Support filtering where required.
* Be documented using OpenAPI.

Example error:

```json
{
  "code": "SONG_NOT_FOUND",
  "message": "Song was not found",
  "timestamp": "...",
  "traceId": "..."
}
```

---

# 20. Data Requirements

PostgreSQL shall be the primary transactional database.

The database shall use appropriate:

* Constraints.
* Foreign keys.
* Unique constraints.
* Indexes.
* Transactions.

Examples:

```text
users.email UNIQUE

playlist_song(playlist_id, song_id)

song_like(user_id, song_id) UNIQUE

artist_follow(user_id, artist_id) UNIQUE
```

Database migrations shall be version-controlled.

Candidate technology:

**Flyway**

---

# 21. Object Storage Requirements

Object storage shall be used for large binary objects.

Supported object types may include:

```text
Audio
Album covers
Artist images
User profile images
```

The application shall store references rather than large binary objects in PostgreSQL.

The object-storage abstraction should allow switching between:

```text
MinIO
S3
Other S3-compatible storage
```

without major changes to business logic.

---

# 22. Search Architecture

Search indexing shall be decoupled from transactional writes.

Example:

```text
Music Service
     |
     | Song Created
     ↓
   Kafka
     |
     ↓
Search Indexer
     |
     ↓
OpenSearch
```

This means PostgreSQL remains the source of truth while the search index is eventually consistent.

The system shall define behavior when the search index is temporarily unavailable.

---

# 23. Service Ownership

Each service shall have clear responsibility for its domain.

Example:

```text
Auth Service
    → Identity/authentication

User Service
    → User profile/domain data

Music Service
    → Artists, albums, songs

Playlist Service
    → Playlists and playlist relationships

Streaming Service
    → Audio delivery and playback initiation

History Service
    → Listening history

Analytics Service
    → Aggregated platform statistics

Notification Service
    → Notification delivery

Search Service
    → Search/indexing
```

Services should not directly access another service's database.

Inter-service data access shall occur through:

```text
gRPC
or
Kafka
```

as appropriate.

---

# 24. Future Extensions

Potential future features:

* Personalized recommendations.
* Collaborative playlists.
* Lyrics.
* Podcasts.
* Offline downloads.
* Subscription plans.
* Payment processing.
* Advanced analytics.
* ML-based recommendations.
* CDN integration.
* Multi-region deployment.

---

# 25. Definition of Done

The project shall not be considered complete merely because the APIs work.

The production-oriented release should include:

* Core functionality.
* Authentication and authorization.
* PostgreSQL persistence.
* gRPC service-to-service communication.
* Kafka event processing.
* Redis caching.
* Audio streaming.
* Object storage.
* Search.
* Automated tests.
* Dockerized services.
* CI/CD.
* Kubernetes deployment.
* Metrics.
* Structured logging.
* Distributed tracing.
* Load testing.
* Failure testing.
* API documentation.
* gRPC/Protobuf contracts.
* Architecture documentation.
* Database migrations.
* Externalized secrets.

---

# 26. Initial Milestones

## Milestone 1 — Architecture & Foundation

* Repository structure.
* Service boundaries.
* Spring Boot foundation.
* PostgreSQL.
* Flyway.
* Docker.
* Initial CI.
* Initial API conventions.
* Initial `.proto` contracts.

## Milestone 2 — Identity

* Keycloak.
* OAuth2/OIDC.
* Authentication.
* Authorization.
* User management.

## Milestone 3 — Music Catalog

* Artists.
* Albums.
* Songs.
* Catalog APIs.
* Music gRPC service.

## Milestone 4 — Storage & Streaming

* MinIO/S3.
* Audio upload.
* Audio metadata.
* HTTP range requests.
* Streaming service.

## Milestone 5 — Playlists & Social Features

* Playlists.
* Likes.
* Follows.
* Playlist ↔ Music gRPC communication.

## Milestone 6 — Redis

* Caching.
* Cache invalidation.
* Rate limiting.
* Distributed cache behavior.

## Milestone 7 — Kafka

* Playback events.
* History.
* Analytics.
* Event retries.
* DLQ.
* Idempotent consumers.

## Milestone 8 — Search

* OpenSearch/Elasticsearch.
* Kafka-based indexing.
* Search API.
* Eventual consistency handling.

## Milestone 9 — Notifications

* Notification service.
* Notification preferences.
* Async notification processing.
* SSE/WebSocket where appropriate.

## Milestone 10 — Production Engineering

* Timeouts.
* Retries.
* Circuit breakers.
* Graceful shutdown.
* Failure testing.
* Load testing.
* Performance tuning.

## Milestone 11 — Observability

* OpenTelemetry.
* Prometheus.
* Grafana.
* Loki.
* Distributed tracing.
* Dashboards and alerts.

## Milestone 12 — Kubernetes & Deployment

* Docker images.
* Container registry.
* Kubernetes.
* Ingress.
* Secrets/configuration.
* Autoscaling.
* CI/CD.
* Staging.
* Production deployment.

---

# 27. Engineering Principles

The project shall follow these principles:

1. **Correctness before optimization.**
2. **Simple architecture before unnecessary complexity.**
3. **Every distributed component must have a clear reason to exist.**
4. **REST is primarily for external APIs.**
5. **gRPC is the default for synchronous internal communication.**
6. **Kafka is used for asynchronous/event-driven communication.**
7. **Services own their data.**
8. **Failures are expected and must be handled explicitly.**
9. **Observability is part of the system, not an afterthought.**
10. **Infrastructure should be reproducible.**
11. **Database constraints should enforce data integrity.**
12. **Architecture decisions should document trade-offs.**
13. **Performance assumptions must eventually be measured.**
14. **Distributed complexity should be introduced only when it provides engineering value.**
15. **The system should evolve incrementally toward the target architecture.**
