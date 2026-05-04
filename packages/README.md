# Atalaya Packages

Shared modules consumed by multiple apps. Each package is independently versionable inside the monorepo.

## Planned

| Package | Phase | Description |
|---------|-------|-------------|
| `core-protocol` | 1 | Wire types shared between node, hub, and control. The single source of truth for message shapes. |
| `core-rules` | 1 | Natural-language rule engine. Takes a description from a sensor and a rule set, decides whether to alert. |
| `core-ml` | 1 | Inference abstraction. Pluggable backends: llama.cpp Android, MediaPipe LLM, server-hosted endpoint. |
| `core-sensors` | 1 | Sensor abstraction interfaces. Implementations live in role-specific app modules. |

Packages that don't exist yet are placeholders. See [`../docs/PRD-PHASES.md`](../docs/PRD-PHASES.md).
