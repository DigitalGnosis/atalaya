# Phase 1 — Goal & Success Criteria

## Goal

Ship a working Atalaya node app for a single Android phone running the **camera role**. End-to-end: phone takes a frame, Gemma 4 describes it, a natural-language rule decides whether to alert, and a notification fires.

## Target user

**Normie.** Someone who has never used Tailscale, has never seen a Docker compose, and just wants to install an APK and have their old phone watch the front door. The first dogfood test user is the founder; the intended audience is anyone. Polish targets are written for the normie path. Power-user features (rule editing in raw form, model file swaps, debug toggles) are reachable but not surfaced by default.

## Success criteria

A reasonable user — no technical background — can:

1. Install the Atalaya node app from a sideloaded APK or F-Droid (Play Store later).
2. **First-launch tutorial walks them through** what the app does and what permissions are about to be asked for. Skippable for power users (single setting toggle).
3. Grant camera, microphone, foreground service, and notification permissions through a **guided flow with explanatory screens** ("why we need camera", "why we need always-on") — not raw runtime prompts.
4. **Download the Gemma 4 E2B model in-app** with a progress UI on first launch. Not adb-push, not "go grab files from a Hugging Face URL." Tap, wait, watch the bar fill.
5. Pick a rule from a **pre-built suggestions list** ("alert me when a person is at the front door") with one-tap accept, OR write their own in plain English.
6. Choose a frame interval via a slider (default determined after research, three labeled presets: Battery saver / Balanced / Responsive).
7. Tap "Start watching." The phone enters a foreground service mode with a clear persistent notification ("Atalaya is armed — tap to stop").
8. Lock the phone, set it down. The service continues running.
9. Trigger the rule (walk into frame, open the door). Receive a push notification on this same phone within ~10 seconds of the next sampled frame.
10. Tap the alert to see what was caught (frame thumbnail, description, time).
11. Mark the alert as ✓ "real" or ✗ "false alarm" with one tap (feedback for v1.1's learning loop).
12. Stop the service from the persistent notification.

A normie should never have to:

- Open a terminal
- Edit a config file
- Read documentation to install
- Understand what a foreground service is
- Know what a GGUF file is
- Configure FCM, Tailscale, or any networking

## Quality gates

- **Offline.** After initial model download, the app works with the phone in airplane mode. Phase 1 has no network transport — local notifications only (per ADR-0006).
- **Battery-aware.** Documented expected power draw at default interval. Charge-aware throttling lands in v1.1 if not v1.0.
- **No data leaves the device** (Phase 1) — frames stay local, descriptions stay local, alerts are local.
- **Survives Android's death squad.** Foreground service with `camera|microphone` type per ADR-0007. No phantom kills after 30 minutes.
- **Reproducible build.** Anyone with the repo can build the same APK byte-for-byte (best effort within Android tooling).
- **Normie-installable.** A user with no technical background can install, configure one rule, and arm the watcher in under 10 minutes from first tap.
- **Uninstall is clean.** Reinstalling later doesn't carry confusing state from a previous install.

## What we measure

| Metric | Target | Why |
|--------|--------|-----|
| Cold-start to first description | ≤ 10s | Onboarding feel |
| Frame sample → rule decision | ≤ 5s on Pixel 6 | UX of "real" alerts |
| Battery drain at 5s interval | ≤ 8%/hr on Pixel 6 | Always-on viability |
| Foreground service uptime | ≥ 24h continuous | Reliability |
| False positive rate (curated test set) | ≤ 10% | User trust |
| APK size | ≤ 50MB (model downloaded separately) | F-Droid friendliness |

Targets refined after research. Numbers above are starting hypotheses, not commitments.

## Out of scope (this phase)

See [Phase 1 README → Out of scope](README.md#phase-1-out-of-scope-deferred-to-later-phases).
