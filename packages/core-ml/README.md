# `packages/core-ml`

The inference abstraction. Pluggable backends for vision and text inference. Phase 1 ships the llama.rn JNI implementation.

**Phase:** 1 (current)
**Stack:** Kotlin (Android-targeting; native libs from llama.rn)
**Status:** 🔵 skeleton — ADR-0003 committed, no code yet

## What it owns

- `VisionInference` interface — `describe(jpegBytes, prompt) → String`
- `TextInference` interface — `judge(prompt, grammar) → String` (used by `core-rules`)
- llama.rn JNI binding (Phase 1's only impl, extracted from React Native)
- Model file management (download, integrity check, cache)
- Backend selection: NPU (Hexagon HTP) → GPU (Adreno OpenCL) → CPU fallback

## What it does NOT own

- The judge prompt or grammar (those live in `core-rules`)
- Frame capture (that's `core-sensors`)
- Choice of which model to use (that's a per-app setting; this module accepts the model file path)

## Dependencies

| On | Why |
|----|-----|
| `llama.rn` (extracted JNI) | Gemma 4 vision + text inference |
| `kotlinx.coroutines` | Async inference dispatch |

## Specs

- ADR (runtime selection): [`../../docs/phase-1/decisions/ADR-0003-inference-runtime.md`](../../docs/phase-1/decisions/ADR-0003-inference-runtime.md)
- Research note: [`../../docs/phase-1/research/M4-local-gemma.md`](../../docs/phase-1/research/M4-local-gemma.md)

## Consumers

| Module | Use |
|--------|-----|
| [`apps/node`](../../apps/node/) | Calls `visionInference.describe(...)` per captured frame |
| [`packages/core-rules`](../core-rules/) | Calls `textInference.judge(...)` per rule evaluation |

## Source layout (planned)

```
packages/core-ml/
├── README.md
├── build.gradle.kts
└── src/main/
    ├── kotlin/dev/digitalgnosis/atalaya/ml/
    │   ├── VisionInference.kt           # interface
    │   ├── TextInference.kt             # interface
    │   ├── llama/
    │   │   ├── LlamaSession.kt          # JNI wrapper
    │   │   ├── LlamaVisionInference.kt
    │   │   └── LlamaTextInference.kt
    │   └── ModelManager.kt              # download + integrity check
    └── jniLibs/                         # Hexagon HTP .so per arch
```

## Backend selection logic (planned)

```
on init:
  if Build.HARDWARE matches Snapdragon 8 Gen 1+:
    try Hexagon HTP backend
  if HTP unavailable AND OpenCL available:
    try Adreno OpenCL backend
  fall back to CPU
```

Documented per-device in the supported-hardware page.
