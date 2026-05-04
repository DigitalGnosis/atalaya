---
adr: 0003
title: Inference runtime — llama.rn JNI extracted (Gemma 4 E2B Q4_K_M + F16 mmproj)
status: stale (pivot in progress)
date: 2026-05-04
---

> ## ⚠️ STALE — pivot in progress
>
> This ADR placed inference on the phone via llama.rn JNI. That's wrong — old phones can't run Gemma 4 well. Pivoting to **hub-side inference**. New ADR will supersede this one once research lands. See [`HANDOFF.md`](../../../HANDOFF.md) and [`../research/PIVOT-RESEARCH.md`](../research/PIVOT-RESEARCH.md). **Do not act on this ADR.**

# ADR-0003 — Inference runtime

## Context

Atalaya's camera role runs Gemma 4 vision on-device. We need to pick a runtime. M4 research ([`../research/M4-local-gemma.md`](../research/M4-local-gemma.md)) compared three candidates:

1. **llama.cpp Android JNI** (the official `examples/llama.android` sample) — universal but **text-only** in the sample. Vision via `libmtmd` is not exposed; we'd write the JNI ourselves. ~1-2 weeks of native work before any product code.
2. **Off Grid Mobile AI / `llama.rn`** — already wires Gemma 4 vision with Hexagon HTP and Adreno OpenCL. MIT license. The catch: it's a React Native binding. We can extract the JNI without RN.
3. **Google AI Edge Gallery / LiteRT-LM** — cleanest Kotlin API, official Google sample, Apache-2.0. The catch: proprietary `.litertlm` model format; LiteRT gallery sets an 8 GB RAM floor that excludes the 4-6 GB old phones at the heart of our value proposition.

Two earlier assumptions corrected by M4:
- "Always BF16 mmproj" is wrong on Android. Some Adreno OpenCL paths lack a BF16 cast op; **F16 mmproj is the Android default**. Off Grid's model picker actively excludes BF16 mmproj.
- The official llama.cpp Android sample has no vision. Picking "raw llama.cpp" front-loads weeks of mmproj JNI work.

## Decision

We will use **llama.rn extracted to a pure Kotlin/JNI binding** as Atalaya's inference runtime. Specifically:

- Source: `mybigday/llama.rn` (MIT) at the latest stable commit
- Extraction: take the native code (`llama.rn/cpp/`) and the JNI bridge, drop the React Native bridge layer, expose a Kotlin `LlamaSession` API directly
- Model: `unsloth/gemma-4-E2B-it-GGUF`, **Q4_K_M** main + **F16 mmproj** (not BF16 — Android Adreno compatibility)
- Acceleration: Hexagon HTP on Snapdragon 8 Gen 1+, Adreno OpenCL on older Snapdragon, Vulkan/CPU fallback elsewhere
- Min SDK driven by this choice: 24 (Android 7) inherited from llama.rn — see ADR-0007

## Alternatives considered

- **LiteRT-LM** — cleanest API but the 8 GB RAM floor is a non-starter; old Pixel 4a / Samsung A-series have 4-6 GB. Also `.litertlm` is proprietary, locking us to whatever litert-community publishes.
- **Raw llama.cpp + write our own mmproj JNI** — universal and clean long-term, but 1-2 weeks of work that doesn't get us to a product faster. Defer to v2 if we want to escape llama.rn.
- **MediaPipe LLM Inference** — abandoned mid-research; LiteRT-LM is the successor and inherits the same `.task`/`.litertlm` issues.
- **Cloud inference (Gemma via API)** — explicitly rejected. Atalaya's pitch is on-device privacy. Cloud is not a Phase 1 option.

## Consequences

**Easier:**
- Vision works today. We don't write JNI ourselves.
- GGUF is portable — switching quantizations or models later is a file swap.
- NPU/GPU/CPU fallback is already wired and tested by Off Grid against multiple Snapdragon and Tensor chipsets.

**Harder:**
- llama.rn ships ~30 MB of Hexagon `.so` files per HTP version (v69-v81). Our APK is bigger.
- Maintenance: we track upstream `llama.rn` releases. They're active (commits today) but the project has a small maintainer surface compared to llama.cpp itself.
- One known hardware exclusion: Snapdragon 7 Gen 1 SM7450 (llama.rn issue #279). Document on the supported-device page.

**New constraints:**
- F16 mmproj only. Q4 mmproj for Gemma 4 is not yet shipped by `ggml-org` and is risky on Android. Lock the model picker.
- `core-ml` package wraps llama.rn behind an `Inference` interface — Phase 1 ships the llama.rn implementation; v2 alternatives (MediaPipe, raw llama.cpp, server-hosted) slot in without changing call sites.
- We commit to E2B for v1. E4B is documented but not the default — left as a setting for users on 8 GB+ devices once they validate description quality.

## Related

- Supersedes: none
- Superseded by: none
- Informed by: [`../research/M4-local-gemma.md`](../research/M4-local-gemma.md)
- Triggers: ADR-0004 (frame interval — needs real benchmarks), ADR-0007 (min SDK), `packages/core-ml/` skeleton in roadmap Step 2
- License lineage: MIT (llama.rn) + MIT (llama.cpp) → AGPL-3.0 combined work (compatible). Attribution headers preserve MIT origin.
