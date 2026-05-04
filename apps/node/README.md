# `apps/node`

The Atalaya watcher app. Runs on an Android phone, captures frames, runs Gemma 4 vision, evaluates rules, fires alerts.

**Phase:** 1 (current)
**Platform:** Android (Kotlin + Compose)
**Status:** 🔵 skeleton — no code yet, awaiting Step 3 vertical slice

## What it does

A configurable role daemon. Phase 1 ships only the **camera role**:

1. Foreground service starts when user taps "Arm"
2. CameraX captures a frame at the configured interval
3. Local Gemma 4 (via `core-ml`) describes the frame
4. `core-rules` evaluates the description against active rules
5. On match → `LocalNotificationTransport` fires a push to this same phone

Phase 3+ adds audio, contact, motion, presence, keypad, and siren roles to this same app.

## Direct dependencies

| Package | Why |
|---------|-----|
| [`packages/core-protocol`](../../packages/core-protocol/) | Wire types — `Observation`, `Alert`, `Rule` |
| [`packages/core-onboarding`](../../packages/core-onboarding/) | First-launch tutorial, contextual hints |
| [`packages/core-rules`](../../packages/core-rules/) | NL rule engine |
| [`packages/core-ml`](../../packages/core-ml/) | Gemma 4 inference |
| [`packages/core-sensors`](../../packages/core-sensors/) | Camera capture interface |
| [`packages/core-ui-base`](../../packages/core-ui-base/) | BaseViewModel |

## Specs

- Module-level: _(not yet drafted — will land in `docs/phase-1/modules/apps-node.md` during Step 2)_
- Architecture: [`../../ARCHITECTURE.md`](../../ARCHITECTURE.md)
- Goal & success criteria: [`../../docs/phase-1/01-goal.md`](../../docs/phase-1/01-goal.md)
- Roadmap: [`../../docs/phase-1/02-roadmap.md`](../../docs/phase-1/02-roadmap.md)
- Service strategy ADR: [`../../docs/phase-1/decisions/ADR-0007-min-sdk-and-service.md`](../../docs/phase-1/decisions/ADR-0007-min-sdk-and-service.md)

## Source layout (planned)

```
apps/node/
├── README.md
├── build.gradle.kts
├── src/main/
│   ├── AndroidManifest.xml
│   ├── kotlin/dev/digitalgnosis/atalaya/node/
│   │   ├── AtalayaApplication.kt          # @HiltAndroidApp
│   │   ├── di/                            # Hilt modules
│   │   ├── watcher/                       # Foreground service + capture loop
│   │   │   ├── WatcherService.kt          # Sticky FGS, ports IPNService shape
│   │   │   ├── CaptureScheduler.kt        # Interval scheduling
│   │   │   └── alerts/
│   │   │       └── LocalNotificationTransport.kt
│   │   ├── ui/
│   │   │   ├── armed/                     # Armed status screen
│   │   │   ├── rules/                     # Rule editor + suggestions
│   │   │   └── settings/
│   │   └── onboarding/                    # Tutorial content (uses core-onboarding)
│   └── res/
└── src/test/kotlin/
```

This layout is illustrative. The Gradle setup ships in this commit.
