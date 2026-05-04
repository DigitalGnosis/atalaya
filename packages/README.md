# Atalaya Packages

Shared modules consumed by multiple apps. Each package is independently versionable inside the monorepo.

## Packages

| Package | Phase | Status | Folder |
|---------|-------|--------|--------|
| `core-protocol` | 1 | 🔵 skeleton | [`core-protocol/`](core-protocol/) |
| `core-onboarding` | 1 | 🔵 skeleton | [`core-onboarding/`](core-onboarding/) |
| `core-rules` | 1 | 🔵 skeleton | [`core-rules/`](core-rules/) |
| `core-ml` | 1 | 🔵 skeleton | [`core-ml/`](core-ml/) |
| `core-sensors` | 1 | 🔵 skeleton | [`core-sensors/`](core-sensors/) |
| `core-ui-base` | 1 | 🔵 skeleton | [`core-ui-base/`](core-ui-base/) |

## What each package owns

- **`core-protocol`** — wire types (`Observation`, `Alert`, `Rule`, `AlertTransport`). The leaf module — depends on nothing else in Atalaya.
- **`core-onboarding`** — hints, tutorials, persistence of seen state. Mechanism only; content lives per-app.
- **`core-rules`** — natural-language rule engine. Evaluates an `Observation` against a `List<Rule>` and decides match.
- **`core-ml`** — inference abstraction. Phase 1 ships the llama.rn JNI implementation. Pluggable.
- **`core-sensors`** — sensor abstraction interfaces. Implementations live in apps because they're platform-specific.
- **`core-ui-base`** — Bitwarden's `BaseViewModel<S, E, A>` ported verbatim. The view-layer convention all Atalaya apps use.

## Module dependency rules (per ADR-0001)

- Packages may depend on other packages, but the dependency graph must be acyclic.
- Packages must NOT depend on apps.
- `core-protocol` is the leaf. Every other package can depend on it; it depends on nothing internal.

See [`../docs/MODULE-MAP.md`](../docs/MODULE-MAP.md) for the visual dependency graph.
