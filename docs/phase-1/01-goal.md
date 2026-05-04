# Phase 1 — Goal & Success Criteria

## Goal

Ship a working Atalaya node app for a single Android phone running the **camera role**. End-to-end: phone takes a frame, Gemma 4 describes it, a natural-language rule decides whether to alert, and a notification fires.

## Success criteria

A reasonable user can:

1. Install the Atalaya node app on a supported Android phone (Pixel 6+ or equivalent, target Android 12+).
2. Grant camera, microphone, foreground service, and notification permissions through a guided flow.
3. Download the Gemma 4 E2B model and the multimodal projector inside the app on first launch.
4. Configure a single rule in plain English. Example: _"Alert me if there's a person visible after 9 PM"_ or _"Alert me if the front door is open"_.
5. Configure a frame interval (default determined after research, configurable 3–30s).
6. Configure a notification target (initially: this same phone, future: a designated control phone).
7. Tap "Start watching." The phone enters a foreground service mode with a persistent notification.
8. Lock the phone, set it down. The service continues running.
9. Trigger the rule (walk into frame, open the door). Receive a push notification on the configured target within ~10 seconds of the next sampled frame.
10. Stop the service from the persistent notification.

## Quality gates

- **Offline.** After initial model download, the app works with the phone in airplane mode (other than the FCM token which needs an outbound channel).
- **Battery-aware.** Documented expected power draw at default interval. Charge-aware throttling lands in v1.1 if not v1.0.
- **No data leaves the device** (Phase 1) — frames stay local, descriptions stay local, only push notifications cross the network.
- **Survives Android's death squad.** Foreground service with `dataSync` or `camera` type. No phantom kills after 30 minutes.
- **Reproducible build.** Anyone with the repo can build the same APK byte-for-byte (best effort within Android tooling).

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
