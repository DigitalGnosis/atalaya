# `packages/core-observability`

Observability for Atalaya. Wraps CipherWare so every Atalaya component emits spans the same way other Digital Gnosis services already do.

**Phase:** 1 (current)
**Stack:** Kotlin (Android-targeting; CipherWare Kotlin SDK)
**Status:** 🔵 skeleton

## What it owns

- A thin facade over CipherWare's Kotlin client
- `Tracer` interface — `startSpan(name, attrs) → Span` then `span.end(status, attrs)`
- `Logger` interface — `info(msg, attrs)`, `error(throwable, msg, attrs)`
- Trace ID propagation between watcher service, ML inference, rule engine
- Atalaya-specific span naming conventions (e.g. `atalaya.frame.capture`, `atalaya.inference.gemma.vision`, `atalaya.rule.evaluate`)
- Opt-in user-facing telemetry switch (off by default — privacy first)

## What it does NOT own

- The CipherWare backend itself (lives at `/opt/dg-middleware/data/cipherware.db` on dg-core)
- Crash reporting (separate concern; Phase 1 may add Bugsnag or Sentry separately, opt-in)
- Performance metrics rendering (those live in CipherWare Viewer)

## Dependencies

| On | Why |
|----|-----|
| [`cipherware-sdk-android`](https://github.com/DigitalGnosis/cipherware-sdk-android) | The DG-published Kotlin SDK. We CONSUME this — we do not write a new client. |
| `kotlinx.coroutines` | Async span emission |

`cipherware-sdk-android` is a separate DG-owned repo that already ships the Kotlin SDK Connect uses today. Atalaya pulls the same artifact by version. This module is a thin facade over that SDK plus Atalaya-specific span naming conventions — nothing more.

## Why CipherWare

DG already runs CipherWare as the central observability spine for all services. Atalaya is a DG product. Plugging into CipherWare means:

- One place to view spans across the entire DG stack — Connect, Watchman, Atalaya, dg-spine
- Existing tooling — `cipherware` CLI, CipherWare Viewer web UI — works for Atalaya immediately
- Kotlin SDK already proven in Connect (FCM, playback, downloads, uploads, WS lifecycle)
- No new vendor, no new API key, no new bill

## Privacy posture

Telemetry is **opt-in only.** The default install emits zero spans externally. A user who opts in has spans flow to whichever CipherWare instance they configure (their own self-hosted or DG's, their choice). Span attributes never include personally identifying content — frame contents, descriptions, user data all stay out of attributes.

## Consumers

| Module | Use |
|--------|-----|
| [`apps/node`](../../apps/node/) | Watcher service emits `atalaya.frame.capture` and `atalaya.alert.fire` spans |
| [`packages/core-ml`](../core-ml/) | Emits `atalaya.inference.gemma.vision` and `atalaya.inference.gemma.judge` spans with latency, peak RAM |
| [`packages/core-rules`](../core-rules/) | Emits `atalaya.rule.evaluate` spans with rule ID, match boolean (no description content) |

## Specs

- DG-wide span taxonomy and contracts: `dg-backchannel/projects/wire-contracts/2026-04-21-dg-wire-contracts.md`
- CipherWare CLI reference: see `dg-helm show cipherware`

## Source layout (planned)

```
packages/core-observability/
├── README.md
├── build.gradle.kts
└── src/main/kotlin/dev/digitalgnosis/atalaya/observability/
    ├── Tracer.kt              # interface
    ├── Logger.kt              # interface
    ├── Span.kt
    ├── cipherware/
    │   ├── CipherWareTracer.kt
    │   └── CipherWareLogger.kt
    └── conventions/
        └── SpanNames.kt       # the canonical atalaya.* span names
```
