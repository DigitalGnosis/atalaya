# Phase 1 — References (Code we ported from)

Atalaya is built on the principle that nothing is scratch-built when a working reference exists. Every pattern, snippet, or architectural choice we lift from another project gets a citation here, with attribution and license details.

## Citation format

Each reference is its own file. See [`TEMPLATE.md`](TEMPLATE.md). At minimum:

- **Project name + URL**
- **License** (must be compatible with AGPL-3 — most FOSS licenses are)
- **What we ported** (one paragraph)
- **Files touched** in our repo
- **Their commit hash** at the time we read it

## External references

These are public open-source projects we port from.

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

## DG-internal references

These are DG-owned repositories — apps and libraries already shipping in our ecosystem — that we study or port from. Same citation discipline applies (commit hash, what was lifted), but no license-compatibility analysis since everything is AGPL-3.0 / MIT inside DG.

| Reference | Path on pop | Status | Candidate use |
|-----------|-------------|--------|---------------|
| `cipherware-sdk-android` | `~/AndroidProjects/cipherware-sdk-android` | already shipping | **Direct dependency** of `core-observability`. We consume the published artifact, not a port. |
| Connect | `~/AndroidProjects/Connect` | already shipping | Source for FCM integration, MediaSession patterns, voice routing, CipherWare span emission patterns (Phase 2 transport, audio role) |
| Dispatch (Android client) | `~/AndroidStudioProjects/Dispatch` (pop) | already shipping | Source for the `DispatchTransport` integration (Phase 2). Reference for DG-branded UI patterns. |
| AndyClaw | `~/AndroidStudioProjects/AndyClaw` (pop) | already shipping | Same Compose + Hilt + Material 3 stack. Source for theme conventions, Hilt module patterns. |
| StacheApp | `~/AndroidStudioProjects/StacheApp` (pop) | already shipping | Same stack + Supabase auth. Source for theme + auth (Phase 2 control app). |
| ChessCoach | `~/AndroidStudioProjects/ChessCoach` (pop) | already shipping | Same stack. Theme reference. |
| Bitwarden Android (clone) | `~/AndroidStudioProjects/bitwarden-android` (pop) | already cloned | Local copy of the Bitwarden repo — no need to re-clone for M1 ports. |
| TeamsOrderListener | `~/AndroidStudioProjects/TeamsOrderListener` (pop) | already shipping | Possibly a `NotificationListenerService`. Reference for Phase 3 if Atalaya watches notifications from other apps. |

DG-internal references typically port faster than external ones — same conventions, same Hilt setup, same Compose patterns. When we need a piece of functionality that DG has already shipped, we look at a DG app first and an external project second.

When porting from a DG-internal repo, cite the path on pop, the commit hash at the time of read, and what was lifted — same discipline as external references.

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
