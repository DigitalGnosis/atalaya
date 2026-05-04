# `packages/core-protocol`

The wire types Atalaya components exchange. The single source of truth for message shapes.

**Phase:** 1 (current)
**Stack:** Kotlin Multiplatform (apps/node consumes; apps/hub will consume from JVM in Phase 2)
**Status:** 🔵 skeleton — spec drafted, no code yet

## What it owns

- `Observation` — a sensor reading from a node (vision in Phase 1; audio/contact/motion later)
- `Alert` — a rule-matched observation
- `Rule` — a user's alert condition with structured envelope (per [ADR-0005](../../docs/phase-1/decisions/ADR-0005-rule-format.md))
- `AlertTransport` — the pluggable interface for delivering alerts (per [ADR-0006](../../docs/phase-1/decisions/ADR-0006-push-transport.md))
- Identifiers (`NodeId`, `RuleId`, `AlertId`)
- Enums (`Role`, `Severity`)

## What it does NOT own

- Transport implementations (FCM, ntfy, NotificationManager — those live in app modules)
- Rule evaluation (that's `core-rules`)
- Inference (that's `core-ml`)
- Persistence (that's per-app)

## Dependencies

| On | Why |
|----|-----|
| `kotlinx.serialization` | Cross-platform JSON for protocol payloads |
| `kotlinx.datetime` | `Instant` type that serializes consistently |

**Nothing else.** This is the leaf module — it imports from no other Atalaya module. Portability is the point.

## Specs

- Module spec with code examples: [`../../docs/phase-1/modules/EXAMPLE-core-protocol.md`](../../docs/phase-1/modules/EXAMPLE-core-protocol.md)

## Consumers

| Module | Use |
|--------|-----|
| [`apps/node`](../../apps/node/) | Builds `VisionObservation`, builds `Alert`, calls `AlertTransport.send` |
| [`packages/core-rules`](../core-rules/) | Consumes `Observation` to evaluate against `Rule`s |
| `apps/hub` (Phase 2) | Receives serialized `Observation` and `Alert` from nodes |
| `apps/control` (Phase 4) | Receives `Alert` for display |

## Source layout (planned)

```
packages/core-protocol/
├── README.md
├── build.gradle.kts          # Phase 1 Step 2
└── src/commonMain/kotlin/dev/digitalgnosis/atalaya/protocol/
    ├── Observation.kt
    ├── Alert.kt
    ├── Rule.kt
    ├── AlertTransport.kt
    ├── Ids.kt
    └── Severity.kt
```
