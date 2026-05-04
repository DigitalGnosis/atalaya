---
module: M4
title: Local Gemma 4 inference on Android
status: findings
opened: 2026-05-04
time_box_ends: 2026-05-04
sources:
  - https://github.com/alichherawalla/off-grid-mobile-ai
  - https://github.com/google-ai-edge/gallery
  - https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md
  - https://github.com/ggml-org/llama.cpp/blob/master/docs/multimodal.md
  - https://huggingface.co/blog/gemma4
  - https://huggingface.co/ggml-org/gemma-4-E2B-it-GGUF
  - https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm
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

### Repo state at investigation time

| Repo | Latest commit | License | Stack |
|---|---|---|---|
| `ggml-org/llama.cpp` | `eff06702` 2026-05-04 | MIT | C/C++ + JNI sample |
| `alichherawalla/off-grid-mobile-ai` | `e3957480` 2026-05-04 | MIT | React Native + `llama.rn` |
| `google-ai-edge/gallery` | `3b368f4a` 2026-04-24 | Apache-2.0 | Kotlin + LiteRT-LM (`com.google.ai.edge.litertlm:litertlm-android:0.10.0`) |

All three are AGPL-3.0 compatible (MIT and Apache-2.0 are permissive and freely combinable into AGPL).

### 1 — Runtime comparison

| Dimension | llama.cpp Android JNI (`examples/llama.android`) | Off Grid Mobile AI (`llama.rn`) | Google AI Edge Gallery (LiteRT-LM) |
|---|---|---|---|
| Integration ease (for native Android) | Low — sample is a Kotlin lib + `cpp` source you compile yourself; no published artifact | Medium — but only as a React Native binding (`llama.rn`); we'd port the Kotlin call shape, not adopt RN | High — single Maven dep `com.google.ai.edge.litertlm:litertlm-android:0.10.0`, official Apache-2.0 sample |
| GGUF support | Yes, native | Yes, native (uses llama.cpp under the hood) | **No.** Uses proprietary `.litertlm` / `.task` bundle format |
| mmproj / vision support | **Not in the sample.** `InferenceEngine` exposes only `sendUserPrompt(message: String)` (`examples/llama.android/lib/src/main/java/com/arm/aichat/InferenceEngine.kt:31`). No JNI binding for `libmtmd`. We'd have to write our own. | Yes — `context.initMultimodal({ path: mmProjPath, use_gpu })` and image attachments. Vision proven on Gemma 4 E2B/E4B (`src/constants/models.ts:19,29`, `src/services/llmHelpers.ts:255`). | Yes — bitmap input via `Content.ImageBytes` with separate `visionBackend` (`LlmChatModelHelper.kt:85-94, 271-281`). |
| NPU path | Available via llama.cpp Hexagon (HTP) backend, but the sample does not surface a NPU device flag | **Yes — Hexagon HTP** via `devices: ['HTP0']`, ships Hexagon libs `v69/v73/v75/v79/v81` in `android/app/src/main/assets/ggml-hexagon/` (`docs/PERFORMANCE_IMPROVEMENTS.md`). **OpenCL/Adreno** also wired. Whitelist Snapdragon 8 Gen 1+; **excludes Snapdragon 7 Gen 1 SM7450** (llama.rn issue #279). | Yes — `Backend.NPU(nativeLibraryDir = ...)` for QNN/AI-core via LiteRT delegate; `Backend.GPU()` for OpenCL/Vulkan. Selected per-modality (text vs vision can use different backends). |
| GPU path | Vulkan/OpenCL build flags supported in CMake | OpenCL on Adreno (text); CLIP vision is **CPU-only on Android** today (iOS gets Metal CLIP) | GPU delegate works; vision recommended on GPU for Gemma 4 |
| CPU acceleration | SME2 (Arm) / AMX runtime detection | Same (inherited from llama.cpp); 6 threads on Android default | Hand-tuned by Google for Tensor / Snapdragon; opaque |
| Min SDK | **33** (Android 13) — see `examples/llama.android/lib/build.gradle.kts:13` | **24** (Android 7) — `android/build.gradle:4` | **31** (Android 12) — `Android/src/app/build.gradle.kts:37` |
| NDK version | 29.0.13113456 | 27.1.12297006 | n/a (no NDK code; AAR only) |
| Native footprint | 1 `.so` we'd produce + GGUF model (text + image as we wire it) | ~30+ MB of Hexagon `.so` per HTP version + `librnllama.so` + GGUF model | LiteRT-LM AAR (~tens of MB) + `.litertlm` model (~2.6 GB for E2B vision) |
| Model artifact | GGUF (open) | GGUF (open) | `.litertlm` proprietary bundle |
| Last commit | 2026-05-04 (today) | 2026-05-04 (today) | 2026-04-24 (~10 days ago) |
| License compatibility w/ AGPL-3.0 | Compatible | Compatible | Compatible |
| Risk of vendor lock-in | None — we can swap quantizations and forks at will | Low — RN dependency is the awkward bit, but the GGUF format is portable | **High** — `.litertlm` is Google-only; we'd be tied to whatever `litert-community` publishes. Also Google's app analytics (Firebase, FCM, Analytics) are baked into the sample. |

**Key insight:** the official llama.cpp Android sample (`examples/llama.android`) is **text-only**. To get Gemma 4 vision through llama.cpp on Android, we either (a) extend the JNI to bridge `libmtmd` ourselves, or (b) use `llama.rn` which already does this. There is no shrinkwrapped third option for direct GGUF + mmproj on Android Kotlin.

### 2 — Model variant for v1

The blog (`huggingface.co/blog/gemma4`) gives:

| Variant | Effective params | Total params | Audio | Context |
|---|---|---|---|---|
| **E2B** | 2.3B | 5.1B | yes | 128k |
| **E4B** | 4.5B | 8B | yes | 128k |
| 26B A4B | 4B active | 26B total | no | 256k |
| 31B | 31B | dense | no | 256k |

Real on-disk sizes from `ggml-org/gemma-4-E2B-it-GGUF`:

- `gemma-4-E2B-it-Q8_0.gguf` — 4.97 GB
- `gemma-4-E2B-it-bf16.gguf` — 9.31 GB
- `mmproj-gemma-4-E2B-it-Q8_0.gguf` — 557 MB
- `mmproj-gemma-4-E2B-it-bf16.gguf` — 987 MB

Note: `ggml-org` only publishes Q8_0 and BF16 for Gemma 4. **`unsloth/gemma-4-E2B-it-GGUF`** (used by Off Grid) ships the broader Q4_K_M / Q5_K_M / Q6_K range plus matching mmproj quants — that's what we want for ~1.5 GB main model files. Off Grid sets `minRam: 4 GB` for E2B and `minRam: 6 GB` for E4B (`src/constants/models.ts:23,33`); LiteRT gallery sets `minDeviceMemoryInGb: 8` for E2B (`model_allowlists/1_0_11.json`) — Google's threshold is more conservative.

**Recommendation: E2B Q4_K_M for v1.** Old Pixel 6 has 8 GB RAM; Pixel 4a / 5 / Samsung A-series have 4–6 GB. E2B Q4_K_M (~1.5 GB main + ~600 MB mmproj F16/BF16) fits in 4 GB phones with the foreground service overhead. E4B is a v2 setting for 8 GB+ devices once we confirm description quality is the bottleneck — most security-camera prompts ("describe the scene, list people / animals / vehicles") don't need 4.5B params.

### 3 — Quantization (the BF16-mmproj question)

The user's prior assumption was **"Q4 main + BF16 mmproj, Q4 mmproj produces garbage."** What I actually found:

- **`ggml-org` (canonical llama.cpp upstream) ships only Q8_0 and BF16 mmproj for Gemma 4** (`huggingface.co/ggml-org/gemma-4-E2B-it-GGUF`). They do not publish a Q4 mmproj at all. That is itself a strong signal — they trust BF16, accept Q8_0, and don't risk Q4 on the vision projector.
- **llama.cpp's own multimodal example uses Q4_K_M mmproj** for Gemma 3 (`docs/multimodal.md:28`: `--mmproj mmproj-gemma-3-4b-it-Q4_K_M.gguf`). So Q4_K_M mmproj is not categorically broken — but Gemma 3 ≠ Gemma 4, and ggml-org chose not to ship Q4 for Gemma 4.
- **Off Grid explicitly avoids BF16 mmproj** at the matcher level: `src/services/huggingface.ts:166-172` — `// Fallback: prefer F16/FP16, exclude BF16 (can be incompatible with some runtimes)`. Their position is the **opposite** of the assumption: they treat **BF16 as the risk**, not Q4. Reason: some llama.rn / OpenCL paths don't have a BF16 cast op and produce garbage.

**Resolution:** the right pairing depends on the runtime. For llama.cpp/llama.rn on Android, the empirically safe pairing is **main Q4_K_M + mmproj F16** (matching the off-grid policy). On runtimes that don't lower BF16 well (Adreno OpenCL CLIP), F16 mmproj is the conservative choice. **BF16 mmproj is correct only when the runtime has BF16 support end-to-end**, which is not guaranteed on Android GPU/NPU paths today.

So the previous "always pull BF16 mmproj" rule is **wrong on Android**. Pull F16 mmproj from `unsloth/gemma-4-E2B-it-GGUF` (which publishes both); fall back to BF16 only on backends that demonstrably handle it. **This is a finding to flag in ADR-0003.**

### 4 — Inference latency on a 768×768 frame

No first-party numbers in any of the three repos for Gemma 4 specifically — both the gallery README and the Hugging Face Gemma 4 blog are silent on per-device benchmarks. The off-grid `PERFORMANCE_IMPROVEMENTS.md` discusses HTP/OpenCL paths qualitatively but does not publish tok/s figures. Our own benchmarking will produce these numbers — we cannot ship a runtime decision pretending we know them.

What we *can* say from the architecture:

- **Pixel 6 (Tensor G1)** — no QNN/HTP, no NNAPI vendor delegate that helps Gemma 4. CPU-only via llama.cpp on the 4 Cortex-A76+A55 cluster. Expectation: 3–6 tok/s decode for E2B Q4_K_M at text. Vision prefill on a 768×768 frame: tens-of-seconds class. **TBD** until measured.
- **Pixel 9 (Tensor G4)** — TPU/EdgeTPU is closed to general LiteRT use; effectively CPU for our path. ~1.5–2× Pixel 6. **TBD.**
- **Snapdragon 8 Gen 1+ (SM8450+)** — Hexagon HTP via llama.rn `devices: ['HTP0']` is the speedup lever; community reports on llama.cpp issue tracker suggest 2–4× over CPU for prefill on text. Vision (CLIP) still CPU-only on Android in off-grid today. **TBD.**

This is a benchmark table to fill, not a decision input we have today. Do not block ADR-0003 on these numbers; do block ADR-0004 (frame interval default) on them.

### 5 — Memory pressure

Heuristic from llama.cpp / llama.rn: peak RSS ≈ **1.5–2× model file size** during prefill (model + KV cache + CLIP activations + ggml scratch). For E2B Q4_K_M:

- Main model file: ~1.5 GB
- mmproj F16: ~600 MB
- KV cache (4096 ctx, F16): ~150 MB
- CLIP activations + scratch: ~300–500 MB
- **Estimated peak RSS: 2.5–3.0 GB**

On a 4 GB device this is the OOM danger zone — the foreground service is competing with system + camera + alerter. On 6 GB+ this is comfortable. Off-grid's `minRam: 4 GB` for E2B works because it's a foreground app, not a background service; **Atalaya's foreground service profile is tighter, so set the practical floor at 6 GB.**

LiteRT-LM gallery's E2B `.litertlm` is 2.58 GB on disk — comparable peak RSS, but Google sets `minDeviceMemoryInGb: 8`, suggesting their internal memory measurements are higher than the GGUF path.

### 6 — Battery cost

**No documented numbers in any source.** Anecdotal frame from the architecture:

- A single E2B Q4_K_M vision pass on CPU at ~5 tok/s prefill on a 768² image (≈250 image tokens + ~150 prompt tokens + ~50 output tokens) is ~2–4 seconds wall-clock and probably 3–6 J on a Pixel 6 class CPU.
- A 5000 mAh phone at 3.85 V holds ~70 kJ. 1% = 700 J. So **~150–200 inferences per battery percent on CPU**, before idle drain.
- HTP path on Snapdragon 8 Gen 1+ would roughly double this (faster + lower duty cycle).
- 5 s frame interval = 12 inferences/min = 720/hr → **~3–5 %/hr battery cost just for inference**, before camera, screen-off doze, etc.

These are envelope estimates, not measured. They suggest the **default frame interval should be 5 seconds, not 1 second** — a 1 s loop would burn ~15–25 %/hr.

### 7 — Inference API shape

**llama.cpp Android JNI sample** (`examples/llama.android/lib/src/main/java/com/arm/aichat/InferenceEngine.kt:31`):

```kotlin
// Text-only — no image input in the official sample.
val engine: InferenceEngine = AiChat.getInferenceEngine(context)
engine.loadModel("/data/.../gemma-4-E2B-it-Q4_K_M.gguf")
engine.setSystemPrompt("You are a security camera describing a scene.")
val tokens: Flow<String> = engine.sendUserPrompt(
    message = "Describe this image.",
    predictLength = 256,
)
tokens.collect { print(it) }
// To add vision: write our own JNI around libmtmd. ~1–2 weeks of work.
```

**Off Grid via `llama.rn`** (`src/services/llm.ts:91, 159, 221`):

```typescript
// Load main model + mmproj, then send a multimodal message.
await llmService.loadModel(
    "/data/.../gemma-4-E2B-it-Q4_K_M.gguf",
    "/data/.../mmproj-gemma-4-E2B-it-F16.gguf",
);
// llama.rn under the hood: context.initMultimodal({ path: mmProjPath, use_gpu })
const messages = [{
    role: "user",
    content: "Describe this scene.",
    attachments: [{ type: "image", uri: "file:///cache/frame.jpg" }],
}];
await llmService.generateResponse(messages, (token) => process.stdout.write(token.content));
```

To use this from native Kotlin, we'd build directly against `mybigday/llama.rn`'s JNI layer (same Hexagon `.so` set, same `initMultimodal` JNI call) and skip React Native — or fork their JNI as our own AAR.

**Google AI Edge Gallery (LiteRT-LM)** (`LlmChatModelHelper.kt:110-153, 247-306`):

```kotlin
val engine = Engine(EngineConfig(
    modelPath = "/data/.../gemma-4-E2B-it.litertlm",
    backend = Backend.GPU(),                              // CPU / GPU / NPU
    visionBackend = Backend.GPU(),                        // separate per-modality
    audioBackend = null,
    maxNumTokens = 4000,
))
engine.initialize()
val convo = engine.createConversation(ConversationConfig(
    samplerConfig = SamplerConfig(topK = 64, topP = 0.95, temperature = 1.0),
))
val contents = mutableListOf<Content>(
    Content.ImageBytes(bitmap.toPngByteArray()),          // PNG bytes, not JPEG
    Content.Text("Describe this scene."),
)
convo.sendMessageAsync(Contents.of(contents), object : MessageCallback {
    override fun onMessage(m: Message) { partial(m.toString()) }
    override fun onDone() { done() }
    override fun onError(t: Throwable) { fail(t) }
}, emptyMap())
```

For Atalaya the desired call site `gemma.describe(jpegBytes, prompt) -> String` is most easily synthesized over **llama.rn JNI** or **a custom libmtmd JNI** — both are Kotlin-native, both keep us on GGUF, both stay open. The LiteRT-LM call site is also clean but ties us to `.litertlm`.

### 8 — Model download

Hugging Face URL pattern (verified on the live repos):

```
https://huggingface.co/{org}/{repo}/resolve/main/{filename}
```

Concrete examples for our pick:

```
https://huggingface.co/unsloth/gemma-4-E2B-it-GGUF/resolve/main/gemma-4-E2B-it-Q4_K_M.gguf
https://huggingface.co/unsloth/gemma-4-E2B-it-GGUF/resolve/main/mmproj-gemma-4-E2B-it-F16.gguf
```

Off Grid scans the HF API at `https://huggingface.co/api/models/{repo}/tree/main` (`src/constants/index.ts`, `src/services/huggingface.ts`) to enumerate files and sizes, then issues background downloads with resume support (`src/services/modelManager/download.ts`). HF returns a `lfs.size` and an etag (sha256-compatible); we should check the etag against a known-good value or, at minimum, record sha256 on first successful run. No source documents canonical sha256 values for the `unsloth` Gemma 4 quants — we'd compute on first download and pin in the app config from then on.

### 9 — NPU acceleration

Three concrete paths surface in the repos:

- **Hexagon HTP (Snapdragon 8 Gen 1+, SM8450+)** via llama.cpp's Hexagon backend, exposed to llama.rn through `devices: ['HTP0']`. Off Grid ships `.so` per Hexagon arch (v69, v73, v75, v79, v81) in `android/app/src/main/assets/ggml-hexagon/` (`docs/PERFORMANCE_IMPROVEMENTS.md`). Speedup not quantified in their docs; community reports 2–4× CPU prefill on text. **Excludes Snapdragon 7 Gen 1 (SM7450) — known crashes per llama.rn issue #279.** HTP and GPU layers are mutually exclusive.
- **Adreno OpenCL** for Adreno 700+. Off Grid caps GPU layers by RAM tier (`src/services/llmHelpers.ts`) and disables `ctx_shift` on Android GPU because the ggml `set` op SIGSEGVs on Adreno (upstream bug, `docs/PERFORMANCE_IMPROVEMENTS.md` "Improvement 6"). **CLIP vision is CPU-only on Android today** in off-grid — that's a real limitation for Atalaya's hot path.
- **LiteRT NPU delegate** in `Backend.NPU(nativeLibraryDir = context.applicationInfo.nativeLibraryDir)`. Targets Pixel TPU and Snapdragon QNN through Google's delegate — opaque, but tested and shipped by Google. The gallery defaults vision to GPU, not NPU, for Gemma 4 (`model_allowlists/1_0_11.json`: `"visionAccelerator": "gpu"`).

Pixel 6 has neither Hexagon nor a useful EdgeTPU delegate path for Gemma 4 — it's CPU-bound on llama.cpp/llama.rn and CPU-bound on LiteRT-LM in practice.

### 10 — Practical inference API target for Atalaya

The desired API for the inference module:

```kotlin
interface GemmaVision {
    suspend fun load(modelPath: String, mmProjPath: String, accel: Accel)
    suspend fun describe(jpegBytes: ByteArray, prompt: String, maxTokens: Int = 96): String
    suspend fun unload()
}
```

This is achievable on **all three** runtimes, but cleanly only on llama.rn (existing JNI we can extract) or LiteRT-LM (existing AAR). The llama.cpp official sample requires us to write the mmproj JNI ourselves first.

## Benchmark table

| Phone | Chipset | Runtime | Frame size | Latency | RAM peak | Battery /hr (5s interval) |
|-------|---------|---------|-----------|---------|----------|---------------------------|
| Pixel 6 | Tensor G1 | llama.rn CPU + F16 mmproj | 768×768 | TBD (estimate 3–5 s) | TBD (estimate 2.5–3 GB) | TBD (estimate 4–6 %) |
| Pixel 9 | Tensor G4 | llama.rn CPU + F16 mmproj | 768×768 | TBD (estimate 1.5–3 s) | TBD (estimate 2.5–3 GB) | TBD (estimate 3–5 %) |
| Pixel 9 | Tensor G4 | LiteRT-LM GPU vision | 768×768 | TBD | TBD (gallery sets 8 GB floor) | TBD |
| Snapdragon 8 Gen 1 (e.g. S22) | SM8450 | llama.rn HTP text + CPU CLIP | 768×768 | TBD (estimate 1.5–2.5 s) | TBD | TBD (estimate 2–3 %) |
| Snapdragon 8 Gen 2 (S23) | SM8550 | llama.rn HTP text + CPU CLIP | 768×768 | TBD | TBD | TBD |

All numbers above marked TBD must be measured on real hardware before ADR-0004 is finalized. The estimates are bounding ranges, not numbers to ship against.

## Tried that didn't work

- **Verifying `ggml-org/gemma-4-E2B-it-GGUF` Q4_K_M availability** — webfetch only surfaced Q8_0 and BF16 in the rendered model card. The `unsloth` fork is the source for Q4_K_M and matching F16 mmproj. ggml-org's deliberate omission of Q4 mmproj is itself a useful signal.
- **Reading HF blog for Pixel/Snapdragon latency numbers** — the Gemma 4 blog has none. Google's gallery README has none either. We cannot pre-commit to ADR-0004 frame intervals from public docs alone; we must measure.
- **Using llama.cpp's official `examples/llama.android` for vision** — the JNI binding only exposes text (`InferenceEngine.kt:31`). No mmproj, no image bytes. Either we write the mmproj JNI ourselves (1–2 weeks) or we use `llama.rn`'s pre-existing JNI bridge.
- **Confirming "BF16 mmproj is always correct" rule** — off-grid's HF matcher actively *avoids* BF16 mmproj on Android (`src/services/huggingface.ts:166-172`) due to runtime incompatibility on some Adreno OpenCL paths. The rule on Android should be **F16 mmproj preferred; BF16 only when end-to-end BF16 is verified**. Updating ADR-0003's quantization line accordingly.

## Recommendation

**Pick `llama.rn`'s native JNI as the runtime** (extracted as a Kotlin module in our codebase, no React Native dependency), running **`unsloth/gemma-4-E2B-it-GGUF` Q4_K_M main + F16 mmproj** for v1, with **HTP/OpenCL/CPU fallback chain** matching off-grid's pattern. Ranked: **(1) llama.rn JNI** — open GGUF, vision proven against Gemma 4 today, MIT license, Hexagon HTP and Adreno OpenCL paths shipped, but Android CLIP is currently CPU-only and we'll inherit that. **(2) LiteRT-LM** — cleanest Kotlin API and best Google-tuned NPU/GPU delegate, but proprietary `.litertlm` format and a hard 8 GB RAM floor lock us out of the 4–6 GB device tier that's central to the "old phones as cameras" thesis. **(3) llama.cpp official Android JNI sample** — universal in principle, but the upstream sample is text-only; getting Gemma 4 vision working requires us to write a `libmtmd` JNI ourselves before any product work begins, which is the wrong week-one tradeoff for a thin team. ADR-0003 should pick llama.rn JNI; ADR-0004's default frame interval should be **5 s** based on the battery envelope (not 1 s); ADR-0007 should set **min SDK 26 (Android 8)** to match llama.rn's API floor while staying above the deprecated SDK 24 line, and require **6 GB RAM** as the practical device floor (not 4 GB) given the foreground-service memory profile.
