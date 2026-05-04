---
adr: 0001
title: Monorepo layout — Now in Android style with apps and packages separation
status: accepted
date: 2026-05-04
---

# ADR-0001 — Monorepo layout

## Context

Atalaya ships multiple deliverables: Android node app (Phase 1), Docker hub (Phase 2), web admin (Phase 2), Android control app (Phase 4), iOS control app (Phase 4b), various role apps (Phase 3). All of them share contracts (the wire protocol between node and hub) and benefit from being released atomically when the protocol changes.

We chose monorepo over polyrepo in Phase 0 (see Phase 0 commit). This ADR fixes the **layout** within the monorepo.

## Decision

We will adopt a layout inspired by Google's [Now in Android](https://github.com/android/nowinandroid) project, adapted for our cross-platform needs:

```
atalaya/
├── apps/                      # User-facing deliverables
│   ├── node/                  # Android node app (Phase 1)
│   ├── hub/                   # Server hub (Phase 2)
│   ├── control/               # Homeowner Android app (Phase 4)
│   └── web/                   # Hub admin web UI (Phase 2)
├── packages/                  # Shared modules consumed by multiple apps
│   ├── core-protocol/         # Wire types
│   ├── core-rules/            # NL rule engine
│   ├── core-ml/               # Inference abstraction
│   └── core-sensors/          # Sensor interface contracts
├── infra/                     # Self-host infrastructure (compose, k8s)
└── docs/                      # This documentation tree
```

Each app and each package is a self-contained Gradle module (or appropriate equivalent for the hub/web stacks). Apps depend on packages; packages do not depend on apps. Packages may depend on other packages but should declare it explicitly.

## Alternatives considered

- **Now in Android style with `:core:*` and `:feature:*` instead of `apps/` + `packages/`.** Pros: closely mirrors Google's reference. Cons: doesn't fit cross-platform (hub is not Android). Rejected because Atalaya isn't Android-only.
- **Flat layout (everything top-level).** Pros: fewer directories. Cons: doesn't scale past Phase 2; loses the boundary between deliverables and shared code. Rejected.
- **Per-platform top-level dirs (`android/`, `server/`, `web/`).** Pros: clear platform boundaries. Cons: shared protocol code becomes ambiguous — does it live in `android/`? `server/`? Rejected because shared modules deserve their own home.

## Consequences

**Easier:**
- Adding a new app: `apps/<name>/` with its own build config.
- Adding a shared module: `packages/<name>/` with a clear consumption pattern.
- Cross-language sharing: `packages/core-protocol/` can house both Kotlin types (for apps/node, apps/control) and Go/Python types (for apps/hub) under one module umbrella.
- Atomic protocol changes: one PR touches `packages/core-protocol/` plus every consuming app.

**Harder:**
- A change to a shared package can ripple into multiple apps; CI must run all dependents.
- Polyglot tooling: we'll have Gradle for Android, plus another build tool for the hub (Go's `go build`? Python's `uv`?). Each app picks its own.
- Onboarding: contributors need to learn the layout. Mitigation: clear README in each `apps/` and `packages/` subdir.

**New constraints:**
- Apps must NEVER import each other's internals. Cross-app communication goes through `packages/core-protocol/` only.
- Packages must NOT depend on apps.
- Each module owns its own version pinning where applicable.

## Related

- Supersedes: none
- Superseded by: none
- Informed by: [Now in Android](https://github.com/android/nowinandroid) project structure, [Supabase monorepo](https://github.com/supabase/supabase) layout
- Triggers: ADR-0008 (DI framework — informs the apps/node skeleton), Step 2 of [`../02-roadmap.md`](../02-roadmap.md)
