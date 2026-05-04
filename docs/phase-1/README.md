# Phase 1 — Single Camera Node, Local Gemma 4

> ## ⚠️ PHASE 1 PAUSED — ARCHITECTURE PIVOT IN PROGRESS
>
> Founder caught the on-device-inference flaw. Phase 1's premise of "phone runs Gemma 4" doesn't hold. We're pivoting to **phones-as-sensors, hub-as-brain.** Vertical slice work is paused. Open items: see [`research/PIVOT-RESEARCH.md`](research/PIVOT-RESEARCH.md) for what to investigate before resuming. The full pivot context is at [`../../HANDOFF.md`](../../HANDOFF.md).

**Status:** 🔴 Paused (pivot in progress)
**Opened:** 2026-05-04
**Paused:** 2026-05-04
**Time-box:** TBD after pivot research lands
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

## Modules researched (M-prefixed for sort order)

| ID | Module | Reference to port from | Status | Note |
|----|--------|------------------------|--------|------|
| M1 | View-layer convention (BaseViewModel + state/event/effect) | [Bitwarden Android](https://github.com/bitwarden/android) | ✅ findings | [`research/M1-bitwarden-baseviewmodel.md`](research/M1-bitwarden-baseviewmodel.md) |
| M2 | Background service that survives Android battery optimization | [Haven MonitorService](https://github.com/guardianproject/haven), Tailscale Android client | ✅ findings | [`research/M2-background-service.md`](research/M2-background-service.md) |
| M3 | Camera capture loop | CameraX official samples, Termux:API MicRecorderAPI as a sibling reference | ⚪ pending | [`research/M3-camerax.md`](research/M3-camerax.md) |
| M4 | Local Gemma 4 inference on Android | [Off Grid](https://github.com/alichherawalla/off-grid-mobile-ai), [Google AI Edge Gallery](https://github.com/google-ai-edge/gallery), [llama.cpp Android](https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md) | ✅ findings | [`research/M4-local-gemma.md`](research/M4-local-gemma.md) |
| M5 | Natural-language rule engine | None — designed in this phase | ✅ findings | [`research/M5-rule-engine.md`](research/M5-rule-engine.md) |
| M6 | Push notification pipeline | Bitwarden Android FCM | ✅ findings | [`research/M6-push.md`](research/M6-push.md) |

## Decisions (ADR index)

| # | Question | Status | ADR |
|---|----------|--------|-----|
| 0001 | Monorepo layout — Now in Android style or alternative | ✅ accepted | [`decisions/ADR-0001-monorepo-layout.md`](decisions/ADR-0001-monorepo-layout.md) |
| 0002 | View-layer convention (BaseViewModel pattern) | ✅ accepted | [`decisions/ADR-0002-baseviewmodel-pattern.md`](decisions/ADR-0002-baseviewmodel-pattern.md) |
| 0003 | Inference runtime — llama.rn JNI extracted, Gemma 4 E2B Q4_K_M + F16 mmproj | ✅ accepted | [`decisions/ADR-0003-inference-runtime.md`](decisions/ADR-0003-inference-runtime.md) |
| 0004 | Default frame interval (3s, 5s, 10s, 30s) | 🟡 deferred | _(needs real device benchmarks; opens once we have a working node)_ |
| 0005 | Rule format — NL condition with structured envelope | ✅ accepted | [`decisions/ADR-0005-rule-format.md`](decisions/ADR-0005-rule-format.md) |
| 0006 | Push transport — pluggable AlertTransport, Phase 1 ships local-only | ✅ accepted | [`decisions/ADR-0006-push-transport.md`](decisions/ADR-0006-push-transport.md) |
| 0007 | Min SDK 26, target SDK 35, sticky FGS with camera\|microphone | ✅ accepted | [`decisions/ADR-0007-min-sdk-and-service.md`](decisions/ADR-0007-min-sdk-and-service.md) |
| 0008 | DI — Hilt | ✅ accepted | [`decisions/ADR-0008-di-hilt.md`](decisions/ADR-0008-di-hilt.md) |
| 0009 | Federated package strategy | ✅ accepted | [`decisions/ADR-0009-federated-package-strategy.md`](decisions/ADR-0009-federated-package-strategy.md) |
| 0010 | Mesh strategy — Headscale + WireGuard (Phase 2 impl) | ✅ accepted | [`decisions/ADR-0010-mesh-strategy.md`](decisions/ADR-0010-mesh-strategy.md) |
| 0011 | APK signing key custody — BWS DG-Shared | ✅ accepted | [`decisions/ADR-0011-signing-key-custody.md`](decisions/ADR-0011-signing-key-custody.md) |
| 0012 | Crash reporting — self-hosted Sentry, opt-in | ✅ accepted | [`decisions/ADR-0012-crash-reporting.md`](decisions/ADR-0012-crash-reporting.md) |
| 0013 | First dogfood device — Galaxy S21 | ⚠️ superseded by ADR-0014 | [`decisions/ADR-0013-dogfood-device.md`](decisions/ADR-0013-dogfood-device.md) |
| 0014 | Dogfood device correction — S21 exercises CPU/OpenCL, NOT NPU | ✅ accepted | [`decisions/ADR-0014-dogfood-device-correction.md`](decisions/ADR-0014-dogfood-device-correction.md) |

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

## Open questions (need founder / design input)

- **DG brand language is undefined.** No canonical color palette, typography, voice, or logo across DG products. Atalaya's `core-ui-theme` ships with Material 3 defaults until a brand spec lands. Tracked separately; this is a Digital Gnosis (founder + design) deliverable, not Atalaya engineering.
- **Default frame interval** (deferred via ADR-0004) — needs real device benchmarks once a working node lands.

## Risk register (Phase 1 specific)

- **Battery damage from always-on camera + inference.** Mitigation: configurable frame interval, charge-aware throttle, document recommended duty cycles.
- **Inference latency on older phones.** Mitigation: E2B not E4B for v1, document min recommended chipset.
- **False positives confusing users.** Mitigation: confidence threshold, sample-and-show test mode, "is this a real alert?" feedback in v1.1.
- **APK distribution before F-Droid listing.** Mitigation: ship signed APKs from GitHub Releases, document install steps.
- **No DG brand spec yet.** Mitigation: ship neutral Material 3 defaults; theme module designed for one-line swap when brand lands.
