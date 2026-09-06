# Voucher Manager

A voucher/coupon management system with multi-tenant orgs, admin oversight, and a
dashboard frontend. Orgs manage their own users and vouchers; vouchers can be
freely redeemable by code or restricted to specific pre-assigned users.

## Features

- **Two login types**: Admin and Org, JWT-based.
- **Multi-tenancy**: users and vouchers belong to an org; orgs only see their own
  data, admins can act across all orgs.
- **Voucher lifecycle**: create, force-expire, redeem, list.
- **Voucher scoping**: `FREE` vouchers can be redeemed by anyone with the code;
  `USER_SPECIFIC` vouchers must be linked (assigned) to a user first, and that
  link can be revoked.
- **Dashboard**: stat cards, a voucher-status breakdown chart, a 30-day
  redemption trend chart, a per-org breakdown (admin), and a recent-activity
  feed.

## Tech stack

| Layer    | Stack |
|----------|-------|
| Backend  | Java 17+, Spring Boot 4.1 (Web, Data JDBC, Validation), PostgreSQL |
| Auth     | Hand-rolled JWT (`jjwt`) + BCrypt (`spring-security-crypto`) — no Spring Security filter chain |
| Frontend | Angular 22 (standalone components, signals, zoneless change detection) |

## Project structure

```
voucher-manager/
├── src/main/java/abhinav/projects/vouchermanager/
│   ├── auth/       # Admin/Org login, JWT issue+verify, CurrentPrincipal (org-scoping/authorization)
│   ├── user/       # Users (org-scoped)
│   ├── voucher/    # Vouchers, redemption, assign/revoke, history
│   ├── stats/       # Stat cards, activity feed, daily redemption trend
│   └── error/       # ProblemDetail-based exception handling
├── src/main/resources/
│   ├── application.yaml
│   └── schema.sql   # Full DROP/CREATE on every boot — no migrations, no persisted data across restarts
├── docker-compose.yml   # Postgres for local dev
└── frontend/
    └── src/app/
        ├── auth/        # Login page, token storage, route guards, HTTP interceptor
        ├── orgs/        # Admin: create/list orgs, org-context selector
        ├── users/       # Users list/create
        ├── vouchers/    # Vouchers list/create/detail (redeem, link, revoke, force-expire)
        ├── overview/    # Dashboard landing page
        ├── stats/       # Stats API client + redemption trend chart
        └── charts/      # Reusable bar chart
```

## Getting started

### Prerequisites

- Java 17+
- Node.js 20+ and npm
- Docker (for local Postgres)

### 1. Database

```bash
docker compose up -d postgres
```

Starts Postgres on `localhost:5432` with database/user/password `voucher_manager`
(see `docker-compose.yml`).

### 2. Backend

```bash
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`. `schema.sql` drops and recreates every table on
each boot (`spring.sql.init.mode: always`) — there is no persisted data across
restarts, and no migration tool (Flyway/Liquibase) is used. A dev admin account
is seeded automatically on every startup (see **Dev credentials** below).

### 3. Frontend

```bash
cd frontend
npm install
ng serve
```

Runs on `http://localhost:4200`. `frontend/proxy.conf.json` forwards API calls
(`/users`, `/vouchers`, `/auth`, `/orgs`, `/stats`) to the backend on `:8080`, so
the Angular dev server needs no CORS configuration on the backend.

> **Note**: every new top-level backend route needs a matching entry added to
> `proxy.conf.json`, or it won't be reachable from the dev server.

### Dev credentials

The backend seeds one admin account on every boot (`auth/AdminSeeder.java`):

```
email:    admin@example.com
password: admin1234
```

There is no self-signup. Org accounts must be created by an admin (log in as
the seeded admin, then use the **Orgs** page or `POST /orgs`), then that org can
log in via `/auth/org/login`.

## API overview

All endpoints except `/auth/*` and `POST /vouchers/{code}/redeem` require an
`Authorization: Bearer <token>` header.

| Method | Path | Notes |
|---|---|---|
| POST | `/auth/admin/login` | |
| POST | `/auth/org/login` | |
| POST | `/orgs` | admin-only, creates an org account |
| GET | `/orgs` | admin-only, lists all orgs |
| POST | `/users` | org creates its own users; admin must pass `orgId` |
| GET | `/users/{id}` | |
| GET | `/users?orgId=` | admin omits `orgId` to see all orgs |
| POST | `/vouchers` | optional `assignedUserIds` to link users at creation time |
| GET | `/vouchers/{id}` | |
| GET | `/vouchers?orgId=` | |
| POST | `/vouchers/{code}/redeem` | **public, unauthenticated** — end-user redemption by code |
| POST | `/vouchers/{id}/force-expire` | |
| POST | `/vouchers/{id}/assign` | link a `USER_SPECIFIC` voucher to a user |
| POST | `/vouchers/{id}/revoke` | revoke a single user's assignment |
| GET | `/vouchers/{id}/assignments` | |
| GET | `/stats/vouchers?orgId=` | counts by status, users, redemptions |
| GET | `/stats/vouchers/by-org` | admin-only, per-org breakdown |
| GET | `/stats/activity?orgId=&limit=` | recent voucher history, hydrated with voucher code + user email |
| GET | `/stats/redemptions/daily?orgId=&days=` | zero-filled daily counts for the trend chart |

## Domain model notes

- **Org scoping**: `User.orgId` and `Voucher.orgId` are non-null. `CurrentPrincipal`
  (in `auth/`) resolves the effective org for every request — an org's own token
  always wins; an admin must supply `orgId` explicitly for writes, and may omit
  it on reads to see everything.
- **Voucher scope**: `FREE` (default) vs `USER_SPECIFIC`. Redeeming a
  `USER_SPECIFIC` voucher requires a prior, non-revoked `assign` mapping for that
  exact user.
- **Redemption stays public** by design — it's meant for end-users with a code,
  not org/admin staff, so it does not require a login.
