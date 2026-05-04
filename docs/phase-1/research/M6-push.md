---
module: M6
title: Push notification pipeline
status: findings
opened: 2026-05-04
time_box_ends: 2026-05-04
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

### Phase 1 reality: same-phone, no transport needed

In Phase 1, the watcher phone IS the control phone. Source and target are the same device. **There is no network hop needed.** Use Android's `NotificationManager` directly:

```kotlin
val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
    .setSmallIcon(R.drawable.ic_atalaya)
    .setContentTitle("Atalaya — ${rule.condition}")
    .setContentText(observation.description)
    .setLargeIcon(observation.frameThumbnail)
    .setStyle(NotificationCompat.BigPictureStyle().bigPicture(observation.frameThumbnail))
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .setCategory(NotificationCompat.CATEGORY_ALARM)
    .setContentIntent(deepLinkToAlertDetail)
    .build()

NotificationManagerCompat.from(context).notify(alert.id.hashCode(), notification)
```

This requires `POST_NOTIFICATIONS` runtime permission (already in M2's manifest plan).

Critical: `setCategory(CATEGORY_ALARM)` + `PRIORITY_HIGH` is what lets the notification break through doze and battery saver. CATEGORY_ALARM is the strongest non-call category and is allowed for security/safety apps.

### Phase 2+ transport architecture

Phase 2 introduces the hub. Hub-to-control-phone now needs a real transport. Two candidates:

#### FCM (Firebase Cloud Messaging)

| Aspect | Notes |
|--------|-------|
| Reliability | High; high-priority messages bypass doze |
| Cost | Free for our scale (millions of messages/day free tier) |
| Self-hoster cost | Requires Firebase project setup; sender ID + service account JSON |
| Privacy concern | Google sees message metadata (timing, recipient token); we E2E-encrypt the payload |
| Delivery latency | Sub-second typically |
| Lock-in | Medium — replacing FCM later requires re-engineering, but the abstraction is clean |
| Library | `com.google.firebase:firebase-messaging` |

#### ntfy (open source push, self-hostable)

| Aspect | Notes |
|--------|-------|
| Reliability | Good; uses persistent HTTP/SSE; less battery-optimized than FCM |
| Cost | Free public server (ntfy.sh) or self-host (Go binary) |
| Self-hoster cost | Trivial — `docker run binwiederhier/ntfy` |
| Privacy concern | None (self-hosted) or minimal (shared topic strings on ntfy.sh, encrypt payload) |
| Delivery latency | 1-3s typically; depends on client poll interval if not using FCM-relay mode |
| Lock-in | None |
| Library | F-Droid `ntfy-android` app integrates; for in-app, use OkHttp + SSE |

ntfy on Android has a quirk: without FCM passthrough mode, the ntfy Android *app* maintains the connection and acts as a notification bridge. For an in-app integration, we maintain our own SSE connection — which means our app must be running (which is fine, the watcher is a foreground service).

### Recommendation: pluggable transport from day one

The right architecture is a `core-protocol` interface and Phase-N-specific implementations:

```kotlin
// packages/core-protocol/.../AlertTransport.kt
interface AlertTransport {
    suspend fun send(alert: Alert): Result<Unit>
}

// Phase 1
class LocalNotificationTransport(context: Context) : AlertTransport { /* uses NotificationManager */ }

// Phase 2+
class FcmTransport(...) : AlertTransport { /* FCM client */ }
class NtfyTransport(serverUrl: String, topic: String) : AlertTransport { /* HTTP push */ }
```

The watcher service depends on `AlertTransport`, not on FCM or ntfy specifically. Self-hosters who don't want Google can swap to ntfy with no code change.

### Bitwarden's FCM pattern (port reference for Phase 2)

Bitwarden Android (GPL-3.0, AGPL-3.0 compatible) has a battle-tested FCM integration we can port when Phase 2 lands. Path: `app/src/main/java/com/x/bitwarden/data/auth/datasource/network/service/PushService.kt` (and friends). Pattern: a `FirebaseMessagingService` subclass that delegates to a `PushHandler` interface — clean separation, easy to swap.

### Doze interaction

`PRIORITY_HIGH` + `CATEGORY_ALARM` notifications bypass doze for foreground-displayed alerts. FCM `priority: high` messages bypass doze for delivery. ntfy without FCM passthrough relies on the persistent connection; battery saver may throttle. Phase 1's local notification has no transport latency at all so this is moot for v1.

## Tried that didn't work

n/a — design research, no implementations attempted in this round.

## Recommendation

For Phase 1, ship `LocalNotificationTransport` only — Android `NotificationManager` direct, `CATEGORY_ALARM` + `PRIORITY_HIGH`. No FCM, no ntfy, no Firebase project setup, no self-hoster friction. The `AlertTransport` interface goes into `core-protocol` so Phase 2's FCM and ntfy implementations slot in without changing the watcher. ADR-0006 commits this. Phase 2 introduces FCM (default for users on Google-blessed phones) and ntfy (default for self-hosters, configurable server URL) as pluggable alternatives, both behind the same interface.
