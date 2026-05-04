---
module: M4
title: Local Gemma 4 inference on Android
status: stub
opened: 2026-05-04
time_box_ends: 2026-05-07
sources:
  - https://github.com/alichherawalla/off-grid-mobile-ai
  - https://github.com/google-ai-edge/gallery
  - https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md
  - https://huggingface.co/blog/gemma4
related_adrs:
  - ADR-0003
  - ADR-0004
  - ADR-0007
---

# M4 — Local Gemma 4 on Android

## Why this matters

The whole product hinges on Gemma 4 vision running on-device with usable latency and battery. Three runtime candidates exist; we need to pick one and know its costs. The decision drives the inference module's API, the model file management strategy, and the minimum supported chipset.

## Specific questions

1. **Runtime comparison.** Off Grid's runtime, Google AI Edge Gallery (LiteRT), and llama.cpp Android JNI — performance, ease of integration, GGUF support, mmproj support, NPU/GPU acceleration, license, dependency footprint?
2. **Model variant.** E2B (1.5GB) or E4B (4-5GB) for v1? What's the description quality delta on a real-world frame?
3. **Quantization.** Q4 main model + BF16 mmproj is the documented working combo. Confirm.
4. **Inference latency** on a Pixel 6 (Tensor G1) for a 768x768 frame? On a Pixel 9 (Tensor G4)? On a Snapdragon 8 Gen 1?
5. **Memory pressure.** Peak RSS during inference. Risk of OOM in the foreground service when other apps are also memory-pressing.
6. **Battery cost** per inference. Roughly: how many frames per percent of battery on a 5000mAh phone?
7. **NPU acceleration.** QNN on Snapdragon 8 Gen 1+, Adreno via OpenCL, CPU fallback — Off Grid documents this. What's the actual delta?
8. **Model download.** Hugging Face URL pattern, file integrity (sha256), retry logic. We don't bundle the model in the APK.
9. **Multimodal projector pattern.** mmproj is sensitive to quantization — Q4/Q8 mmproj produces garbage. Confirm we always pull BF16 mmproj.
10. **Inference API shape.** What does the call site look like? `gemma.describe(jpegBytes, prompt: "Describe this scene") → String`?

## Findings

_(fill in)_

## Benchmark table (template — fill once measured)

| Phone | Chipset | Runtime | Frame size | Latency | RAM peak | Battery /hr (5s interval) |
|-------|---------|---------|-----------|---------|----------|---------------------------|
| Pixel 6 | Tensor G1 | _(TBD)_ | 768x768 | _(TBD)_ | _(TBD)_ | _(TBD)_ |
| Pixel 9 | Tensor G4 | _(TBD)_ | 768x768 | _(TBD)_ | _(TBD)_ | _(TBD)_ |

## Tried that didn't work

_(log dead ends)_

## Recommendation

_(one paragraph, informs ADR-0003 / ADR-0004 / ADR-0007)_
