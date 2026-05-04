# Phase 1 — Single Camera Node, Local Gemma 4

**Status:** 🔵 Open
**Opened:** 2026-05-04
**Time-box:** 4–6 weeks (3 days research per module, hard cap)
**Companion to:** [`../PRD-PHASES.md`](../PRD-PHASES.md)

---

## Quick navigation

| If you want to know... | Read |
|------------------------|------|
| What "done" looks like | [`01-goal.md`](01-goal.md) |
| The ordered build steps | [`02-roadmap.md`](02-roadmap.md) |
| What we've learned researching X | [`research/`](research/) |
| What we've committed to (ADRs) | [`decisions/`](decisions/) |
| What a module looks like in code | [`modules/`](modules/) |
| Which existing project we ported from | [`references/`](references/) |

## Phase 1 in one paragraph

Ship a working Atalaya node — a single Android phone running the watcher daemon in the camera role. Phone snaps a frame every N seconds, runs it through local Gemma 4 E2B for scene description, evaluates the description against a natural-language rule the user wrote, and pushes a notification when the rule matches. No hub yet, no multi-modal confirmation, no fleet — one phone, one role, one rule. Real product, ships standalone.

## Modules to research (M-prefixed for sort order)

| ID | Module | Reference to port from | Stub |
|----|--------|------------------------|------|
| M1 | View-layer convention (BaseViewModel + state/event/effect) | [Bitwarden Android](https://github.com/bitwarden/android) | [`research/M1-bitwarden-baseviewmodel.md`](research/M1-bitwarden-baseviewmodel.md) |
| M2 | Background service that survives Android battery optimization | [Haven MonitorService](https://github.com/guardianproject/haven), Tailscale Android client | [`research/M2-background-service.md`](research/M2-background-service.md) |
| M3 | Camera capture loop | CameraX official samples, Termux:API MicRecorderAPI as a sibling reference | [`research/M3-camerax.md`](research/M3-camerax.md) |
| M4 | Local Gemma 4 inference on Android | [Off Grid](https://github.com/alichherawalla/off-grid-mobile-ai), [Google AI Edge Gallery](https://github.com/google-ai-edge/gallery), [llama.cpp Android](https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md) | [`research/M4-local-gemma.md`](research/M4-local-gemma.md) |
| M5 | Natural-language rule engine | None — designed in this phase | [`research/M5-rule-engine.md`](research/M5-rule-engine.md) |
| M6 | Push notification pipeline | Bitwarden Android FCM | [`research/M6-push.md`](research/M6-push.md) |

## Decisions queue (ADR index)

Each item below becomes its own ADR file when committed. Status `pending` means the question is open and research is still informing the answer.

| # | Question | Status | ADR |
|---|----------|--------|-----|
| 0001 | Monorepo layout — Now in Android style or alternative | ✅ committed | [`decisions/ADR-0001-monorepo-layout.md`](decisions/ADR-0001-monorepo-layout.md) |
| 0002 | View-layer convention (BaseViewModel pattern) | 🔵 pending M1 | _(opens after M1 research)_ |
| 0003 | Inference runtime — llama.cpp Android JNI vs MediaPipe LLM Inference vs Off Grid's runtime | 🔵 pending M4 | _(opens after M4 research)_ |
| 0004 | Default frame interval (3s, 5s, 10s, 30s) | 🔵 pending M4 | _(opens after we measure inference latency)_ |
| 0005 | Rule format — DSL vs LLM-evaluated NL string | 🔵 pending M5 | _(opens after M5 research)_ |
| 0006 | Push transport — FCM-only or FCM + ntfy | 🔵 pending M6 | _(opens after M6 research)_ |
| 0007 | Min SDK / target SDK | 🔵 pending M2 + M4 | _(opens after we know what background-service and inference need)_ |
| 0008 | DI — Hilt or alternative | 🔵 pending M1 | _(opens after M1 — Bitwarden uses Hilt, default likely lands there)_ |

## Module specs

Module specs describe what a module looks like in code, with example shapes. Real implementation lives in `apps/` and `packages/`.

| Module | Spec | Lives in |
|--------|------|----------|
| `core-protocol` (wire types) | [`modules/EXAMPLE-core-protocol.md`](modules/EXAMPLE-core-protocol.md) | `packages/core-protocol/` (future) |
| `core-rules` | _(stub — opens after M5)_ | `packages/core-rules/` (future) |
| `core-ml` | _(stub — opens after M4)_ | `packages/core-ml/` (future) |
| `core-sensors` | _(stub — opens after M3)_ | `packages/core-sensors/` (future) |
| `apps/node` (camera role) | _(stub — opens after M2/M3/M4 land)_ | `apps/node/` (future) |

## Phase 1 success criteria

Copy from [`01-goal.md`](01-goal.md). Briefly:

- App installs on a Pixel 6+ from sideloaded APK
- User configures a single English rule
- Frames captured at configured interval
- Gemma 4 E2B describes the frame on-device, no internet
- Rule engine matches the description against the rule
- Match → push notification fires
- Survives battery optimization, screen off, locked
- Works offline after model download

## Phase 1 out of scope (deferred to later phases)

- Hub (Phase 2)
- Multi-modal confirmation (Phase 2)
- Roles other than camera (Phase 3)
- Control app (Phase 4)
- Cloud option (Phase 5)
- Hardware integrations (Phase 6)

## Risk register (Phase 1 specific)

- **Battery damage from always-on camera + inference.** Mitigation: configurable frame interval, charge-aware throttle, document recommended duty cycles.
- **Inference latency on older phones.** Mitigation: E2B not E4B for v1, document min recommended chipset.
- **False positives confusing users.** Mitigation: confidence threshold, sample-and-show test mode, "is this a real alert?" feedback in v1.1.
- **APK distribution before F-Droid listing.** Mitigation: ship signed APKs from GitHub Releases, document install steps.
