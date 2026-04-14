# Hexagonal Architecture (Ports & Adapters) in Spring Boot
Clear and concise guide to **layers** and their **responsibilities**.

---

## Goal
Separate the **business core** from technical details (DB, HTTP, messaging, etc.) by using:
- **Ports:** interfaces defined by the core.
- **Adapters:** concrete implementations that connect the core to the outside world.

---

## Recommended Layers

## 1) Domain (`domain`)
**Responsibility:** model the pure business logic, independent of frameworks.

Includes:
- **Entities** (business rules and invariants)
- **Value Objects**
- **Aggregates**
- **Domain Services** (rules not belonging to a single entity)
- **Domain Events** (if applicable)
- **Repositories as PORTS** (interfaces) when persistence is required by the domain

Must NOT contain:
- Spring annotations (`@Service`, `@Entity`, etc.)
- API DTOs, controllers, JPA entities, HTTP clients, etc.

---

## 2) Application (`application`)
**Responsibility:** orchestrate use cases. Acts as the bridge between domain and infrastructure.

Includes:
- **Use Cases / Application Services** (e.g. `CreateUserUseCase`, `CreateNoteService`)
- **Inbound Ports:** interfaces exposing system capabilities
- **Outbound Ports:** interfaces required by use cases (persistence, email, queues, etc.)
- **Use-case DTOs** (Commands/Queries, Results), if used
- **Transaction boundaries** (often where `@Transactional` lives)

Must NOT contain:
- REST controllers, JPA entities, Spring Data repositories, Feign clients, etc.

---

## 3) Infrastructure (`infrastructure`)
**Responsibility:** implement technical details and connect to external systems.

Includes (adapters):
- **Inbound Adapters**  
  e.g. REST Controllers, GraphQL resolvers, consumers, schedulers
- **Outbound Adapters**  
  e.g. persistence (JPA), integrations (HTTP clients), messaging, email, S3, cache

Also typically includes:
- **Spring configuration** (`@Configuration`, beans)
- **Port implementations** (classes implementing interfaces from `application` or `domain`)
- **Mappers** between models (API ↔ application ↔ domain ↔ persistence)

---

## 4) (Optional) Common / Shared Kernel (`common`, `shared`)
**Responsibility:** shared utilities and cross-cutting contracts.

Includes (with care):
- Generic utilities (dates, IDs, helpers)
- Shared constants and configuration
- Common exceptions and error models (e.g. `ApiError`, `ErrorCode`)
- Reusable abstractions (logging, tracing)

Rule of thumb:
- If it is **business logic**, it does not belong here → `domain`.
- If it belongs to a **specific use case**, it does not belong here → `application`.

---

## Ports & Adapters: practical summary
- **Inbound Port:** what the system offers (use case interface).
- **Inbound Adapter:** who calls it (HTTP controller, consumer, etc.).
- **Outbound Port:** what the system needs (persist, notify, call APIs).
- **Outbound Adapter:** how it is done (JPA, HTTP client, SMTP, etc.).

---

## Allowed dependency direction
- `infrastructure` → depends on `application` and `domain`
- `application` → depends on `domain`
- `domain` → depends on nothing (ideally)

---

## Suggested package structure
- `com.yourapp.domain...`
- `com.yourapp.application...`
- `com.yourapp.infrastructure.in...`  (controllers, consumers)
- `com.yourapp.infrastructure.out...` (jpa, http, messaging)
- `com.yourapp.common...` (optional)

---

## Quick checklist
- Does the domain compile without Spring? ✅
- Do use cases depend on interfaces (ports), not on JPA/HTTP? ✅
- Do controllers only adapt HTTP to a use case? ✅
- Does infrastructure contain all framework-specific code? ✅
