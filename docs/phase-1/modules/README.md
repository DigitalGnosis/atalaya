# Phase 1 — Module Specs

A module spec describes:

- What the module is responsible for
- Its public API shape (interfaces, types)
- Its boundaries (what depends on it, what it depends on)
- A code example showing the planned shape — illustrative, not the final implementation

Real implementation lives under `apps/` and `packages/`. Specs in this directory keep the design portable across phases — Phase 2 might extend a spec, Phase 3 might reuse it.

## Index

| Module | Spec | Lives in (target) | Status |
|--------|------|-------------------|--------|
| `core-protocol` (wire types) | [`EXAMPLE-core-protocol.md`](EXAMPLE-core-protocol.md) | `packages/core-protocol/` | example shape |
| `core-onboarding` (hints + tutorials) | [`core-onboarding.md`](core-onboarding.md) | `packages/core-onboarding/` | drafted |
| `core-rules` | _(stub — opens during Step 2)_ | `packages/core-rules/` | pending |
| `core-ml` | _(stub — opens during Step 2)_ | `packages/core-ml/` | pending |
| `core-sensors` | _(stub — opens during Step 2)_ | `packages/core-sensors/` | pending |
| `apps/node` (camera role) | _(stub — opens during Step 2)_ | `apps/node/` | pending |

## Template

See [`TEMPLATE.md`](TEMPLATE.md). Copy when writing a new module spec.
