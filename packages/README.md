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
| `core-observability` | 1 | 🔵 skeleton | [`core-observability/`](core-observability/) |
| `core-ui-base` | 1 | 🔵 skeleton | [`core-ui-base/`](core-ui-base/) |
| `core-ui-theme` | 1 | 🔵 skeleton | [`core-ui-theme/`](core-ui-theme/) |
| `core-ui-components` | 1 | 🔵 skeleton | [`core-ui-components/`](core-ui-components/) |

## What each package owns

- **`core-protocol`** — wire types (`Observation`, `Alert`, `Rule`, `AlertTransport`). The leaf module — depends on nothing else in Atalaya.
- **`core-onboarding`** — hints, tutorials, persistence of seen state. Mechanism only; content lives per-app.
- **`core-rules`** — natural-language rule engine. Evaluates an `Observation` against a `List<Rule>` and decides match.
- **`core-ml`** — inference abstraction. Phase 1 ships the llama.rn JNI implementation. Pluggable.
- **`core-sensors`** — sensor abstraction interfaces. Implementations live in apps because they're platform-specific.
- **`core-observability`** — wraps CipherWare. Every Atalaya component emits spans through this module.
- **`core-ui-base`** — Bitwarden's `BaseViewModel<S, E, A>` ported verbatim. The view-layer convention all Atalaya apps use.
- **`core-ui-theme`** — Material 3 theme. Colors, typography, dimensions, dark/light schemes.
- **`core-ui-components`** — reusable Compose components (cards, badges, dialogs, list items).

## Phase 2+ packages (planned, not yet created)

| Package | Phase | Purpose |
|---------|-------|---------|
| `core-network` | 2 | HTTP client wrapper for hub communications (OkHttp or Ktor) |
| `core-auth` | 2 | Sessions, tokens, biometric unlock when control app pairs with hub |
| `core-middleware` | 2 | Request/response interceptors, retry policies, rate limiting |
| `core-transport` | 2 | `AlertTransport` implementations — FCM, ntfy, DispatchTransport |
| `core-storage` | 2 | Persistence abstraction — SQLite/Room for hub, encrypted prefs for nodes |

## Module dependency rules (per ADR-0001)

- Packages may depend on other packages, but the dependency graph must be acyclic.
- Packages must NOT depend on apps.
- `core-protocol` is the leaf. Every other package can depend on it; it depends on nothing internal.

See [`../docs/MODULE-MAP.md`](../docs/MODULE-MAP.md) for the visual dependency graph.
