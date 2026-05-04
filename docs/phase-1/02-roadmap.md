# Phase 1 — Roadmap

The ordered build plan. Each step links to its research note, the ADR(s) it produces, and the module(s) it lands.

## The arc

```
Research (3 days/module, parallel)
         ↓
Decisions (ADRs commit the research)
         ↓
Module skeletons (Gradle, Hilt, Compose scaffolding)
         ↓
Vertical slice 1: BaseViewModel + a single Compose screen
         ↓
Vertical slice 2: Foreground service, no AI, just keeps a timer
         ↓
Vertical slice 3: CameraX captures every N seconds, saves to disk
         ↓
Vertical slice 4: Local Gemma 4 describes the saved frame
         ↓
Vertical slice 5: Rule engine evaluates the description
         ↓
Vertical slice 6: Match → push notification
         ↓
Hardening + battery + permissions polish
         ↓
v1.0 APK release
```

Vertical slices means each step is end-to-end runnable, even if narrow. We never have a half-built foreground service blocking a half-built camera capture. Each slice ships a debug APK that does one more thing than the last.

## Step-by-step

### Step 0 — Phase open (✅ done)

Phase 1 docs framework, research stubs, ADR queue. This document.

### Step 1 — Research sprint (parallel, 3 days each, hard cap)

| Module | Research stub | Output (ADR) |
|--------|---------------|--------------|
| M1 BaseViewModel | [`research/M1-bitwarden-baseviewmodel.md`](research/M1-bitwarden-baseviewmodel.md) | ADR-0002 |
| M2 Background service | [`research/M2-background-service.md`](research/M2-background-service.md) | ADR-0007 (partial) |
| M3 CameraX | [`research/M3-camerax.md`](research/M3-camerax.md) | _(implementation choice, no ADR needed)_ |
| M4 Local Gemma | [`research/M4-local-gemma.md`](research/M4-local-gemma.md) | ADR-0003, ADR-0004, ADR-0007 (partial) |
| M5 Rule engine | [`research/M5-rule-engine.md`](research/M5-rule-engine.md) | ADR-0005 |
| M6 Push | [`research/M6-push.md`](research/M6-push.md) | ADR-0006 |

Done when every research stub has a `## Findings`, `## Recommendation`, and the corresponding ADRs are committed.

### Step 2 — Module skeletons

Gradle setup, Hilt configured, Compose + Material 3 baseline, module boundaries from ADR-0001 enforced via project structure.

Lands:
- `apps/node/` Android module
- `packages/core-protocol/` skeleton with the wire-type stubs
- `packages/core-rules/` skeleton
- `packages/core-ml/` skeleton (interface only — implementations come later)
- `packages/core-sensors/` skeleton

### Step 3 — Vertical slice 1: One screen, one ViewModel

A single Compose screen with a button. ViewModel uses the BaseViewModel pattern from ADR-0002. Tapping the button updates state. Proves the foundation works.

### Step 4 — Vertical slice 2: Foreground service heartbeat

Service starts from the screen, runs in the foreground with a persistent notification, ticks every N seconds and updates a counter visible in the notification. Survives screen lock and battery saver. No camera, no AI yet.

### Step 5 — Vertical slice 3: Camera capture

Service uses CameraX to capture a JPEG frame at each tick. Saves to internal storage with a timestamp. Logs frame size. Still no AI.

### Step 6 — Vertical slice 4: Gemma describes frame

After capture, runs the frame through the Gemma 4 inference pipeline (per ADR-0003). Logs the description. The persistent notification shows the latest description.

### Step 7 — Vertical slice 5: Rule evaluation

The user can input a rule in the screen's settings. Each captured frame's description is evaluated against the rule (per ADR-0005). Match/no-match logged.

### Step 8 — Vertical slice 6: Push on match

Match → push notification (per ADR-0006). For Phase 1 the target is the same phone. Notification deep-links back to the app showing the matching frame thumbnail and description.

### Step 9 — Onboarding (normie-first polish)

This step is load-bearing for the v1 audience and gets its own slice — not a hardening sub-bullet.

- `core-onboarding` package per [`modules/core-onboarding.md`](modules/core-onboarding.md) — primitives, persistence, skip-all toggle.
- First-launch tutorial: welcome card → "what Atalaya does in 30 seconds" → permissions explainer chain.
- Pre-built rule suggestions list ("alert me when a person is at the front door", "alert me when the door is open after 9 PM") with one-tap accept.
- In-app model download UI with progress bar and clear "what's happening" copy.
- Frame interval slider with three labeled presets — Battery saver / Balanced / Responsive — not a raw seconds field.
- Persistent notification phrasing tuned for trust ("Atalaya is armed — tap to stop").
- Alert detail screen with one-tap ✓ real / ✗ false alarm feedback.
- Power-user escape hatch: settings toggle that hides all hints/tutorials.
- Uninstall+reinstall path verified clean (no orphan permission state confusing the user).

### Step 10 — Hardening

- Permissions flow edge cases (denied → re-prompt with deep-link to settings, OEM-specific battery optimization screens).
- Charge-aware throttle (slow down on battery, normal on charger).
- Crash reporting (opt-in only — surface in onboarding).
- App updates / version pinning.
- Build manufacturer-aware OEM-kill mitigation onboarding screen (per ADR-0007).

### Step 11 — Release

Build a signed APK. Tag `v1.0.0`. Publish via GitHub Releases. F-Droid metadata submitted in parallel (review takes weeks). Install procedure documented for normies — screenshots, no terminal.

## Daily rhythm

- Morning: pick the next slice, write a ~50 word "what I'm doing today" comment in the slice's GitHub issue.
- Afternoon: open a draft PR with whatever you have. Even half-done is reviewable.
- End of day: status update. If you're stuck, name the obstacle in the issue — never silently wait.

## Definition of done (per slice)

- Vertical: a debug APK installs and demonstrates the new behavior end-to-end.
- Linted: ktlint, Android lint, no new warnings.
- Tested: at minimum a smoke test for new behavior. Unit tests where they pay off.
- Documented: the module's spec in `modules/` updated to reflect what was built.
- Reviewed: a maintainer signed off.
- Merged: squash commit to `main` with a reference to the slice issue.
