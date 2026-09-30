# Agency Voyage

A group-travel booking platform: customers pool together on a trip, and the price per
seat drops in tiers as more people join. A group is confirmed only if it reaches the
trip's minimum participation before its booking deadline - otherwise it's cancelled.

Built with **Spring Boot 4.1 / Java 25** (hexagonal/clean architecture) on the backend,
**Angular 22** (standalone components, signals) on the frontend, and **Kafka** for the
event flow between joining a group and reacting to it.

## Contents

- [Architecture](#architecture)
- [Business rules](#business-rules)
- [Running it locally](#running-it-locally)
- [API](#api)
- [Testing](#testing)
- [CI](#ci)
- [Git workflow](#git-workflow)
- [Simplifications and next steps](#simplifications-and-next-steps)

## Architecture

```
backend/
├── domain/          pure Java, zero framework dependencies
├── application/      use cases + ports, depends only on domain (also framework-free)
├── infrastructure/    adapters: JPA/Postgres, Kafka, scheduler - implements the ports
└── web/               the runnable Spring Boot app: REST controllers, bean wiring
frontend/              Angular app (standalone components, signals)
```

This is hexagonal architecture: `domain` has no dependencies at all. `application`
defines the use cases (`CreateGroupBookingUseCase`, `JoinGroupBookingUseCase`, ...) and
the ports they need (`TripRepository`, `GroupBookingEventPublisher`, ...) but never
imports Spring - its services are plain classes, constructor-injected. `infrastructure`
implements those ports against real technology (Postgres via Spring Data JPA, Kafka via
Spring Kafka). `web` is the only module that wires everything together
(`UseCaseWiringConfig`) and exposes it over HTTP.

**Domain model:**
- `Trip` - a travel package: destination, dates, min/max participants, booking
  deadline, and a `PricingSchedule` (base price + tiers that unlock as the group grows).
- `GroupBooking` - one group pooling on a trip. Captures the trip's min/max/deadline/
  pricing *at the moment it opens*, so later changes to the trip catalog don't
  retroactively affect an in-flight group. Owns the business rules: `join` (rejects a
  full group, a past deadline, or a closed booking) and `finalizeBooking` (confirms if
  the minimum was reached by the deadline, cancels otherwise).

**Event flow:** every join (including the creator's) publishes a
`ParticipantJoinedEvent` to Kafka; every finalization publishes a
`GroupBookingFinalizedEvent`. A `GroupBookingFinalizationScheduler` periodically finds
`OPEN` bookings past their deadline and finalizes them. Two independent consumer groups
read both topics: a `NotificationKafkaListener` that logs them - see
[Simplifications](#simplifications-and-next-steps) for why that isn't its own service -
and a `GroupBookingKafkaBridge` that fans a "this booking changed" ping out over SSE
(`GroupBookingEventBroadcaster`) to every browser tab watching that booking, so
`GroupBookingDetail` updates live instead of waiting for its next poll.

## Business rules

Reframing the kata's original group-purchase stories for trips:

- A customer can create a group booking for a trip, or join an existing one, as long as
  the trip's booking deadline hasn't passed and the group isn't full.
- The price per seat drops as the group crosses each trip's price-tier thresholds -
  visible live to everyone already in the group.
- Once a group's deadline passes: if it reached the trip's minimum participants, the
  trip is confirmed at whatever price tier the final count landed on; otherwise it's
  cancelled.

## Running it locally

Requires Docker, Java 25, and Node 24+.

```bash
# 1. Postgres + Kafka + Kafka UI
docker compose up -d

# 2. Backend (http://localhost:8080) - seeds a few sample trips on first run
cd backend
./mvnw -pl web spring-boot:run

# 3. Frontend (http://localhost:4200)
cd frontend
npm install
npm start
```

Kafka UI is at `http://localhost:8085` if you want to watch the topics
(`group-booking.participant-joined`, `group-booking.finalized`) fill up as you use the app.

The frontend's API base URL is hardcoded to `http://localhost:8080` in
`frontend/src/app/core/api-config.ts` - there's no build-time environment config yet.

## API

| Method | Path                                     | Auth | Description                          |
|--------|-------------------------------------------|------|---------------------------------------|
| POST   | `/api/auth/register`                      | -    | Create an account (`{email, password, displayName}`) → `{token, user}` |
| POST   | `/api/auth/login`                         | -    | Log in (`{email, password}`) → `{token, user}` |
| GET    | `/api/trips`                              | -    | List the trip catalog                 |
| GET    | `/api/trips/{tripId}`                     | -    | Get one trip                          |
| POST   | `/api/trips/{tripId}/group-bookings`      | required | Start a group booking as the caller |
| POST   | `/api/group-bookings/{bookingId}/participants` | required | Join a group booking as the caller |
| DELETE | `/api/group-bookings/{bookingId}/participants/me` | required | Leave a group booking as the caller |
| GET    | `/api/group-bookings/{bookingId}`         | -    | Get a group booking's current state (includes `myParticipantId` if a valid token is sent) |
| GET    | `/api/group-bookings/{bookingId}/events`  | -    | SSE stream: a ping each time the booking changes |

Authenticated requests send `Authorization: Bearer <token>`, a JWT (HS256) returned by
register/login. Its secret and expiration are configured via
`agency-voyage.jwt.secret` / `agency-voyage.jwt.expiration-ms` in `application.yml`
(overridable with the `AGENCY_VOYAGE_JWT_SECRET` env var - the default is a dev-only
value, change it for anything beyond local use).

Errors: `401` for a missing/invalid token on a protected endpoint, `404` for an unknown
trip/booking, `409` for a domain rule violation (group full, deadline passed, already
finalized, already joined, email already registered), `400` for validation failures.

## Testing

Each backend module has fast unit tests (`*Test.java`, run by `mvn test`) and, where
real infrastructure matters, integration tests (`*IT.java`, run by `mvn verify` via
Failsafe) against **Testcontainers** - real Postgres and Kafka, not mocks or H2.

```bash
cd backend
./mvnw test      # fast: domain rules, use-case logic (mocked ports), the scheduler
./mvnw verify     # + integration tests: real Postgres/Kafka via Testcontainers (needs Docker)
```

What's covered where:
- `domain` - pricing-tier resolution, every `GroupBooking` state transition.
- `application` - use-case services against fakes/Mockito for their ports.
- `infrastructure` - JPA repository adapters and the Kafka producer, against real
  Testcontainers Postgres/Kafka.
- `web` - `@WebMvcTest` slices for validation/error-mapping, plus a full
  `@SpringBootTest` that drives the real REST API against Testcontainers Postgres +
  Kafka end to end (create/join across a price tier, persisted state, the Kafka event
  actually landing on the topic).

Frontend:

```bash
cd frontend
npm test          # Vitest, component tests with HttpTestingController
```

## CI

Two GitHub Actions workflows (`.github/workflows/`), each scoped to its own directory
so unrelated changes don't trigger a run:
- `backend-ci.yml` - `./mvnw verify` (unit + integration tests; GitHub-hosted runners
  have Docker, so Testcontainers works without extra setup).
- `frontend-ci.yml` - `ng test` + `ng build`.

## Git workflow

One feature branch per feature, pushed to `origin`. `main` is never pushed to or merged
into by this work - branches are merged in by hand, in order:

`project-scaffold` → `domain-model` → `application-use-cases` → `persistence-postgres`
→ `kafka-events` → `rest-api` → `frontend-trip-catalog` → `frontend-group-booking` →
`ci-pipelines` → `live-price-updates` → `leave-group-booking` → `authentication` →
`ui-carousels`

## Simplifications and next steps

Documented deliberately, not accidentally missed:

- **`NotificationKafkaListener` lives in the same deployable as everything else.** In a
  real system this would be its own service, consuming the same topics to actually
  notify customers. Kept in-process here to demonstrate the event flow without standing
  up a second deployable for an MVP pass.
- **Live updates hold their state in memory, in the one deployable.**
  `GroupBookingEventBroadcaster` keeps its SSE subscribers in a plain in-memory map on
  the `web` instance that received the connection. That's fine for one instance; running
  several behind a load balancer would need either sticky sessions or moving the fan-out
  itself onto Kafka (e.g. each instance's bridge re-publishing to a per-connection
  topic, or a shared pub/sub layer) so a subscriber connected to instance A still hears
  about an event consumed by instance B. `GroupBookingDetail` also keeps a 20s fallback
  poll as a backstop in case an SSE connection drops.
- **Trips are seed data, not admin-managed.** `TripCatalogSeeder` inserts a handful of
  sample trips on first startup; there's no create/edit flow for the catalog itself.
- **Auth is email/password + JWT, no refresh tokens.** `register`/`login` issue a
  single long-lived (24h) JWT; there's no refresh flow or revocation - logging out just
  drops the token client-side. `User` stays a pure identity in `domain` (id, email,
  display name); the password hash lives only in `infrastructure`
  (`UserJpaEntity`/`BCryptPasswordHasher`), never touching the domain or application
  layers. "Which participant is me" is now computed server-side on every response
  (`GroupBookingResponse.myParticipantId`) from the caller's authenticated `UserId`,
  replacing the earlier `localStorage`-based heuristic.
- **Trip photos are stand-in placeholders, not real destination photography.** The
  frontend's `tripPhotoUrls` generates a deterministic picsum.photos set per trip id
  (same trip always gets the same photos) so the carousels have something to show;
  there's no real photo library or upload flow wired up.
- **Further bonus ideas from the original brainstorm** not built here: waitlists once a
  group is full, referral/invite discounts, multi-currency pricing, an
  event-sourced audit trail for group history.
