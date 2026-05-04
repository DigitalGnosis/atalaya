---
adr: 0014
title: Dogfood device correction — Galaxy S21 exercises CPU/OpenCL paths, NOT Hexagon HTP NPU
status: accepted
date: 2026-05-04
supersedes: ADR-0013
---

# ADR-0014 — Dogfood device correction

## Context

ADR-0013 named the Samsung Galaxy S21 as the first dogfood device and claimed its Snapdragon 888 has "Hexagon HTP — our preferred NPU path per ADR-0003." **That claim is wrong.** Verification:

- llama.cpp's Hexagon HTP backend [documentation](https://github.com/ggml-org/llama.cpp/blob/master/docs/backend/snapdragon/README.md) explicitly states: **"Hardware requirements... Snapdragon 8 Gen 2 or higher SoC"**.
- llama.rn ships Hexagon library variants v69, v73, v75, v79, v81. The Snapdragon 888's Hexagon 780 is **V68** — not in the supported list.
- Snapdragon 8 Gen 1 = V69, Gen 2 = V73, Gen 3 = V75, Gen 4/5 = V79/V81. The 888 predates the supported range.

The Galaxy S21 is still a valid dogfood device, but for **different reasons** than ADR-0013 claimed.

This ADR supersedes ADR-0013 to correct the inference-path claim and re-justify the S21 choice.

## Decision

**Galaxy S21 remains the working first dogfood device** (pending physical confirmation per ADR-0013). But:

- **Inference path is OpenCL on Adreno 660 GPU, with CPU fallback.** NOT Hexagon HTP. The S21 will not get the NPU acceleration we hoped for.
- **Performance baseline expectations adjust.** Inference latency per frame will be in the 5-15 second range for E2B vision (CPU/GPU on 888) instead of the sub-second we'd expect on a Gen 2+ chipset.
- **The S21 validates: Samsung OEM-kill mitigation, OpenCL Adreno path, and the "old phone" target audience.**
- **A second dogfood device on Snapdragon 8 Gen 2+ is needed eventually** (Pixel 8+, Galaxy S23+, OnePlus 11+) to validate the Hexagon HTP path that gives us our headline performance numbers. Treat this as a Phase 1 Step 11 release prep need, not a blocker for Steps 3-10.

## Why the S21 still works as the first dogfood device

| Spec | Value | Why it still matters |
|------|-------|---------------------|
| RAM | 8 GB | E2B (~1.5 GB) + foreground service overhead fits with headroom |
| GPU | Adreno 660 | OpenCL inference path validated; baseline for older devices |
| Android | Currently 14+ | Above Min SDK 26 |
| Battery | 4000 mAh | Adequate for measured duty cycles |
| OEM | Samsung | Stress-tests OEM-kill mitigation per ADR-0007 (real benefit, unchanged from ADR-0013) |
| Release | Jan 2021 | Genuinely represents "old phone" target audience |

## Why we still need a Gen 2+ device eventually

The headline performance numbers in our README (sub-5-second descriptions on flagship phones) come from the Hexagon HTP NPU path. To validate that — and to ship marketing claims — we need a second dogfood device with Snapdragon 8 Gen 2 or newer. This is a Phase 1 Step 11 (release prep) need.

Candidates:
- **Pixel 8** (Tensor G3) — different chipset family, but Tensor has its own NPU; verify llama.rn support separately
- **Pixel 9** (Tensor G4) — same caveat as G3
- **Galaxy S23** (Snapdragon 8 Gen 2) — Hexagon V73, supported
- **OnePlus 11 / 12** (Snapdragon 8 Gen 2/3) — Hexagon V73/V75, supported

The cleanest path is a Gen 2+ Snapdragon device since llama.rn explicitly supports its Hexagon. Tensor NPU support through llama.rn needs separate verification.

## Lesson learned (relevant to future ADRs)

I committed ADR-0013 with an unverified claim about NPU compatibility. The chipset version specifics — Hexagon V68 vs V73 — are exactly the kind of detail Policy 003 (Research Before Agreement) exists to catch. Going forward:

- **Any ADR that asserts hardware capability claims must cite the source verifying it.** Vendor docs, llama.cpp/llama.rn documentation, or fresh search results.
- **The "what's new in this commit" line in ADR alternatives sections** must distinguish "I confirmed X" from "I think X."
- **For multi-component decisions** like dogfood device (chipset + RAM + Android + OEM), each component claim deserves its own verification, not a general nod.

## Consequences

**Same as ADR-0013, except:**

**Easier:**
- We have a real baseline for OpenCL/CPU performance on a 5-year-old Snapdragon device. That's the "worst case still works" data point that makes our claims credible.
- Samsung OEM-kill mitigation gets exercised (unchanged).

**Harder:**
- Performance numbers from S21 alone won't be flattering. We need a Gen 2+ device for headline performance claims before public release.
- Two devices to maintain in the dogfood loop — S21 for compatibility floor, Gen 2+ for performance ceiling.

**New constraints:**
- README's performance claims must cite the device they were measured on. No "5-second descriptions" without saying which phone.
- Phase 1 Step 11 release prep includes acquiring/borrowing a Gen 2+ Snapdragon device for benchmarking.
- ADR-0013's NPU claim is corrected; future ADRs that rely on hardware specifics must cite verification sources.

## Related

- Supersedes: [ADR-0013](ADR-0013-dogfood-device.md)
- Superseded by: none (until physical confirmation reveals a different drawer phone)
- Informed by: [llama.cpp Hexagon HTP docs](https://github.com/ggml-org/llama.cpp/blob/master/docs/backend/snapdragon/README.md), [llama.cpp Hexagon backend overview](https://huggingface.co/PanhaPa/My-Llama-3.2-FineTuned/blob/main/llama.cpp/docs/backend/hexagon/developer.md), [M4 research note](../research/M4-local-gemma.md)
- Triggers: README performance-claim discipline; second dogfood device acquisition before Step 11
