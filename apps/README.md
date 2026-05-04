# Atalaya Apps

User-facing applications. Each app lives in its own subdirectory.

## Planned

| App | Phase | Platform | Description |
|-----|-------|----------|-------------|
| `node` | 1 | Android (Kotlin) | The watcher daemon. Configurable role(s). Local Gemma 4 inference for camera/audio nodes. Pushes events to the hub. |
| `hub` | 2 | Server (Docker) | Coordinator. Orchestrates rules across nodes, persists events, runs heavier AI for nodes that can't. |
| `control` | 4 | Android (Kotlin), later iOS | Homeowner-facing app. Arm/disarm, live view, history, rule config. |
| `web` | 2 | Web (Compose Multiplatform or Svelte) | Hub admin dashboard. Node status, recent events, rule config. |

Apps that don't exist yet are placeholders. See [`../docs/PRD-PHASES.md`](../docs/PRD-PHASES.md) for the per-phase build plan.
