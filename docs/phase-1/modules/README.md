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
| `core-rules` | _(stub — opens after M5 research)_ | `packages/core-rules/` | pending |
| `core-ml` | _(stub — opens after M4 research)_ | `packages/core-ml/` | pending |
| `core-sensors` | _(stub — opens after M3 research)_ | `packages/core-sensors/` | pending |
| `apps/node` (camera role) | _(stub — opens after M2/M3/M4 land)_ | `apps/node/` | pending |

## Template

See [`TEMPLATE.md`](TEMPLATE.md). Copy when writing a new module spec.
