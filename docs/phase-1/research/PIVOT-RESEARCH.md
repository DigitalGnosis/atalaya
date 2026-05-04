---
title: Pivot research — phones-as-sensors, hub-as-brain
status: needs investigation
opened: 2026-05-04
time_box_per_item: 3 days
---

# Pivot research — what we need to know before resuming

The founder caught the on-device-inference flaw. We're pivoting to **phones-as-sensors, hub-as-brain.** This document lists the concrete research items the pivot opens. Each item has a hard 3-day time-box.

Open the [HANDOFF.md](../../../HANDOFF.md) at the repo root for the full story and what's affected.

---

## Item 1 — Streaming protocol from phone to hub

**Question:** What protocol does the phone use to ship frames to the hub?

**Candidates:**
- **WebSocket over mesh, framed JPEGs.** Simplest. Works over Atalaya Mesh (WireGuard tunnel). Easy to implement on Android.
- **RTSP.** Frigate's standard. But Android phones don't natively serve RTSP — we'd need to embed a server.
- **WebRTC.** Built for video streaming, NAT traversal built in. Heavyweight; we don't need P2P (mesh handles routing).
- **MJPEG over HTTP.** Dead simple, works everywhere, bandwidth-heavy.
- **gRPC streaming.** Strongly typed, good tooling. Adds protobuf dependency.

**Research targets:**
- Frigate's go2rtc — how it ingests from various sources
- Tailscale's recommendations for streaming over mesh
- Battery cost of WebSocket vs MJPEG on Android (continuous connection vs reconnect-per-frame)
- Bandwidth at 5s cadence × 768x768 JPEG quality 75 (rough math: ~50KB/frame, 600KB/min, ~36MB/hour)

**Time-box:** 3 days
**Triggers ADR:** new ADR-NNNN to replace ADR-0003 (now hub-side) and define the wire shape

---

## Item 2 — Hub stack architecture

**Question:** What runs on the hub?

**Candidates:**
- **Embed Frigate.** Mature, well-supported, has the streaming + AI + storage pieces. Heavy footprint (~2 GB), opinionated about its UI.
- **Custom Python/Go server.** Calls llama.cpp directly via REST or gRPC, minimal footprint. We own the whole stack.
- **Hybrid: Frigate for ingest + custom layer on top.** Use Frigate's mature pieces, add Atalaya-specific rule engine and routing.

**Research targets:**
- Frigate's actual hub footprint (RAM, CPU, disk, dependencies)
- Whether Frigate's external API is rich enough for our purposes
- Sizing for a "minimum viable Atalaya hub": dg-core is fine, but what about a Pi 5 with 8GB? A used NUC?
- Comparison: build-on-Frigate vs build-narrow-Python-server in time-to-v1

**Time-box:** 3 days
**Triggers ADR:** new ADR-NNNN for hub stack choice + minimum hub spec

---

## Item 3 — Hub-side inference placement

**Question:** Where does Gemma 4 vision run on the hub?

We already established (in earlier conversation) that dg-core can run Gemma 4 E4B on CPU. For DG-distributed Atalaya, dg-core is the obvious hub. For self-hosters, what?

**Candidates:**
- **CPU-only, llama.cpp.** Universal, slower per frame. Pi 5 8GB might handle E2B; E4B is tight.
- **GPU via Vulkan/OpenCL.** Faster but requires beefier hub hardware.
- **Cloud API (OpenAI/Anthropic vision).** Easy for users without infra; violates privacy-first posture by default.

**Research targets:**
- llama.cpp inference latency for Gemma 4 E2B/E4B on Pi 5, NUC, dg-core, used Mac Mini
- Power draw at always-on inference
- Whether hub-side WAN-API option is offered as opt-in for users without local hardware

**Time-box:** 3 days
**Triggers ADR:** new ADR-NNNN replacing ADR-0003

---

## Item 4 — Phone-side app reshape

**Question:** What does the Android node app actually do, now that inference moved to the hub?

**Phase 1 phone responsibilities (corrected):**
1. Capture frames at the configured cadence (CameraX) — unchanged
2. Compress to JPEG at configured quality
3. Stream to hub over Atalaya Mesh
4. Receive alerts back from hub
5. Show alerts as local notifications (`LocalNotificationTransport`)
6. Settings UI (rule editor, frame interval, alert target)
7. Onboarding (per `core-onboarding`)
8. Hub pairing (mesh join via QR code)

**No longer phone responsibilities:**
- Local Gemma inference
- Rule evaluation (moves to hub)
- Model download (model lives on hub)

**Research targets:**
- CameraX background-service capture without preview surface (we have a stub research note for this — M3-camerax.md — needs filling)
- WebSocket library choice on Android (OkHttp vs Ktor)
- Foreground service type — still `camera|microphone` per ADR-0007 (still valid)

**Time-box:** 3 days
**Triggers:** revision of `apps/node/` module spec; new ADR for streaming client choice

---

## Item 5 — Hub minimum spec + onboarding

**Question:** What's the smallest hub that runs Atalaya, and how does a normie set it up?

**Constraints:**
- Normie target — onboarding can't require Linux skills
- Self-hosted-first — DG-cloud option is opt-in only
- Hub must run Headscale (mesh control plane), the inference server, the rule engine, alert dispatch

**Candidates:**
- **Pi 5 8GB** — $80 hardware + SD card; runs Atalaya hub Docker compose. Documented setup.
- **Used NUC / Mini PC** — $150-300 used; comfortable for E4B inference.
- **Old Mac Mini M1+** — surprisingly good per DeepCamera's experience.
- **Existing home server (Synology, Unraid)** — Docker compose just runs.
- **DG-hosted cloud** — for users who don't want hardware. Opt-in, encrypted-at-rest.

**Research targets:**
- Pi 5 inference benchmarks for Gemma 4 E2B (this is the floor)
- Setup guide complexity — flash an SD card vs run a Docker compose
- Cost-of-entry document we can ship

**Time-box:** 3 days
**Triggers:** README pitch refresh; possibly a `infra/` doc on hub setup paths

---

## Item 6 — Privacy posture for streaming frames

**Question:** Frames now cross the network. What's the privacy story?

**Constraints:**
- Frames stream phone → hub. Mesh is encrypted (WireGuard).
- Hub processes frames in memory. By default, retention is short.
- Saved frames (only on alert) live on the hub. User-controlled retention.

**Research targets:**
- E2E encryption from phone to hub — beyond mesh-level WireGuard, do we add app-level encryption? (Probably yes — defense in depth.)
- Default retention for non-alert frames (recommend: in-memory only, never on disk)
- Default retention for alerted frames (recommend: 30 days, configurable, encrypted at rest)
- The "DG-cloud-hosted hub" privacy promise — what we commit to

**Time-box:** 2 days (smaller scope)
**Triggers:** new ADR-NNNN for privacy posture; revision of README privacy section

---

## Item 7 — Phase 1 scope reshape

**Question:** What does Phase 1 actually ship now?

**Old Phase 1 (wrong architecture):** node-only app on a phone with on-device Gemma. No hub, no mesh.

**New Phase 1 (corrected):** node app on phone + minimal hub on dg-core (or Pi 5 spec) + mesh. Smallest end-to-end demo.

**Research target:** what's the smallest cut of phase 1 that demonstrates the corrected architecture end-to-end? Likely:
- One phone running node app (camera capture, mesh client, alert receiver)
- Hub on dg-core (Headscale + Gemma 4 + simple rule engine)
- One rule, one alert, one notification
- Documented hub setup procedure

**Time-box:** 2 days
**Triggers:** new Phase 1 goal doc, new roadmap with corrected slices, possibly a Phase 1.1 vs Phase 1.0 split

---

## Order of operations

Tackle in this order — each one informs the next:

1. **Item 1 (streaming protocol)** — sets the wire shape
2. **Item 2 (hub stack)** — picks the hub framework
3. **Item 3 (inference placement)** — fixes ADR-0003
4. **Item 5 (hub minimum spec)** — answers the normie self-host question
5. **Item 6 (privacy posture)** — captures the new threat model
6. **Item 4 (phone app reshape)** — depends on items 1+3
7. **Item 7 (Phase 1 reshape)** — depends on everything above

Estimated total research: 2-3 weeks for one engineer working through serially. Less with parallel agents.

## When research lands

Each item produces an ADR (or revises an existing one). The full set of new ADRs supersedes the wrong ones from the original Phase 1. Then we update:

- `ARCHITECTURE.md` (clean rewrite of the affected sections)
- `docs/phase-1/01-goal.md` (new goal)
- `docs/phase-1/02-roadmap.md` (new vertical slices)
- `apps/node/README.md` (corrected scope)
- `packages/core-ml/README.md` (thin client, not on-device runtime)
- New `apps/hub/` directory (Phase 1 essential, not Phase 2 deferred)

Then we resume engineering.
