# Atalaya — Per-Phase PRD

**Date:** 2026-05-04
**Author:** Forge (Engineering)
**Companion to:** `atalaya-design-2026-05-04.md`
**Status:** Draft

---

## Operating Principles (Non-Negotiable)

1. **No scratch builds.** Every phase opens with research. Find the app that already solves the piece, study it, port the pattern.
2. **Bitwarden BaseViewModel is the view-layer convention.** Not a debate. Every Atalaya app (node, hub admin, control) uses Bitwarden's MVVM + state pattern.
3. **Modularity is the prime directive.** Every role, sensor, transport, and storage backend is a plugin. Swap any piece without touching others.
4. **Time-boxed research.** Each phase's research opens with a hard cap (default 3 working days). After the cap: stop researching, start porting. The PRD is a scaffold for hunting, not a license to study forever.
5. **Public from week one.** Repo open from Phase 0. Build in public.

---

## Phase 0 — Foundations

**Goal:** Repo, license, manifesto, conventions, naming. Anything that touches every later phase is decided here.

**Success criteria:**
- Repo exists, public, with README that explains the SimpliSafe / Supabase comparison
- License decided and applied
- Module/package layout defined
- BaseViewModel skeleton committed
- CI green on day one
- Atalaya name + domain checked for trademark/availability

**Research targets (3-day cap):**
- **Bitwarden Android** (`bitwarden/android`) — read MVVM module structure, BaseViewModel, Compose+Hilt patterns, Gradle conventions
- **Now in Android** (`android/nowinandroid`) — Google's reference for modern Android architecture
- **Supabase monorepo** (`supabase/supabase`) — read how a BaaS organizes app/api/db/realtime as separate packages in one repo
- **Pocketbase** (`pocketbase/pocketbase`) — single-binary Go BaaS, study how minimal and modular a hub can be
- License examples: MongoDB SSPL, Sentry's BSL-then-Apache, Bitwarden GPL3 + commercial dual

**Patterns to port:**
- Bitwarden's BaseViewModel + MVI-style state/event/effect tuple
- Now in Android's module separation: `:core:*`, `:feature:*`, `:app`
- Supabase-style monorepo if we plan multiple deliverables (node app, hub, control app, web dashboard)

**Modular boundaries (sacred):**
- App layer never imports network types directly — always through repository abstractions
- ML inference is its own module, replaceable per-platform (Android ML Kit, llama.cpp, server-hosted)
- Sensor access is its own module, role-pluggable

**Deliverables:**
- `atalaya/` repo with module skeleton
- `LICENSE` (lean AGPL-3 or dual AGPL+commercial)
- `README.md` with the elevator pitch + comparison to SimpliSafe and Supabase
- `ARCHITECTURE.md` (port the design doc)
- `CONTRIBUTING.md` with the operating principles
- BaseViewModel + sample ViewModel committed
- CI: lint + test on PR

**Time-box:** 5 working days total (3 research, 2 setup)

**Open questions:**
- Final name (Atalaya available?)
- License (AGPL-3 alone, or dual)
- Monorepo or polyrepo
- GitHub org name

---

## Phase 1 — Single Camera Node, Local Gemma 4

**Goal:** One Android phone, one role (camera node), local Gemma 4 inference, natural-language rule, push alert to a designated phone. Real product, ships standalone.

**Success criteria:**
- App installs on a Pixel 6+ from F-Droid or APK
- Configure a single rule in plain English: e.g. "alert me if any person is visible after 9 PM"
- Phone takes a frame every N seconds (configurable 3–30s)
- Gemma 4 E2B describes the frame on-device
- Rule engine evaluates the description against the rule
- Match → push notification fires to designated control device
- Survives battery optimization, screen off, locked
- Works offline (no internet)

**Research targets (3-day cap):**
- **Off Grid** (open-source local AI on Android) — port the GGUF model loading + inference loop
- **Google AI Edge Gallery** — official Google sample for Gemma 4 on Android, port the runner
- **llama.cpp Android docs** (`ggml-org/llama.cpp/docs/android.md`) — JNI + build setup
- **MediaPipe LLM Inference** samples — alternative runner if llama.cpp Android proves unstable
- **Haven** (`guardianproject/haven`) — port the MonitorService pattern that survives Android battery optimization (GPL-3, AGPL-compatible)
- **Termux:API** (`termux/termux-api`, already cloned) — `MicRecorderAPI` + `TextToSpeechAPI` patterns for sensor access from a service
- **Bitwarden Android FCM integration** — port the push notification pipeline
- **CameraX official samples** — for the capture loop

**Patterns to port:**
- Haven's MonitorService → Atalaya NodeService (foreground service that survives Android's death squad)
- Off Grid / AI Edge Gallery → Gemma 4 model loader
- Bitwarden's FCM → push wiring
- Bitwarden's BaseViewModel → all UI state

**Modular boundaries:**
- `:role:camera` module — depends only on `:core:sensor`, `:core:rules`, `:core:alerts`
- `:inference:gemma` module — pluggable; could swap to a remote endpoint module later
- `:transport:push` module — pluggable; could swap to MQTT, WebSocket, dispatch

**Deliverables:**
- `atalaya-node` Android app, role: camera
- Settings UI: rule input (NL), interval, alert target
- One-tap test trigger ("simulate alert")
- F-Droid metadata + screenshots

**Time-box:** 4–6 weeks after Phase 0 ships

**Open questions:**
- Gemma 4 E2B vs E4B for v1 — start with E2B, keep loader pluggable
- Frame interval default — 5s? 10s? Configurable from day one
- Alert transport — FCM-only for v1, MQTT in Phase 2

---

## Phase 2 — Hub + Multi-Node Coordination

**Goal:** Multiple nodes report to a hub. Hub orchestrates rules across nodes, persists events, runs heavier AI for nodes that can't.

**Success criteria:**
- Hub Docker image runs on dg-core, Pi, or any Linux box (`docker compose up`)
- Two or more nodes register with the hub over Tailscale or local network
- Web dashboard at hub URL shows node status, recent events, live thumbnails
- Multi-modal rule: "if camera A says person AND audio node says footsteps within 5s, alert"
- Hub stores last 24h of events with searchable metadata
- Hub-to-node and node-to-hub all encrypted (TLS or mesh-encrypted)

**Research targets (3-day cap):**
- **Supabase** — how a self-hostable BaaS layers Postgres + auth + realtime + storage. Decide: bring Supabase wholesale OR build narrow Pocketbase-style.
- **Pocketbase** — single-binary alternative; study its admin UI, auth, realtime
- **Frigate** (`blakeblackshear/frigate`) — port the MQTT event model and the Home Assistant integration shape
- **Home Assistant Core** — port the entity / device / integration abstraction
- **DeepCamera** (`SharpAI/DeepCamera`) — port the agentic watcher pattern
- **NATS / MQTT** broker patterns for node-to-hub coordination
- **Tailscale Android client** (open source) — port the mesh integration

**Patterns to port:**
- Pocketbase's single-binary, single-config simplicity (if we go narrow)
- Supabase's realtime subscription pattern (if we go full)
- Frigate's MQTT event taxonomy (port the topic structure)
- DeepCamera's pluggable AI skill model

**Modular boundaries:**
- Hub storage backend pluggable: Postgres (default), SQLite (small deployments)
- Hub object store pluggable: S3-compat (production), local filesystem (small)
- Hub realtime transport pluggable: WebSocket, MQTT, NATS
- AI inference pool can run in-hub or delegate to dg-core / external server

**Deliverables:**
- `atalaya-hub` Docker image
- `atalaya-hub-web` admin dashboard (Compose Multiplatform web, or Svelte/React)
- `docker-compose.yml` for one-line self-host
- Hub-side rule engine that fires across multiple nodes
- E2E encryption between node, hub, and control

**Time-box:** 6–8 weeks

**Open questions:**
- Supabase vs Pocketbase vs custom narrow stack — depends on Phase 0 choice
- MQTT vs WebSocket vs NATS — read Frigate before deciding
- Where does AI inference live for nodes that can't run it locally — hub? dg-core? external?

---

## Phase 3 — Role Expansion

**Goal:** Every sensor type a phone exposes is a deployable role. Each role is a separately-shippable module.

### 3a. Audio Classifier Node

**Research targets:**
- **YamNet** (TensorFlow audio model) — port classification head
- **Google's TF Lite audio classifier samples** — the reference Android implementation
- **BirdNet** (FOSS audio classification) — pattern for continuous classification

**Patterns to port:**
- TF Lite's continuous audio buffer + classification loop
- Sound event taxonomy (glass break, gunshot, smoke alarm, baby cry, dog bark)

**Time-box:** 3 weeks

### 3b. Contact Node (Magnetometer + Magnet)

**Research targets:**
- F-Droid magnetic field apps as reference
- Haven's sensor trigger code (port directly)

**Patterns to port:**
- Magnetometer threshold detection
- Pairing flow: hold magnet, calibrate baseline, set threshold

**Time-box:** 1 week

### 3c. Motion Node

**Research targets:**
- Haven's accelerometer + camera trigger (port directly)

**Patterns to port:**
- Haven's accel-to-camera-capture pipeline; replace its motion-only logic with Gemma 4 confirmation

**Time-box:** 1 week

### 3d. Presence Node

**Research targets:**
- Home Assistant's `device_tracker` — WiFi/BLE presence integration
- Tasker plugins (closed but documented) — WiFi presence patterns

**Patterns to port:**
- WiFi scan + recognized-device list
- BLE beacon scan + RSSI thresholds

**Time-box:** 2 weeks

### 3e. Keypad Node (Voice + NFC + Face)

**Research targets:**
- **Termux:API** SpeechToText (already cloned)
- F-Droid NFC tools for fob reading
- Google's face recognition samples (ML Kit Face)

**Patterns to port:**
- Voice passphrase challenge → TTS confirm → STT capture → match
- NFC tag UID match
- Face match with liveness detection

**Time-box:** 4 weeks

### 3f. Siren Node

**Research targets:** Trivial. Standard Android `MediaPlayer` + TTS.

**Time-box:** 2 days

**Phase 3 Total:** 11–12 weeks if shipped serially. Can parallelize roles across contributors.

---

## Phase 4 — Control App (Homeowner-Facing)

**Goal:** Separate Android app for the daily-driver phone. Arm/disarm, live view, history, rule config.

**Success criteria:**
- Connects to hub over Tailscale or local network
- Arm/disarm with passcode, voice, or face
- Live thumbnails from camera nodes
- Event history (last 30 days, searchable)
- Rule editor with NL input + suggestions

**Research targets:**
- **Bitwarden Android** — control plane patterns, auth flow, settings hierarchy
- **Home Assistant Companion App** — live view + control patterns
- **Supabase Studio** — admin UI patterns translated to mobile

**Patterns to port:**
- Bitwarden's auth flow + passcode + biometric unlock
- Bitwarden's BaseViewModel for every screen
- Material 3 theming consistent with node app

**Modular boundaries:**
- Hub client is an interface; implementations: Atalaya protocol (default), MQTT (alt)
- Live view component pluggable: WebRTC, MJPEG poll, snapshot

**Deliverables:**
- `atalaya-control` Android app
- iOS port via shared Kotlin Multiplatform Mobile (KMM) — Phase 4b
- Push notifications fed from hub

**Time-box:** 6–8 weeks

---

## Phase 5 — Cloud Hub Option

**Goal:** Managed hosted hub at atalaya.dg (or wherever). Same code as self-hosted. Convenience tier.

**Success criteria:**
- One-click signup
- Free tier for small deployments (≤3 nodes, 7-day retention)
- Paid tier (more nodes, longer retention, SMS alert add-on, optional pro monitoring)
- Self-host always available — no feature lockout
- E2E encryption preserved — we host encrypted blobs, can't see content

**Research targets:**
- **Supabase Cloud** — pricing model, signup flow
- **Bitwarden's premium tier** — payment integration patterns
- **Stripe's Android/iOS SDKs** — billing
- **Plausible Analytics** — how a self-host-first project monetizes the hosted version

**Patterns to port:**
- Supabase's pricing tiers (free + pro + team + enterprise)
- Bitwarden's Stripe integration
- Plausible's "you can self-host the same thing" messaging

**Modular boundaries:**
- Billing module separable; self-hosters never load it
- Cloud hub uses the same `atalaya-hub` Docker image — no fork

**Deliverables:**
- Cloud onboarding flow
- Billing
- Optional pro monitoring partner integration
- Privacy + security documentation

**Time-box:** 8–10 weeks

---

## Phase 6 — Hardware Integrations

**Goal:** Atalaya integrates with non-phone sensors and existing smart home gear. Plays well with Home Assistant.

**Success criteria:**
- Z-Wave / Zigbee bridge support (water leak, smoke, CO sensors)
- USB peripheral support (NFC reader, external siren)
- Home Assistant integration: nodes appear as entities; HA can arm/disarm Atalaya
- MQTT bridge: any MQTT device can be an Atalaya sensor

**Research targets:**
- **Zigbee2MQTT** (`Koenkk/zigbee2mqtt`) — port the bridge model
- **Home Assistant Core** — port the integration spec; ship Atalaya as an HA integration
- **MQTT** patterns from Frigate

**Patterns to port:**
- Z2M's device translation layer
- HA's integration entity model

**Modular boundaries:**
- External device adapters are plugins; not a kitchen-sink hub

**Deliverables:**
- Atalaya HA integration on HACS
- Z2M / ZHA bridge support
- USB peripheral SDK

**Time-box:** 8–10 weeks

---

## Cross-Cutting Concerns

These cut across all phases. Each phase must address them.

### Power Management
- Cycle charging support (charge to 80%, drain to 30%, repeat)
- Sleep modes for low-priority nodes
- Battery health monitoring per node

### Node Health
- Heartbeat ping every N seconds
- Hub alerts if a node goes silent
- Self-test on startup

### Updates
- Play Store / F-Droid for node app
- Docker pull for hub
- Signed releases, reproducible builds where possible

### Privacy
- E2E encryption: node ↔ hub ↔ control
- Hub stores encrypted blobs only
- Local-first: no data leaves the user's network unless they opt into cloud

### Telemetry
- Opt-in only
- Anonymized
- Plausible-style minimal

### Documentation
- Every phase ships docs at the same time as code
- Self-host guide, contribution guide, role spec, plugin author guide

---

## Reference Implementation Map (Quick Index)

| Need | Reference | License | Notes |
|------|-----------|---------|-------|
| MVVM / view layer | Bitwarden Android | GPLv3 | Locked-in convention |
| Module structure | Now in Android | Apache | Google's modern reference |
| Local LLM on Android | Off Grid + Google AI Edge Gallery | Apache / MIT | Phase 1 critical |
| Background service survival | Haven MonitorService | GPLv3 | Port directly |
| Sensor APIs | Termux:API source | Apache | Already cloned |
| Camera capture | CameraX samples | Apache | Standard |
| Push | Bitwarden FCM | GPLv3 | Standard |
| Hub backend (full) | Supabase | Apache | Self-host model |
| Hub backend (narrow) | Pocketbase | MIT | Single-binary |
| Realtime | Supabase Realtime / Centrifugo | Apache / Apache | Pluggable |
| Audio classification | YamNet / TF Lite samples | Apache | Phase 3a |
| Mesh networking | Tailscale Android | BSD | Pluggable transport |
| HA integration | HA Core | Apache | Phase 6 |
| Z-Wave/Zigbee | Zigbee2MQTT | MIT | Phase 6 |

---

## Risk Register (Track Across Phases)

1. **Liability** — DIY-only positioning, never replace monitored alarm without insurance
2. **Old phone reliability** — node health monitoring mandatory
3. **Battery damage from always-charging** — power management baked in from Phase 1
4. **AI false positives** — confidence layer + user-trainable suppression
5. **Old Android version ceiling** — non-AI roles available for old phones
6. **Update delivery** — F-Droid + Play Store + Docker pull all functional
7. **Mesh transport dependency** — Tailscale assumption, but pluggable for users who don't use it
8. **Hub-as-single-point-of-failure** — multi-hub support eventually, but document the SPOF clearly in v1

---

## Phase Status Tracker (Living Document)

| Phase | Status | Started | Shipped | Notes |
|-------|--------|---------|---------|-------|
| 0 | Not started | — | — | Foundations |
| 1 | Not started | — | — | Single camera node |
| 2 | Not started | — | — | Hub + multi-node |
| 3a–f | Not started | — | — | Role expansion |
| 4 | Not started | — | — | Control app |
| 5 | Not started | — | — | Cloud hub |
| 6 | Not started | — | — | Hardware integrations |

Update this table as phases progress.
