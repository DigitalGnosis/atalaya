# Atalaya Apps

User-facing applications. Each app lives in its own subdirectory.

## Apps

| App | Phase | Platform | Status | Folder |
|-----|-------|----------|--------|--------|
| `node` | 1 | Android (Kotlin) | 🔵 skeleton | [`node/`](node/) |
| `hub` | 2 | Server (Docker) | ⚪ pending | _(future)_ |
| `control` | 4 | Android (Kotlin), later iOS | ⚪ pending | _(future)_ |
| `web` | 2 | Web (Svelte or Compose Multiplatform) | ⚪ pending | _(future)_ |

## What each app does

- **`node`** — the watcher daemon. Configurable role(s). Local Gemma 4 inference for camera/audio nodes. In Phase 1 ships with the camera role only.
- **`hub`** — Phase 2. Coordinator that orchestrates rules across nodes, persists events, runs heavier AI for nodes that can't.
- **`control`** — Phase 4. Homeowner-facing app. Arm/disarm, live view, history, rule config.
- **`web`** — Phase 2. Hub admin dashboard. Node status, recent events, rule config.

See [`../docs/PRD-PHASES.md`](../docs/PRD-PHASES.md) for the per-phase build plan and [`../docs/README.md`](../docs/README.md) for navigation.

## Module dependency rules

- Apps may depend on packages in `../packages/`.
- Apps must NEVER import from each other directly. Cross-app communication goes through `packages/core-protocol` only (per ADR-0001).
