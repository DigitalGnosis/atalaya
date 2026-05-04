---
module: M6
title: Push notification pipeline
status: stub
opened: 2026-05-04
time_box_ends: 2026-05-07
sources:
  - https://github.com/bitwarden/android
  - https://ntfy.sh/
related_adrs:
  - ADR-0006
---

# M6 — Push pipeline

## Why this matters

When a rule matches, the user must know within seconds. Phase 1 alerts go to the same phone (later phases route to a designated control phone). We need a push transport that's reliable, deliverable while the device is in doze, and not vendor-locked. FCM is the obvious answer; ntfy is the open-source alternative.

## Specific questions

1. **FCM** — sender ID setup, server key handling for self-hosters, token refresh, doze-bypass requirements, billing (FCM is free for our scale).
2. **ntfy.sh** — open, self-hostable. Adds a dependency on either ntfy.sh or a self-hosted ntfy server. What's the latency comparison?
3. **Local push** (within-device when target is the same phone as the watcher) — can we just fire a notification directly without going through any server?
4. **Self-hosters' constraint.** A Phase 1 self-hoster has no hub yet. The watcher phone is the source AND the target. How does a fully local install send a notification to itself? `NotificationManager` directly, no transport needed.
5. **Dual transport for Phase 2+.** When a hub exists, the hub needs to push to a control device. FCM there too? ntfy? Both?
6. **Reliability under doze.** FCM high-priority messages bypass doze. ntfy's behavior depends on the Android client.

## Findings

_(fill in)_

## Tried that didn't work

_(log dead ends)_

## Recommendation

_(one paragraph, informs ADR-0006)_
