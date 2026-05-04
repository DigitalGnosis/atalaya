# Phase 1 — References (Code we ported from)

Atalaya is built on the principle that nothing is scratch-built when a working reference exists. Every pattern, snippet, or architectural choice we lift from another project gets a citation here, with attribution and license details.

## Citation format

Each reference is its own file. See [`TEMPLATE.md`](TEMPLATE.md). At minimum:

- **Project name + URL**
- **License** (must be compatible with AGPL-3 — most FOSS licenses are)
- **What we ported** (one paragraph)
- **Files touched** in our repo
- **Their commit hash** at the time we read it

## Index

| Reference | License | What we used | File |
|-----------|---------|--------------|------|
| Bitwarden Android | GPL-3.0 | BaseViewModel pattern, FCM integration | _(stub — fill after M1 / M6 research)_ |
| Haven (Guardian Project) | GPL-3.0 | MonitorService pattern for foreground service survival | _(stub — fill after M2)_ |
| Off Grid Mobile AI | MIT | GGUF model loading, llama.cpp Android JNI | _(stub — fill after M4)_ |
| Google AI Edge Gallery | Apache-2.0 | LiteRT-LM Gemma 4 runner | _(stub — fill after M4)_ |
| Now in Android | Apache-2.0 | Module structure, DI patterns, build conventions | _(stub — fill after Step 2)_ |
| llama.cpp | MIT | Android build config, JNI shape | _(stub — fill after M4)_ |
| CameraX official samples | Apache-2.0 | ImageAnalysis use case for headless capture | _(stub — fill after M3)_ |
| Termux:API | Apache-2.0 (sibling reference for sensor patterns; `MicRecorderAPI`) | _(may not be a direct port; informs design)_ | _(if used)_ |

## License compatibility

AGPL-3.0 is compatible with most permissive (MIT, Apache-2.0, BSD) and copyleft (GPL-3.0) licenses. Code we port from those projects can land under our AGPL-3.0 umbrella with proper attribution.

| Source license | Compatible with AGPL-3.0? | Notes |
|----------------|---------------------------|-------|
| MIT | ✅ | Attribution required in source headers |
| Apache-2.0 | ✅ | Attribution + NOTICE file mention |
| BSD-2/3 | ✅ | Attribution required |
| GPL-3.0 | ✅ | Combined work is AGPL-3.0 |
| AGPL-3.0 | ✅ | Same license |
| LGPL | ✅ | Treat as separate module if linking dynamically |
| Proprietary / no license | ❌ | Cannot use |
| GPL-2 only | ⚠️ | Only "GPL-2-or-later" is compatible; "GPL-2-only" is not |
