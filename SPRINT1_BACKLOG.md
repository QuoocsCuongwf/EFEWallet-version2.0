# EFEWallet Sprint 1 Backlog and Execution Board

## Sprint Goal
Deliver a secure, testable MVP transfer flow running in Docker: auth, transfer, idempotency, and baseline QA.

## Sprint Scope
- In scope: AuthService hardening, TransactionService transfer completion, idempotency fixes, security consistency, integration testing, docs alignment.
- Out of scope: API Gateway implementation, RabbitMQ production event flow, advanced monitoring stack.

## Definition of Done
- Code merged and reviewed.
- Unit/integration tests pass in CI or local equivalent.
- Docker startup works for both services and databases.
- API contract matches docs.
- Error handling returns structured responses.

## Epic Backlog (Jira-ready)

| Key | Epic | Task | Priority | Story Points | Acceptance Criteria |
|---|---|---|---|---:|---|
| AUTH-1 | Auth | Login/Register API hardening | P0 | 5 | Input validation, JWT expiry handling, consistent error responses. |
| AUTH-2 | Auth | Seed data/schema alignment | P1 | 3 | `data.sql` matches JPA schema; startup has no SQL init errors. |
| TX-1 | Transaction | Implement transfer service logic | P0 | 8 | Controller calls service; transaction persists; proper response returned. |
| TX-2 | Transaction | Fix DTO/API contract fields | P0 | 3 | Use `amount`, `transactionId`, `createdAt`; response naming consistent. |
| TX-3 | Transaction | Add request validation annotations | P0 | 2 | Invalid payloads rejected with clear validation errors. |
| IDEM-1 | Idempotency | Fix aspect injection and hash logic | P0 | 5 | Dependencies injected; hash compare logic correct; duplicate retries handled. |
| IDEM-2 | Idempotency | Idempotency retry behavior tests | P0 | 3 | Same key+same payload replays cached response; same key+different payload rejected. |
| SEC-1 | Security | JWT authorities mapping in TransactionService | P0 | 3 | Roles mapped into authorities; authorization checks function correctly. |
| SEC-2 | Security | Global exception handling | P0 | 3 | `@RestControllerAdvice` returns consistent error payload and status codes. |
| OPS-1 | DevOps | Docker env/secrets cleanup | P1 | 2 | Sensitive defaults minimized; env docs updated. |
| QA-1 | QA | Integration flow tests | P0 | 8 | Covers auth → transfer → idempotent retry end-to-end. |
| DOC-1 | Docs | README implementation alignment | P1 | 2 | README reflects current endpoints/fields and implemented components only. |

## Dependency Map
- TX-1 depends on: TX-2, TX-3, IDEM-1, SEC-1
- QA-1 depends on: AUTH-1, TX-1, IDEM-2, SEC-2
- DOC-1 depends on: completion of all API-impacting tickets

## Day-by-day Execution Board (10 working days)

### Day 1-2: Foundation
- In Progress: TX-2, TX-3, SEC-1
- Todo: TX-1, IDEM-1, SEC-2, AUTH-1, AUTH-2, IDEM-2, QA-1, OPS-1, DOC-1

### Day 3-4: Core implementation
- In Progress: TX-1, IDEM-1
- Code Review: TX-2, TX-3, SEC-1

### Day 5: Stabilization
- In Progress: SEC-2, AUTH-1
- QA: tickets cleared from code review

### Day 6-7: Verification
- In Progress: QA-1, IDEM-2, AUTH-2

### Day 8: Infrastructure pass
- In Progress: OPS-1

### Day 9: Documentation and polish
- In Progress: DOC-1
- Bugfix lane: defects from QA

### Day 10: Sprint close
- UAT checklist
- Release candidate tag
- Sprint retrospective and carry-over planning

## Kanban Status Template
- Todo
- In Progress
- Code Review
- QA
- Done
- Blocked

## Risk Register (Sprint 1)
- **R1**: Incomplete transaction logic delays QA-1  
  Mitigation: prioritize TX-1 early and keep PRs small.
- **R2**: Idempotency behavior regressions  
  Mitigation: enforce IDEM-2 automated tests before merge.
- **R3**: Contract drift between docs and API  
  Mitigation: keep DOC-1 in same sprint before release.

