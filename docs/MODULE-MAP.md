# Atalaya — Module Map

The dependency graph between Atalaya's apps and packages. GitHub renders Mermaid natively — view this file on github.com to see the visuals.

## Phase 1 dependency graph

```mermaid
graph TD
    classDef app fill:#1e40af,stroke:#3b82f6,color:#fff
    classDef pkg fill:#065f46,stroke:#10b981,color:#fff
    classDef leaf fill:#7c2d12,stroke:#f59e0b,color:#fff

    Node["apps/node<br/>(camera role)"]:::app

    Onboarding["packages/core-onboarding<br/>(hints + tutorials)"]:::pkg
    Rules["packages/core-rules<br/>(NL rule engine)"]:::pkg
    ML["packages/core-ml<br/>(Gemma inference)"]:::pkg
    Sensors["packages/core-sensors<br/>(sensor interfaces)"]:::pkg
    UIBase["packages/core-ui-base<br/>(BaseViewModel)"]:::pkg
    Protocol["packages/core-protocol<br/>(wire types)"]:::leaf

    Node --> Onboarding
    Node --> Rules
    Node --> ML
    Node --> Sensors
    Node --> UIBase
    Node --> Protocol

    Rules --> Protocol
    Rules --> ML

    Onboarding --> Protocol
    Sensors --> Protocol
    UIBase --> Protocol
```

**Read top-down.** `apps/node` depends on every package. The packages depend only on `core-protocol` (the leaf), and `core-rules` additionally depends on `core-ml` because rule evaluation calls into the LLM judge.

## Phase 2 expansion (preview)

```mermaid
graph TD
    classDef app fill:#1e40af,stroke:#3b82f6,color:#fff
    classDef pkg fill:#065f46,stroke:#10b981,color:#fff
    classDef leaf fill:#7c2d12,stroke:#f59e0b,color:#fff
    classDef new fill:#7c1d6f,stroke:#d946ef,color:#fff

    Node["apps/node"]:::app
    Hub["apps/hub (NEW)"]:::new
    Web["apps/hub-web (NEW)"]:::new

    Onboarding["packages/core-onboarding"]:::pkg
    Rules["packages/core-rules"]:::pkg
    ML["packages/core-ml"]:::pkg
    Sensors["packages/core-sensors"]:::pkg
    UIBase["packages/core-ui-base"]:::pkg
    Protocol["packages/core-protocol"]:::leaf
    Transport["packages/core-transport (NEW)"]:::new

    Node --> Onboarding
    Node --> Rules
    Node --> ML
    Node --> Sensors
    Node --> UIBase
    Node --> Transport
    Node --> Protocol

    Hub --> Rules
    Hub --> ML
    Hub --> Transport
    Hub --> Protocol

    Web --> Protocol

    Transport --> Protocol
    Rules --> Protocol
    Rules --> ML
    Onboarding --> Protocol
    Sensors --> Protocol
    UIBase --> Protocol
```

Phase 2 introduces `apps/hub`, `apps/hub-web`, and a new `packages/core-transport` for FCM / ntfy / DispatchTransport implementations of the `AlertTransport` interface.

## Folder layout (current state on disk)

```
atalaya/
├── apps/
│   ├── README.md
│   └── node/                         ← Phase 1 — camera role
│       ├── README.md
│       └── src/main/kotlin/         (skeleton — code lands in roadmap Step 3+)
│
├── packages/
│   ├── README.md
│   ├── core-protocol/               ← wire types (the leaf)
│   │   ├── README.md
│   │   └── src/main/kotlin/
│   ├── core-onboarding/             ← hints + tutorials
│   │   ├── README.md
│   │   └── src/main/kotlin/
│   ├── core-rules/                  ← NL rule engine
│   │   ├── README.md
│   │   └── src/main/kotlin/
│   ├── core-ml/                     ← Gemma inference
│   │   ├── README.md
│   │   └── src/main/kotlin/
│   ├── core-sensors/                ← sensor interfaces
│   │   ├── README.md
│   │   └── src/main/kotlin/
│   └── core-ui-base/                ← BaseViewModel from Bitwarden
│       ├── README.md
│       └── src/main/kotlin/
│
├── infra/
│   └── README.md                    ← (Phase 2+ — hub deployment)
│
└── docs/
    ├── README.md
    ├── ARCHITECTURE.md
    ├── MODULE-MAP.md                ← (this file)
    ├── PRD-PHASES.md
    ├── framework/
    │   └── PHASE-TEMPLATE/
    └── phase-1/
        ├── README.md
        ├── 01-goal.md
        ├── 02-roadmap.md
        ├── decisions/
        ├── modules/
        ├── references/
        └── research/
```

## Dependency rules (per ADR-0001)

1. Apps depend on packages.
2. Packages may depend on other packages — but the graph stays acyclic.
3. Packages NEVER depend on apps.
4. Apps NEVER depend on each other directly. Cross-app comms go through `core-protocol`.
5. `core-protocol` is the leaf. It depends on nothing internal.

## How to read the Mermaid graphs

- **Blue boxes** = apps (deliverables).
- **Green boxes** = packages (shared modules).
- **Orange box** = `core-protocol`, the leaf.
- **Purple boxes** = items added in Phase 2 (preview only).
- Arrows point from dependent to dependency. `A → B` means "A depends on B."

## Adding a new module

1. Decide: app or package? Apps are deliverables (have a UI or run as a server). Packages are shared logic with no user-facing surface.
2. If app: create `apps/<name>/` with README + `src/`.
3. If package: create `packages/<name>/` with README + `src/`.
4. Add the row to [`apps/README.md`](../apps/README.md) or [`packages/README.md`](../packages/README.md).
5. Update this `MODULE-MAP.md` graph.
6. Open an ADR in the relevant phase if the new module changes the dependency rules.
