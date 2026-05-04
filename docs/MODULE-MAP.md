# Atalaya — Module Map

The dependency graph between Atalaya's apps and packages. GitHub renders Mermaid natively — view this file on github.com to see the visuals.

## Phase 1 dependency graph

```mermaid
graph TD
    classDef app fill:#1e40af,stroke:#3b82f6,color:#fff
    classDef pkg fill:#065f46,stroke:#10b981,color:#fff
    classDef ui fill:#0e7490,stroke:#06b6d4,color:#fff
    classDef obs fill:#92400e,stroke:#f59e0b,color:#fff
    classDef leaf fill:#7c2d12,stroke:#f59e0b,color:#fff

    Node["apps/node<br/>(camera role)"]:::app

    Onboarding["core-onboarding<br/>(hints + tutorials)"]:::pkg
    Rules["core-rules<br/>(NL rule engine)"]:::pkg
    ML["core-ml<br/>(Gemma inference)"]:::pkg
    Sensors["core-sensors<br/>(sensor interfaces)"]:::pkg
    Observability["core-observability<br/>(CipherWare wrapper)"]:::obs
    UIBase["core-ui-base<br/>(BaseViewModel)"]:::ui
    UITheme["core-ui-theme<br/>(Material 3 theme)"]:::ui
    UIComp["core-ui-components<br/>(shared composables)"]:::ui
    Protocol["core-protocol<br/>(wire types)"]:::leaf

    Node --> Onboarding
    Node --> Rules
    Node --> ML
    Node --> Sensors
    Node --> Observability
    Node --> UIBase
    Node --> UITheme
    Node --> UIComp
    Node --> Protocol

    Rules --> Protocol
    Rules --> ML
    Rules --> Observability

    ML --> Observability

    Onboarding --> Protocol

    Sensors --> Protocol

    UIComp --> UITheme
    UIComp --> Onboarding
    UIComp --> Protocol

    UIBase --> Protocol
```

**Read top-down.** `apps/node` depends on every package. The leaf is `core-protocol`. The `core-ui-*` family forms its own subgraph: components consume theme + onboarding + protocol; theme is a pure leaf within UI. `core-observability` is consumed by everything that emits spans (the watcher in node, inference in ml, evaluation in rules).

## Phase 2 expansion (preview)

```mermaid
graph TD
    classDef app fill:#1e40af,stroke:#3b82f6,color:#fff
    classDef new fill:#7c1d6f,stroke:#d946ef,color:#fff

    Node["apps/node"]:::app
    Hub["apps/hub (NEW)"]:::new
    Web["apps/hub-web (NEW)"]:::new

    P1["Phase 1 packages<br/>(see graph above)"]
    Network["core-network (NEW)<br/>HTTP wrapper"]:::new
    Auth["core-auth (NEW)<br/>sessions, tokens"]:::new
    Middleware["core-middleware (NEW)<br/>interceptors"]:::new
    Transport["core-transport (NEW)<br/>FCM/ntfy/DispatchTransport"]:::new
    Storage["core-storage (NEW)<br/>persistence abstraction"]:::new

    Node --> P1
    Node --> Network
    Node --> Auth
    Node --> Transport

    Hub --> P1
    Hub --> Network
    Hub --> Auth
    Hub --> Middleware
    Hub --> Transport
    Hub --> Storage

    Web --> Network
    Web --> Auth

    Network --> Middleware
    Transport --> Network
```

Phase 2 introduces five new packages — `core-network`, `core-auth`, `core-middleware`, `core-transport` (real `AlertTransport` impls), and `core-storage`. Plus `apps/hub` and `apps/hub-web`. The Phase 1 packages all carry forward unchanged.

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
│   ├── core-observability/          ← CipherWare wrapper
│   │   ├── README.md
│   │   └── src/main/kotlin/
│   ├── core-ui-base/                ← BaseViewModel from Bitwarden
│   │   ├── README.md
│   │   └── src/main/kotlin/
│   ├── core-ui-theme/               ← Material 3 theme
│   │   ├── README.md
│   │   └── src/main/kotlin/
│   └── core-ui-components/          ← shared composables
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
