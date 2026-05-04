---
adr: 0007
title: Min SDK 26, target SDK 35 — sticky foreground service with camera|microphone type
status: accepted
date: 2026-05-04
---

# ADR-0007 — Min SDK + foreground service strategy

## Context

Atalaya's camera role runs an always-on foreground service that captures frames, runs Gemma vision, evaluates rules, and fires notifications. M2 research ([`../research/M2-background-service.md`](../research/M2-background-service.md)) confirmed this is the right primitive (not WorkManager — its 15-minute periodic floor disqualifies it for our 5-second cadence). M4 research ([`../research/M4-local-gemma.md`](../research/M4-local-gemma.md)) put a floor on inference runtime requirements.

Two questions to commit:

1. What SDK range supports both the inference runtime and the modern foreground-service API surface?
2. What does the watcher service look like — type, lifecycle, kill detection?

## Decision

### SDK range

- **Min SDK: 26 (Android 8 / Oreo).** This is Tailscale Android's floor and matches llama.rn's lower bound when extracted (RN binding's 24 was conservative). It loses pre-Oreo phones but those are out of our target audience anyway — we want phones from the last decade, not antiques.
- **Target SDK: 35 (Android 15).** Required for Play Store compliance through 2026 and unlocks the modern foreground-service-type API surface.
- **Compile SDK: 35.**
- **NDK:** 27.x (matching llama.rn's tested setup)

### Foreground service shape

A single sticky `Service` (not VPN, not JobIntentService), declaring `foregroundServiceType="camera|microphone"`, lifecycle modeled after Tailscale's `IPNService` (modern reference, BSD-3-Clause) with the conceptual shape ported from Haven's `MonitorService` (GPL-3.0).

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />

<service
    android:name=".watcher.WatcherService"
    android:exported="false"
    android:foregroundServiceType="camera|microphone" />
```

Service contract:
- `startForeground(id, notification, FOREGROUND_SERVICE_TYPE_CAMERA or FOREGROUND_SERVICE_TYPE_MICROPHONE)`
- `PARTIAL_WAKE_LOCK` only (NOT Haven's deprecated `FULL_WAKE_LOCK`)
- `START_STICKY` from `onStartCommand`, with explicit handling of the null-intent restart case (port from Tailscale's IPNService)
- Persistent visible notification (NOT Haven's IMPORTANCE_MIN — users need to know the watcher is armed)
- `WatcherService` checks user-armed-state from persisted prefs on null-intent restart; if armed, re-establish, else `START_NOT_STICKY`

### Critical Android 15 constraint

A camera FGS cannot be created from the background. **Auto-arm-on-boot is not supported.** Arm flow:

1. User opens app → taps "Arm" → service starts foreground → user can leave the app
2. Re-arming after a kill: notification action ("Tap to resume watching"), or push from a Phase 2+ companion device

Document this prominently in the README. It's a structural Android limitation, not a bug.

### OEM kill mitigation

OEM kill (Xiaomi, Samsung, OnePlus) has no programmatic fix in 2026. Onboarding flow includes a `Build.MANUFACTURER`-aware screen that deep-links to the right Settings activity (autostart, battery-optimization opt-out, lock-in-recents). Use the `dontkillmyapp.com` Intent map. Require `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` on all devices as part of the arm flow to dodge doze.

### Kill detection

Android offers no callback when the OS kills our service. Detection is reconstructive: a 15-minute WorkManager watchdog compares a persisted heartbeat against `now`. If the heartbeat is stale and the user-armed flag is set, post a "Tap to resume watching" notification.

## Alternatives considered

- **Min SDK 24 (Android 7).** Pros: covers older phones. Cons: pre-doze foreground-service quirks, more compatibility branches, slightly older notification API. Rejected — net negative for marginal device coverage.
- **Min SDK 31 (Android 12).** Pros: matches LiteRT-LM. Cons: excludes a significant chunk of working old phones (Pixel 3/4, mid-range Samsungs from ~2019). Rejected.
- **WorkManager periodic.** Pros: OS handles scheduling, battery optimizations baked in. Cons: 15-minute floor on periodic workers. Rejected — fundamental incompatibility with our cadence.
- **JobScheduler.** Pros: tighter than WorkManager. Cons: still subject to doze, no foreground-service guarantees. Rejected.
- **Two services (one camera, one mic).** Pros: cleaner separation. Cons: more lifecycle complexity, doesn't match the unified watcher concept. Rejected — single service with multi-type FGS is the modern pattern.

## Consequences

**Easier:**
- Modern Android API surface (FGS types, runtime permissions, scoped storage).
- Single service to debug, monitor, and reason about.
- Tailscale's IPNService is a clean port for the lifecycle skeleton.

**Harder:**
- Auto-arm-on-boot impossible. Need user-friendly "tap to resume" flow.
- OEM kill onboarding is per-vendor — Xiaomi flow ≠ Samsung flow ≠ OnePlus flow. We maintain a vendor map.
- Must support API 26 → API 35 — that's nine API levels of compatibility branches in a few places.

**New constraints:**
- Pre-Oreo phones unsupported. Document.
- All FGS-related code paths must check `Build.VERSION.SDK_INT` for the `startForeground(..., type)` overload (added in API 29).
- Heartbeat persistence required for kill detection.
- Notification cannot be dismissable while service is armed — `setOngoing(true)` + `setForegroundServiceBehavior` for Android 12+.

## Related

- Supersedes: none
- Superseded by: none
- Informed by: [`../research/M2-background-service.md`](../research/M2-background-service.md), [`../research/M4-local-gemma.md`](../research/M4-local-gemma.md)
- Triggers: `apps/node/` watcher service skeleton in roadmap Step 2; ADR-0008 (DI — Hilt is the wiring mechanism)
- License lineage: Haven (GPL-3.0) and Tailscale Android (BSD-3-Clause) both compatible with AGPL-3.0; ported portions get attribution headers
