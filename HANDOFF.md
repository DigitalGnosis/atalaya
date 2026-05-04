# 🔴 HANDOFF — READ THIS FIRST

**Status:** Architecture pivot in progress
**Pivoted:** 2026-05-04
**Action required:** Research before continuing engineering work

---

## TL;DR for the next engineer

Stop. Before you continue any vertical slice work or write any new code, **read this whole file and the [PIVOT-RESEARCH.md](docs/phase-1/research/PIVOT-RESEARCH.md) note.**

A core premise of this project — that old Android phones run Gemma 4 vision on-device — was wrong. We caught it. The right architecture is **phones as sensors, AI on the hub.** Five ADRs need rewriting and the architecture doc has a banner pointing at this file.

The bones are still right. The compute placement was wrong.

## What happened

Atalaya was originally pitched as "AI security system on repurposed old phones" — meaning the phone runs Gemma 4 locally. Phase 1 ADRs (especially ADR-0003) committed to llama.rn JNI on the phone with E2B model files.

The founder caught the contradiction: an "old phone" by definition can't run modern AI well. Verification confirmed it:

- Snapdragon 888 (Galaxy S21 — 2021 flagship) has Hexagon V68
- llama.rn HTP backend requires V69+ (Snapdragon 8 Gen 1+, 2022+)
- Even the 2021 flagship doesn't get NPU acceleration through our stack
- Anything older than that falls off a cliff entirely

The product was promising hardware behavior the hardware can't deliver. We caught this BEFORE writing real code, which is the saving grace.

## The corrected architecture

**Phones are sensors. The hub is the brain.**

```
[Old phone (camera role)]                      [Hub on a real machine]
   captures frame ─────► streams over Atalaya Mesh ─────► Gemma 4 vision
                                                              │
                                                              ▼
                                                       evaluates rules
                                                              │
                                                              ▼
[Control phone] ◄────── alert if match ◄─────────────────────┘
```

- Phone runs: CameraX capture, frame compression, mesh networking, alert receiver. **No on-device inference.**
- Hub runs: stream ingest, Gemma 4 inference (CPU on dg-core or any beefy box), rule engine, alert dispatch.
- Mesh (Headscale + WireGuard, ADR-0010) becomes Phase 1 essential, NOT Phase 2 deferred — phone needs to talk to hub from day one.
- Pitch gets stronger: ANY old phone can stream a JPEG. Hardware floor drops to 4 GB RAM and Android 8 with no NPU concern at all.

This is closer to **Frigate's** architecture (ingest from cameras, central NVR with AI) than to DeepCamera's. We become "Frigate's pattern with phones-as-cameras + privacy-first opinionated stack."

## What's still valid (don't rewrite these)

| Decision | Status |
|----------|--------|
| Project name — Atalaya | ✅ Keep |
| License — AGPL-3.0 | ✅ Keep |
| GitHub org — DigitalGnosis | ✅ Keep |
| Monorepo layout (ADR-0001) | ✅ Keep |
| BaseViewModel pattern (ADR-0002) | ✅ Keep |
| Rule format (ADR-0005) | ✅ Keep — rule engine just moves to hub-side |
| Push transport (ADR-0006) | ✅ Keep — `AlertTransport` interface unchanged |
| DI Hilt (ADR-0008) | ✅ Keep |
| Federated package strategy (ADR-0009) | ✅ Keep |
| Mesh — Headscale + WireGuard (ADR-0010) | ✅ Keep — promote from Phase 2 to Phase 1 essential |
| Signing keys BWS DG-Shared (ADR-0011) | ✅ Keep |
| Crash reporting Sentry (ADR-0012) | ✅ Keep |
| Normie target user | ✅ Keep |
| DG ecosystem integration | ✅ Keep |
| `core-protocol`, `core-onboarding`, `core-rules`, `core-sensors`, `core-ui-base`, `core-ui-theme`, `core-ui-components`, `core-observability` modules | ✅ Keep — interfaces stay, implementation locations shift |

## What's now WRONG (do not act on these — rewrite first)

| Decision | What's wrong |
|----------|-------------|
| ADR-0003 (Inference runtime) | ⚠️ Says llama.rn JNI on phone. Should be: hub-side inference (Python/Go on Linux) + thin client on phone |
| ADR-0004 (Frame interval) | ⚠️ Was deferred for benchmarks; benchmarks are now hub-side, not phone-side |
| ADR-0007 (Min SDK + service) | ⚠️ Min SDK can drop because phone does less. Service still foreground but lighter |
| ADR-0013 (Galaxy S21 dogfood) | ⚠️ Already superseded by ADR-0014 |
| ADR-0014 (S21 dogfood corrected) | ⚠️ Still names S21 but for the wrong reasons; phone-as-sensor changes what we're testing |
| `packages/core-ml/` module spec | ⚠️ Currently scoped as on-device runtime; needs reshape to "thin client of hub inference" |
| Phase 1 scope | ⚠️ Currently ships node-only with notification-on-same-phone. Now needs hub + node together because there's no inference without the hub |

## Where to start (next engineer's first hour)

1. **Read [`docs/phase-1/research/PIVOT-RESEARCH.md`](docs/phase-1/research/PIVOT-RESEARCH.md)** — the concrete research items the pivot opens.
2. **Read [`ARCHITECTURE.md`](ARCHITECTURE.md)** — the design doc, with the pivot banner pointing back here.
3. **Skim the affected ADRs** (003, 004, 007, 013, 014) to see what's marked stale. **Don't rewrite them yet** — research the answers first.
4. **Pick the highest-leverage research item** from PIVOT-RESEARCH.md and time-box it (3 days max).
5. **When you have findings, write the new ADR that supersedes the old one.** Same pattern as ADR-0014 supersedes ADR-0013.

## What Forge already considered but didn't fully answer

- **Streaming protocol** — RTSP, MJPEG, WebRTC, or custom-over-mesh. Frigate uses RTSP from IP cameras; Atalaya can't because phones don't natively serve RTSP. Most likely: a custom WebSocket over the mesh, framed JPEGs. Validate against Frigate's go2rtc patterns.
- **Hub stack** — embed Frigate? Or build atop a smaller Python/Go server that calls llama.cpp directly? Frigate is mature but huge; a custom narrow hub is leaner but reinvents wheels. Decide.
- **Where inference lives for DG users vs self-hosters** — DG users get dg-core (already established it can run Gemma 4 E4B on CPU). Self-hosters need to spec a minimum hub: Pi 5 8GB? NUC? An old desktop?
- **Bandwidth and battery cost on the phone** — streaming JPEGs at 5-second cadence is light, but always-on cellular cost for users without home WiFi matters.
- **Privacy posture** — frames cross the mesh now. Default is end-to-end encrypted from phone to hub. But: hub stores frames, however briefly. Default retention should be aggressive (1 hour rolling buffer + saved frames only on alert).

## What's already committed to git

The following files reflect the OLD architecture and have either been banner-marked or stay valid:

- `ARCHITECTURE.md` — banner added, content needs rewrite (DON'T rewrite without research first)
- `docs/phase-1/decisions/ADR-0003-*.md` through `ADR-0014-*.md` — banners on the wrong ones
- `docs/phase-1/01-goal.md` — needs revision after architecture lands
- `docs/phase-1/02-roadmap.md` — vertical slices paused; new slices to be designed
- `apps/node/`, `packages/*/` — module scaffolding still valid; some specs need reshape
- `BUILDING.md`, `LICENSE`, `README.md`, `CONTRIBUTING.md`, `SECURITY.md` — unchanged, all still valid

## What Nigel needs to know on resume

1. The pivot is captured. He doesn't need to remember the details.
2. His TODO is in [`NIGEL-TODO.md`](NIGEL-TODO.md) — updated to reflect this state.
3. The team caught a real architectural error before shipping. That's a win, not a setback.
4. Phase 1 is paused, not cancelled.
5. The next session opens with research.

## Provenance

This handoff was written 2026-05-04 by Forge after the founder caught the contradiction. The pivot conversation is preserved in this session's transcript and the dispatch history (search "phones-as-sensors" or "ADR-0014" in dispatch history).
