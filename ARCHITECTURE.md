# Atalaya — DIY AI Security System on Old Phones

**Date:** 2026-05-04
**Author:** Forge (Engineering)
**Status:** Design draft, pre-build
**Tagline:** Your old phones. Your security system. Your code. AI-native, multi-modal, self-hosted, open source. SimpliSafe meets Supabase.

---

## The Problem

SimpliSafe and Ring sell closed, cloud-dependent systems with monthly fees, professional monitoring, and questionable privacy. Open-source homelab options exist (Frigate, Scrypted, DeepCamera) but assume IP cameras and a beefy central server. Phone-as-camera apps (Alfred, Manything, AtHome) are old-school motion-detection wrappers with cloud upload. None of them put modern on-device VLM vision on a phone for surveillance. Haven (Snowden + Guardian Project, GPL-3, dormant since 2021) is the closest spiritual ancestor — single-purpose tamper detection, no AI, no fleet, no orchestration. The intersection — phone-as-AI-camera-node forming a complete security system — is empty.

## The Pitch

Atalaya turns junked Android phones into a complete, self-hosted, AI-native security system. Vision, audio, contact, motion, presence, keypad, siren — all from old hardware most people throw away. Multi-modal AI confirmation kills false positives. Natural-language rules. End-to-end encryption. Self-hosted by default; optional managed hub for users who don't want to run their own server.

Like Supabase: you can use the cloud version, or run the same code on your own server. Like SimpliSafe: a real working security system. Unlike either: your data, your hardware, your rules.

---

## Hardware Substrate: The "Weaponized Android" Inventory

A single Android phone exposes:

- **Camera** — vision (Gemma 4 E2B/E4B scene description)
- **Microphone** — audio classification (glass break, gunshot, smoke alarm, voice)
- **Accelerometer** — tamper / vibration / slam detection
- **Light sensor** — sudden ambient changes
- **Magnetometer** — door/window contact (with paired magnet)
- **GPS** — geofencing
- **WiFi scanner** — presence detection (which devices on network = who's home)
- **Bluetooth/BLE** — beacon proximity
- **NFC** — keypad fob arm/disarm
- **Cellular radio** — backup connectivity (WiFi cut? SMS still works)
- **Internal battery** — UPS for power outages
- **Speaker** — siren, TTS announcements
- **Display** — keypad UI, status panel
- **Vibration motor** — haptic alerts

One phone is most of a SimpliSafe.

---

## Architecture: Three Planes

### Node Plane
Phones running the Atalaya node app. Configurable role(s) per phone. Local AI inference where capable. Pushes events to hub.

### Hub Plane
Coordinator. Orchestrates rules across nodes, handles storage, runs heavier AI for nodes that can't.

Hub can be:
- Another phone running hub role
- A Docker container on a Pi or any Linux box
- Self-hosted on dg-core or any server
- Managed cloud (the Supabase analog — same code, same architecture)

### Control Plane
The homeowner's daily-driver phone. Separate Atalaya app for arm/disarm, live view, history, rule config.

---

## Roles a Node Can Play

A single phone can run more than one. Roles are pluggable.

| Role | What it does | Sensors used |
|------|--------------|--------------|
| **Camera node** | Vision watcher, Gemma 4 scene description, NL rule matching | Camera, mic |
| **Audio node** | Sound classification (glass break, gunshot, smoke alarm) | Mic |
| **Contact node** | Door/window open detection | Magnetometer + magnet, accelerometer |
| **Motion node** | Movement / vibration / accelerometer-triggered camera capture | Accel, camera |
| **Presence node** | Who's home — by phone WiFi/BLE detection | WiFi scanner, BT |
| **Keypad node** | Mounted by entrance, accepts arm/disarm via voice/NFC/face | Mic, NFC, camera |
| **Siren node** | High-volume alert announcements | Speaker, TTS |
| **Hub node** | Orchestration, storage, rule engine | Network |

---

## How AI Weaponizes This Beyond Just Vision

1. **Multi-modal confirmation** — camera says "person", audio confirms footsteps, motion confirms vibration. Only then alert. Single-sensor false positives die.
2. **Natural-language rules** — "Alert me if anyone except family enters the garage after 9 PM." Gemma understands context. No rules-engine programming.
3. **Voice + face for disarm** — passphrase + camera confirms it's you. Replaces keypad.
4. **Audio classifier replaces three sensors** — glass break detector, smoke alarm relay, baby monitor — all one mic with the right model.
5. **Tamper detection** — move the camera or cover it, instant alert via accelerometer + light sensor.
6. **AI-enhanced rule engine** — every event passes through a small reasoning step before alerting.

The AI isn't a feature — it's the glue that makes a multi-phone deployment behave like one smart system.

---

## Build Path (Phased)

### Phase 0 — Foundations
- Project name, license (AGPL-3 or dual), manifesto
- README with the SimpliSafe / Supabase comparison
- Architecture doc (this file, refined)
- Git repo

### Phase 1 — Single Node, Single Role
- Android Kotlin app with one role: camera node
- Local Gemma 4 E2B inference via llama.cpp / MediaPipe
- Simple natural-language rule engine
- Push notification to a designated phone
- **Ships standalone value as Atalaya v0.**

### Phase 2 — Hub + Multi-Node
- Hub Docker image
- Multiple nodes report to hub
- Multi-modal rule engine across nodes
- Web dashboard for hub config
- **Atalaya v1.**

### Phase 3 — Role Expansion
- Audio classification node
- Contact node (magnetometer)
- Motion node
- Presence node
- Keypad node (voice/NFC/face)
- Siren node

### Phase 4 — Control App
- Separate Android app for the homeowner
- Arm/disarm, live view, history, rule config
- iOS via shared backend (no node role on iOS — iOS doesn't allow always-on sensor access)

### Phase 5 — Cloud Hub Option
- Hosted hub at atalaya.dg or wherever
- Free tier, paid tier
- Self-host always available — same code

### Phase 6 — Hardware Integrations
- USB peripherals (NFC readers, sirens, smoke sensors)
- Z-Wave / Zigbee bridge
- Home Assistant integration

Each phase ships standalone value. No 18-month wait for a v1.

---

## Strategic Moves

1. **License: AGPL-3 (or dual AGPL + commercial).** Protects against cloud hijacking — competitors can't extract our code, host it, and not give back.
2. **Modular by role from day one.** Never one monolithic app. Roles are plugins. Community can add roles.
3. **Self-host first, cloud second.** One-line Docker compose. Self-hosters become evangelists. Cloud is the convenience tier.
4. **AI as glue, not the product.** Pitch isn't "we have AI." Pitch is "your old phones become a complete security system you own, free, private, smarter than SimpliSafe." AI just makes it work without false alarms.
5. **The community already exists.** Haven's 6800 stars, Frigate's user base, Termux power users, the "old phone homelab" crowd. Find them.
6. **Open the watcher early.** Public repo from week one. Build in public. Attracts contributors and validates demand.

---

## Honest Risks

1. **Liability** — security product failures = legal exposure. Clear DIY disclaimers. Don't market as a replacement for monitored alarms unless we get insurance + UL listing (real product money).
2. **Old phone reliability** — phones die, batteries swell. Node health monitoring is mandatory. A quietly-dead camera is worse than no camera.
3. **Battery damage from always-on charging** — power management with cycle charging, charge limits. Some phones support natively, others don't.
4. **AI false positives** — pets, family, deliveries. Confidence layer + learning loop required.
5. **Old Android version ceiling** — phones too old for Gemma fall back to non-AI roles (audio classifier with simpler models, contact sensor, etc.).
6. **Update mechanism for a security system** — Play Store, F-Droid, Docker pulls all need to be seamless.
7. **Connectivity assumptions** — Tailscale or WireGuard mesh. Cellular fallback for hub-to-node when WiFi dies.
8. **Privacy at the hub** — when the hub is in the cloud, users trust us. End-to-end encryption from node to control app, hub stores only encrypted blobs.

---

## Recommended First Step

Phase 0 plus Phase 1 spike on dg-core:

1. Spin up Gemma 4 E4B as a sandboxed dg-helm Docker service on dg-core. Hard CPU/memory limits so it can't starve other services.
2. Write the watcher loop in Python. Pull frames from a USB cam plugged into dg-core (or a phone via termux-camera-photo over SSH). Run them through Gemma. Run NL rules.
3. Wire alerts through dispatch (DG-internal) for testing.
4. Once the rule engine is real and the watcher works, port the loop to the phone — Kotlin app, local Gemma 4 E2B, same logic.

That's a real working demo without flashing a phone. From there, the phone port is mechanical.

---

## Competitive Landscape (Confirmed Empty Lane)

| Category | Examples | Where they fall short |
|----------|----------|----------------------|
| Phone-as-camera apps | Alfred, Manything, AtHome, AiCam | Cloud-bound, motion-only, AI is bolted-on premium |
| Server AI NVR | Frigate, Scrypted | Server-based, IP cameras, not phones |
| Server AI agentic NVR | DeepCamera | Closest by mission, but Mac Mini / AI PC, not phones |
| Phone-local AI utility | Off Grid, Google AI Edge Gallery | Chat/utility, not surveillance |
| Spiritual ancestor | Haven (Snowden, GPL-3) | Dormant since 2021, no AI, no fleet |

**Atalaya = DeepCamera's pattern + Off Grid's runtime + Haven's spirit, on phones, AGPL-licensed.**

---

## Open Questions

- Final name? Atalaya is a working name. Strong, mythological, evocative. Worth checking trademark/availability.
- Final license? AGPL-3 vs dual AGPL+commercial.
- Hub stack? Postgres + S3-compat object store + Go/Python API + WebSocket gateway? Or Supabase itself as the backbone?
- Mesh transport? Tailscale dependency vs built-in WireGuard mesh.
- Cellular fallback? Real or v2?
- Trademark check, domain registration, GitHub org name.

---

## Appendix: SimpliSafe Component Map

| SimpliSafe component | Atalaya node role | Notes |
|---------------------|-----------------|-------|
| Base station | Hub node | Docker container or another phone |
| Entry sensor | Contact node | Phone magnetometer + paired magnet |
| Motion sensor | Motion node | Phone accel + camera confirmation |
| Glass break sensor | Audio node | Phone mic + audio classifier |
| Indoor camera | Camera node | Phone camera + Gemma 4 |
| Outdoor camera | Camera node (weatherproofed) | Phone in a weatherproof case |
| Smoke/CO detector | Audio node + integration | Listens for existing detector beeps OR direct sensor integration via USB |
| Water leak sensor | External Z-Wave/Zigbee | Phase 6 |
| Keypad | Keypad node | Phone mounted by door |
| Panic button | Control app + voice | "Atalaya panic" voice command |
| Siren | Siren node | Phone speaker + amp |
| Cellular backup | Built-in | Every node has cellular |
| Battery backup | Built-in | Every node has a battery |
| Pro monitoring | Optional cloud add-on | Future paid tier |
