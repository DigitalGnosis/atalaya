# `packages/core-sensors`

Sensor abstraction interfaces. Implementations live in role-specific app modules.

**Phase:** 1 (current — camera only)
**Stack:** Kotlin (Android-targeting, but interfaces are abstract)
**Status:** 🔵 skeleton — interfaces defined here, implementations in `apps/node`

## What it owns

- `CameraSensor` interface — `captureFrame(): JpegBytes`
- Sensor type enum and metadata
- Phase 3 adds: `AudioSensor`, `ContactSensor`, `MotionSensor`, `PresenceSensor`

## What it does NOT own

- Concrete CameraX implementation (lives in `apps/node` because it's Android-specific)
- Sensor lifecycle within a service (that's the watcher's job)

## Dependencies

| On | Why |
|----|-----|
| `kotlinx.coroutines` | Suspend functions for capture |
| `kotlinx.datetime` | Timestamps |

This module is intentionally interface-heavy. Implementations stay in apps so platform-specific code (CameraX, MediaRecorder, magnetometer) doesn't bleed into the shared package layer.

## Specs

- Research stub (Phase 1): [`../../docs/phase-1/research/M3-camerax.md`](../../docs/phase-1/research/M3-camerax.md)

## Consumers

| Module | Use |
|--------|-----|
| [`apps/node`](../../apps/node/) | Implements `CameraSensor` via CameraX, calls `captureFrame()` from the watcher |

## Source layout (planned)

```
packages/core-sensors/
├── README.md
├── build.gradle.kts
└── src/main/kotlin/dev/digitalgnosis/atalaya/sensors/
    ├── CameraSensor.kt           # interface
    ├── SensorMetadata.kt
    └── (Phase 3+: AudioSensor.kt, ContactSensor.kt, etc.)
```
