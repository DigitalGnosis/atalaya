---
module: M3
title: Camera capture loop with CameraX
status: stub
opened: 2026-05-04
time_box_ends: 2026-05-07
sources:
  - https://developer.android.com/training/camerax
  - https://github.com/android/camera-samples
---

# M3 — CameraX capture loop

## Why this matters

Every N seconds Atalaya snaps a frame and feeds it to inference. Camera2 is too low-level for the value it adds. CameraX is the modern Android-blessed wrapper. We need to know the right `ImageCapture` configuration for our background-service shape — not the foreground viewfinder demo most samples show.

## Specific questions

1. How do we use CameraX from a background service (no preview surface)? Use `ImageAnalysis` use case with a custom analyzer?
2. JPEG vs YUV vs PNG output — best for downstream Gemma input?
3. Resolution — what's the smallest frame that still produces useful Gemma descriptions? Likely 512x512 or 768x768. Can we measure?
4. How do we manage camera lifecycle inside a foreground service that has no Activity?
5. Permission failures — graceful handling pattern?
6. Multiple cameras (front + back) — Phase 1 is single-camera, but the API choice should not block Phase 3 multi-camera roles.
7. Camera tilt / orientation — the phone is mounted statically; does CameraX hand us the right orientation or do we have to read the EXIF?

## Findings

_(fill in)_

## Tried that didn't work

_(log dead ends)_

## Recommendation

_(one paragraph — implementation choice, no ADR needed unless surprising)_
