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
`OPEN` bookings past their deadline and finalizes them. A `NotificationKafkaListener`
consumes both topics and logs them - see
[Simplifications](#simplifications-and-next-steps) for why that isn't its own service.

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

| Method | Path                                     | Description                          |
|--------|-------------------------------------------|---------------------------------------|
| GET    | `/api/trips`                              | List the trip catalog                 |
| GET    | `/api/trips/{tripId}`                     | Get one trip                          |
| POST   | `/api/trips/{tripId}/group-bookings`      | Start a group booking (`{"customerName"}`) |
| POST   | `/api/group-bookings/{bookingId}/participants` | Join a group booking (`{"customerName"}`) |
| GET    | `/api/group-bookings/{bookingId}`         | Get a group booking's current state   |

Errors: `404` for an unknown trip/booking, `409` for a domain rule violation (group
full, deadline passed, already finalized), `400` for validation failures.

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
`ci-pipelines`

## Simplifications and next steps

Documented deliberately, not accidentally missed:

- **`NotificationKafkaListener` lives in the same deployable as everything else.** In a
  real system this would be its own service, consuming the same topics to actually
  notify customers. Kept in-process here to demonstrate the event flow without standing
  up a second deployable for an MVP pass.
- **The frontend polls, it doesn't get pushed to.** `GroupBookingDetail` re-fetches
  every 4 seconds. A Kafka → WebSocket/SSE bridge so the UI updates instantly would be
  the natural next step (and was part of the original brainstorm).
- **Trips are seed data, not admin-managed.** `TripCatalogSeeder` inserts a handful of
  sample trips on first startup; there's no create/edit flow for the catalog itself.
- **No auth.** Anyone can create or join a group with any name they type in.
- **Further bonus ideas from the original brainstorm** not built here: waitlists once a
  group is full, referral/invite discounts, multi-currency pricing, an
  event-sourced audit trail for group history.
