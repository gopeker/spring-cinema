# Plan: Spring Cinema Web Application

## Overview
A cinema web application for presenting movies and selling tickets online. Users can browse movies, view screenings, select seats, and purchase tickets. Includes admin panel for managing content.

## Tech Stack
- **Backend:** Spring Boot 4 (latest), Spring Security + JWT, Spring Data JPA, PostgreSQL
- **Frontend:** Angular 21 (Angular CLI), Bootstrap 5.3+, Vite build
- **Database:** PostgreSQL (3 showrooms: 100, 150, 200 seats)
- **Docker:** compose.yaml (dev via spring-boot-docker-compose), docker-compose.yml (prod), docker-compose-native.yml (prod native), Dockerfile (JVM), Dockerfile-native (GraalVM)
- **Authentication:** JWT tokens (stateless), role-based access (USER / ADMIN)

## Domain Model

| Entity | Fields | Notes |
|--------|--------|-------|
| User | id, name, email, password (BCrypt), role | Roles: USER, ADMIN |
| Movie | id, title, description, duration (min), posterUrl | Hard-coded / seeded |
| Showroom | id, name, rows, seatsPerRow | Computed capacity; seat grid = rows x seatsPerRow |
| Screening | id, movie (FK), showroom (FK), startTime (LocalDateTime), basePrice | Seeded |
| Ticket | id, screening (FK), user (FK), seatRow, seatNumber, price, status, purchaseTime | Status: RESERVED, CONFIRMED, CANCELLED |

## Backend API Design

### Authentication
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/auth/register | - | Register user (name, email, password) |
| POST | /api/auth/login | - | Login -> JWT access token (1h) |

### Movies
| GET | /api/movies | - | List all movies |
| GET | /api/movies/{id} | - | Movie detail |

### Showrooms
| GET | /api/showrooms | - | List all showrooms with capacity |

### Screenings
| GET | /api/screenings | - | Filter by movieId / date (query params) |
| GET | /api/screenings/{id} | - | Screening detail |
| GET | /api/screenings/{id}/seats | - | Seat availability matrix |

### Tickets
| POST | /api/tickets | USER | Purchase ticket |
| GET | /api/tickets/me | USER | Current user's tickets |
| DELETE | /api/tickets/{id} | USER | Cancel own ticket |

### Admin
| POST | /api/admin/movies | ADMIN | Create movie |
| PUT | /api/admin/movies/{id} | ADMIN | Update movie |
| DELETE | /api/admin/movies/{id} | ADMIN | Delete movie |
| POST | /api/admin/screenings | ADMIN | Create screening |
| PUT | /api/admin/screenings/{id} | ADMIN | Update screening |
| DELETE | /api/admin/screenings/{id} | ADMIN | Delete screening |

## Data Seeding (DataSeeder)

On every startup, if DB is empty, seed:
- 3 Showrooms: Saal 1 (100 seats, 10x10), Saal 2 (150 seats, 10x15), Saal 3 (200 seats, 10x20)
- 5-8 Sample Movies: varied titles, descriptions, durations (90-150 min)
- Screenings: next 7 days, multiple screenings per day across all showrooms, prices $8-$15

## Frontend Structure (Angular 21)

```
frontend/
├── src/app/
│   ├── core/
│   │   ├── services/        # auth, movie, screening, ticket services
│   │   ├── guards/          # auth.guard, admin.guard
│   │   └── interceptors/   # auth.interceptor (JWT header)
│   ├── shared/
│   │   └── components/      # navbar, seat-grid, ticket-card
│   ├── features/
│   │   ├── home/             # movie list, upcoming screenings
│   │   ├── movie-detail/     # movie info + screenings
│   │   ├── seat-selection/   # seat grid, price calculation
│   │   ├── checkout/         # purchase confirmation
│   │   ├── my-tickets/       # user's purchased tickets
│   │   ├── auth/             # login + register forms
│   │   └── admin/            # movie & screening CRUD
│   ├── app.routes.ts
│   └── app.config.ts
└── package.json
```

### Pages & Routes
| Path | Component | Description |
|------|-----------|-------------|
| / | HomeComponent | Featured movies, upcoming screenings |
| /movies/:id | MovieDetailComponent | Movie info + screenings |
| /screenings/:id/seats | SeatSelectionComponent | Visual seat grid |
| /checkout | CheckoutComponent | Confirm purchase |
| /tickets | MyTicketsComponent | User's tickets |
| /login | LoginComponent | Login form |
| /register | RegisterComponent | Registration form |
| /admin/movies | AdminMoviesComponent | Movie CRUD |
| /admin/screenings | AdminScreeningsComponent | Screening CRUD |

### Seat Grid UX
- 2D seat grid rendered per showroom (rows x seatsPerRow)
- Colors: green (available), red (taken), gray (blocked/disabled)
- Click available seat -> highlights selection, shows price
- Multi-seat selection (up to 8 per transaction)
- Price tiers: front rows (premium +$3), standard, back rows (discount -$2)

## Security
- JWT: HS256, 1-hour expiry, stored in localStorage
- Passwords: BCrypt (strength 12)
- CORS: Allow Angular dev server (4200) + prod static origin
- Role-based: @PreAuthorize on admin endpoints
- Stateless: No session, no CSRF (REST-only API)

## Configuration
- .env / .env.sample for SPRING_DATASOURCE_*, APP_JWT_SECRET
- application.properties: datasource, JPA/Hibernate, JWT secret, CORS
- No YAML -- use .properties only

## Dotfiles & Project Setup
- .gitignore (Java + Angular + node_modules + .env)
- .env.sample (placeholder values)
- .editorconfig (Java: 4 spaces, JS/TS: 2 spaces)
- .gitattributes (LF normalization)
- .dockerignore
- .vscode/ (recommended extensions + settings)
- .devcontainer/ (Java 25 + Node 24 + PostgreSQL)

## Containerized Development & Deployment

### 1. Development: spring-boot-docker-compose
- compose.yaml in project root (managed by Spring Boot automatically)
- PostgreSQL 18 Alpine container, port 5432
- Credentials from .env / env vars (SPRING_DATASOURCE_*)
- spring-boot-docker-compose starts/stops PostgreSQL automatically on `./mvnw spring-boot:run`
- No manual `docker compose up` needed

### 2. Production: docker-compose.yml (Full Stack)
- PostgreSQL 18 Alpine container (port 5432, pg_isready healthcheck)
- Spring Boot app container (JVM, Dockerfile, port 8080, curl healthcheck via /actuator/health)
- Both on spring-network bridge; named volumes for data persistence
- Credentials injected via env vars: SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/springcinema

### 3. Production: docker-compose-native.yml (Native Image + PostgreSQL)
- Same PostgreSQL setup
- Spring Boot app container (Dockerfile-native, GraalVM 25 native image)
- Healthcheck via curl against /actuator/health
- Much faster startup (~100ms vs several seconds), lower memory footprint

### 4. Dockerfiles
- Dockerfile: Multi-stage build (eclipse-temurin:25-jdk-jammy → eclipse-temurin:25-jre-alpine), non-root user (springboot, UID 1001), curl healthcheck, JVM flags for containers
- Dockerfile-native: Multi-stage build (ghcr.io/graalvm/graalvm-community:25 → debian:12-slim with curl), non-root user, native executable, ~100ms startup

### 5. .dockerignore
- Exclude: .git/, node_modules/, target/, frontend/dist/, frontend/.vite/, .env, .vscode/, .idea, coverage, reports, *.log

### 6. Local Dev Workflow
```
# PostgreSQL starts automatically with spring-boot-docker-compose
./mvnw spring-boot:run

# Angular dev server (separate terminal)
cd frontend && npm start   # port 4200

# Full stack via Docker Compose (no dev server):
docker compose -f docker-compose.yml up -d

# Stop everything:
docker compose -f docker-compose.yml down
```

### 7. Production Deployment
```
# Build JVM image
docker build -t spring-cinema .

# Run with docker-compose (app + DB)
docker compose -f docker-compose.yml up -d

# Or native image (much faster startup)
docker build -f Dockerfile-native -t spring-cinema-native .
docker compose -f docker-compose-native.yml up -d
```

## Implementation Steps

### Step 1: Generate Spring Boot Project
- Run node scripts/create-project-latest.mjs spring-cinema com.cinema spring-cinema com.cinema.springcinema 21 fullstack
- Add Spring Security + JWT dependencies to pom.xml (jjwt-api, jjwt-impl, jjwt-jackson)
- Configure application.properties with datasource, JPA, JWT secret, CORS
- Add .env.sample

### Step 2: Backend Entities & Repositories
- User entity + UserRepository
- Movie entity + MovieRepository
- Showroom entity + ShowroomRepository
- Screening entity + ScreeningRepository
- Ticket entity + TicketRepository

### Step 3: Security Layer
- SecurityConfig (stateless JWT filter, CORS, role-based rules)
- JwtService (token generation + validation)
- JwtAuthFilter (servlet filter)
- AuthController (/auth/register, /auth/login)
- Unit tests for JwtService

### Step 4: Business Logic
- AuthService (register, login)
- MovieService, ShowroomService, ScreeningService
- TicketService (purchase, cancel, seat availability)
- DataSeeder (seed showrooms, movies, screenings)
- Controller for each entity
- @Transactional on purchase endpoint

### Step 5: Admin Endpoints
- AdminController (movies CRUD, screenings CRUD)
- Admin tests with @MockitoBean

### Step 6: Docker & Containerized Setup
- Add compose.yaml (dev, spring-boot-docker-compose, PostgreSQL 18 Alpine, credentials from .env, 512m memory limit, healthcheck)
- Add docker-compose.yml (prod: app + PostgreSQL, spring-network bridge, named volumes, healthchecks, restart unless-stopped)
- Add docker-compose-native.yml (prod: native + PostgreSQL, same structure, native healthcheck)
- Add Dockerfile (JVM multi-stage: Temurin 25 JDK → JRE Alpine, non-root user, curl healthcheck, optimized JVM flags)
- Add Dockerfile-native (GraalVM 25 native multi-stage: graalvm-community:25 → debian:12-slim, native executable)
- Add .dockerignore (standard exclusions)
- Add PostgreSQLContainer integration to test config (@ServiceConnection, TestContainers 2.x, org.testcontainers.postgresql package)

### Step 7: Angular Frontend
- echo | npx --yes @angular/cli@21 new frontend --style=css --ssr=false --skip-git --defaults --skip-install
- cd frontend && npm install (Bootstrap, Bootstrap Icons)
- Configure Angular routing, HTTP client, auth interceptor
- Use modern Angular features (standalone components, signals, inject())
- Build AuthService + auth guards
- Build MovieService, ScreeningService, TicketService
- Build all page components (home, movie-detail, seat-selection, checkout, my-tickets, auth, admin)
- Shared components: navbar, seat-grid, ticket-card
- Run ng build -> output to src/main/resources/static
- Integrate frontend build into Maven lifecycle

### Step 8: Dotfiles & Project Polish
- .gitignore (merge with skill template)
- .editorconfig, .gitattributes, .dockerignore
- .vscode/extensions.json, .vscode/settings.json
- .devcontainer/ (Java 25 + Node 24 + PostgreSQL)

### Step 9: Validation
- ./mvnw clean install -- builds without errors
- ./mvnw test -- unit tests pass
- ./mvnw verify -- integration tests pass (TestContainers)
- cd frontend && npm run start -- Angular dev server starts on port 4200
- Smoke-test: register, login, browse movies, check seats, purchase ticket

### Step 10: Docker Validation
- docker build -t spring-cinema . -- builds without errors
- docker compose -f docker-compose.yml up -d -- starts app + DB successfully
- docker compose -f docker-compose.yml logs spring-app -- app logs show StartupInfoListener banner
- docker compose -f docker-compose.yml down
- docker build -f Dockerfile-native -t spring-cinema-native . -- native image builds (optional, skip if long)
- docker compose -f docker-compose-native.yml up -d -- native app + DB (optional)

## Key Design Decisions
1. Hard-coded seed data -- seeded via DataSeeder on startup, no admin UI for movies/showrooms
2. Stateless JWT -- no sessions, no CSRF, no refresh tokens (1h expiry)
3. Hibernate ddl-auto=update -- schema managed by entities; no Flyway/Liquibase
4. Angular SPA -- served as static assets by Spring Boot in production
5. No Lombok -- use records and explicit code per skill rules
6. Properties over YAML -- all config in .properties files
7. Angular default port 4200
8. Containerized from day one — compose.yaml for dev (automatic via spring-boot-docker-compose), docker-compose.yml for prod (app + PostgreSQL), both Dockerfiles (JVM + GraalVM native) ready for CI/CD

(End of file)
