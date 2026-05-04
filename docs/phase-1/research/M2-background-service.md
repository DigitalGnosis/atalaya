---
module: M2
title: Background service that survives Android battery optimization
status: stub
opened: 2026-05-04
time_box_ends: 2026-05-07
sources:
  - https://github.com/guardianproject/haven
  - https://github.com/termux/termux-app
  - https://github.com/tailscale/tailscale-android
related_adrs:
  - ADR-0007
---

# M2 — Background service survival

## Why this matters

A security watcher that gets killed by Android's death squad after 30 minutes is worthless. Atalaya needs a foreground service that survives screen lock, doze mode, battery optimization, and ideally aggressive OEM kill mechanisms (Xiaomi, Huawei, OnePlus). We need to know exactly what flags, foreground service types, and patterns make this work in 2026 Android.

## Specific questions

1. What `foregroundServiceType` does Atalaya's camera role need under Android 14+? (`camera`? `dataSync`? Multiple?)
2. How does Haven's `MonitorService` declare itself? What's the minimum to port?
3. Tailscale's Android client maintains a persistent VPN service — how do they declare and survive?
4. What permissions does the user have to grant beyond runtime camera/mic? (FOREGROUND_SERVICE_CAMERA, etc.)
5. How aggressive is OEM kill on Xiaomi / Samsung / OnePlus / Pixel — what's the documented behavior in 2026?
6. Does WorkManager + a periodic worker cover us, or do we genuinely need a sticky foreground service?
7. How do we detect when the service has been killed by the OS so we can re-prompt the user?
8. Wake locks — yes/no, partial vs full, battery impact?
9. Battery saver mode interaction — does it pause us or just throttle?
10. Doze mode interaction — frame interval will lengthen during doze. Acceptable for v1?

## Findings

_(fill in)_

## Tried that didn't work

_(log dead ends)_

## Recommendation

_(one paragraph, informs ADR-0007)_
