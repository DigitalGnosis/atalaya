---
adr: 0012
title: Crash reporting — self-hosted Sentry on dg-core, opt-in only
status: accepted
date: 2026-05-04
---

# ADR-0012 — Crash reporting

## Context

Atalaya runs as a foreground service on phones across many Android versions, OEMs, and chipsets. Crashes are inevitable. Without crash reporting we find out users hit a bug only when they tell us — which means most never do. We need crash visibility without violating the privacy-first posture (per ADR-0006: nothing leaves the device unless the user opts in).

Two questions: which tool, and where does it run.

## Decision

**Self-hosted Sentry on dg-core, opt-in only.**

- **Tool:** Sentry (open source, BSL-then-Apache-after-3-years license). Mature, well-supported Android SDK, native NDK crash capture for our llama.rn .so files.
- **Hosting:** self-hosted on dg-core via the standard Sentry Docker compose. Indexed in `dg-helm` as a service. No external SaaS in the trust path.
- **Posture:** **opt-in only**. The default install emits zero crash reports. Users opt in via a settings toggle ("Help improve Atalaya by sharing anonymous crash reports"). When opted out, the SDK is initialized but every event is dropped before send.
- **DSN:** the Sentry DSN points at dg-core's Sentry instance for DG-distributed APKs. Self-hosters who run their own Atalaya hub can override the DSN to point at their own Sentry (or leave it blank to disable).
- **Data minimization:** stack trace, device model, Android version, app version, NDK ABI. **No** user-content fields, **no** rule text, **no** frame data, **no** GPS, **no** identifiers beyond a random opt-in install ID.

## Alternatives considered

- **Bugsnag.** Pros: clean SDK, generous free tier. Cons: SaaS only (no self-host), would put crash data in a third-party. Rejected — violates self-hosted-first posture.
- **Sentry SaaS (sentry.io).** Pros: zero ops. Cons: same third-party concern. Rejected for the same reason.
- **Roll our own (CipherWare for crashes).** Pros: reuse existing observability spine. Cons: CipherWare is for spans/logs, not crash dumps with symbolication and grouping. Reinventing crash reporting is months of work. Rejected — Sentry is the right tool, we just self-host it.
- **No crash reporting.** Pros: ultimate privacy. Cons: we ship blind. Rejected — we want quality data with opt-in consent.
- **Firebase Crashlytics.** Pros: free, popular. Cons: Google dependency, no self-host, would leak crash data to Google. Rejected.

## Consequences

**Easier:**
- Crash reports flow into the same dg-core box that runs Watchman, vaultwarden, etc. — one operations footprint.
- Sentry SDK ships symbolication for native crashes, important for our llama.rn .so failures.
- Users who opt in get the benefit of bugfixes faster.

**Harder:**
- One more service to operate on dg-core (Sentry compose is non-trivial — postgres, redis, kafka, multiple workers). About 4-6 GB RAM at idle.
- Dependency on dg-core for crash visibility on DG-distributed APKs. If dg-core is down, we lose new crash reports until it's back up (existing reports persist).
- Self-hosters who roll their own hub must set up their own Sentry or live without crash reports. Document the DSN-override path clearly.

**New constraints:**
- Opt-in toggle is non-negotiable. Default off, never silently on.
- A user who opts in once can opt out at any time; we honor immediately.
- Settings UI explains what we collect and links to a privacy notice.
- Crash reporting is initialized lazily — if user is opted out, the SDK is initialized but `beforeSend` returns null for every event. Avoids any default network calls.

## Implementation outline (Phase 1 Step 10 hardening)

1. Spin up Sentry Docker compose on dg-core. Index in `dg-helm`.
2. Create the Atalaya project in Sentry; capture the DSN.
3. Store the DSN in BWS DG-Shared as `ATALAYA_SENTRY_DSN` (read at build time so debug builds can use a different DSN if needed).
4. Wire `apps/node` to initialize Sentry with the DSN, with a `beforeSend` that drops everything when the opt-in flag is off.
5. Settings screen: opt-in toggle + plain-language explainer + privacy notice link.
6. Onboarding: on first launch, mention crash reporting briefly with a deferred "decide later" option (default opt-out).
7. Self-hoster docs: how to override DSN, how to roll their own Sentry.

## Related

- Supersedes: none
- Superseded by: none
- Informed by: existing DG self-host pattern (vaultwarden, postgres, watchman on dg-core); ADR-0006 privacy posture
- Triggers: dg-helm card for Sentry, BWS secret for DSN, Phase 1 Step 10 implementation
