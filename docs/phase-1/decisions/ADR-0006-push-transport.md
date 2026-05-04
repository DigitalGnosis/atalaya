---
adr: 0006
title: Push transport — pluggable AlertTransport, Phase 1 ships LocalNotificationTransport only
status: accepted
date: 2026-05-04
---

# ADR-0006 — Push transport

## Context

When a rule matches, the user must know within seconds. M6 research ([`../research/M6-push.md`](../research/M6-push.md)) found that Phase 1's reality is one-phone — the watcher and the user's phone are the same device, so there's no network hop needed. Phase 2+ introduces a hub and a separate control device, at which point real push transport is required.

The architectural question is whether to wire FCM or ntfy now even though Phase 1 doesn't need it, or ship the minimum and design for swap.

## Decision

Define `AlertTransport` as an interface in `packages/core-protocol/`. Phase 1 ships exactly one implementation — `LocalNotificationTransport` — using Android `NotificationManager` directly with `CATEGORY_ALARM` + `PRIORITY_HIGH` to bypass doze. No FCM, no ntfy, no Firebase project setup, no self-hoster friction in Phase 1.

```kotlin
// packages/core-protocol/.../AlertTransport.kt
interface AlertTransport {
    suspend fun send(alert: Alert): Result<Unit>
}

// apps/node/.../LocalNotificationTransport.kt (Phase 1)
class LocalNotificationTransport(private val context: Context) : AlertTransport {
    override suspend fun send(alert: Alert): Result<Unit> = runCatching {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_atalaya)
            .setContentTitle("Atalaya — ${alert.observation.summary()}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            // ...
            .build()
        NotificationManagerCompat.from(context).notify(alert.id.value.hashCode(), notification)
    }
}
```

Phase 2 introduces:
- `FcmTransport` — default for users on Google-blessed phones, requires Firebase project setup by self-hosters who want it
- `NtfyTransport` — default for self-hosters who don't want Google, configurable server URL (ntfy.sh or self-hosted)

Both implement the same `AlertTransport` interface. The watcher service depends on the interface, not on FCM or ntfy.

## Alternatives considered

- **Wire FCM in Phase 1 even though we don't need it.** Pros: forces us to debug push earlier. Cons: requires Firebase project setup, adds Google dependency to v1, friction for users who just want a watcher on their old phone. Rejected — premature.
- **Always use ntfy, even Phase 1.** Pros: open from day one. Cons: requires either ntfy.sh (third-party dependency) or self-hosting (Phase 1 has no hub). Rejected — Phase 1 has no transport need at all.
- **Custom in-process event bus + plug FCM/ntfy in later as listeners.** Pros: more flexible. Cons: more abstraction than the problem requires. Rejected.

## Consequences

**Easier:**
- Phase 1 has zero dependency on a third-party push service.
- Self-hosters can install Atalaya, configure rules, and run on a single phone with no Google involvement.
- The `AlertTransport` interface is a single contract — Phase 2 implementations slot in without watcher changes.
- Bitwarden's FCM integration becomes a reference for Phase 2 (port pattern documented in `references/`).

**Harder:**
- We discover real-world push edge cases later (network failure during alert send, server-side throttling, etc.) — Phase 2 has more to debug at once.
- The `LocalNotificationTransport` doesn't share much code with FCM/ntfy variants; the abstraction is slightly thin in v1. Acceptable.

**New constraints:**
- `AlertTransport.send` returns `Result<Unit>` — failures are logged + retried with backoff. The interface is the same shape for in-process notifications and remote pushes.
- `Alert` payload must serialize to wire-friendly JSON (already true per `core-protocol`).
- `CATEGORY_ALARM` requires user-granted notification permission; the arm flow surfaces this.

## Related

- Supersedes: none
- Superseded by: none
- Informed by: [`../research/M6-push.md`](../research/M6-push.md)
- Triggers: `packages/core-protocol/` `AlertTransport` interface in roadmap Step 2; `apps/node/` `LocalNotificationTransport`
- Future: Phase 2 ADR for FCM and ntfy implementations
