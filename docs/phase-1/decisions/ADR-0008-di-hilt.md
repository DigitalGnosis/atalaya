---
adr: 0008
title: Dependency injection — Hilt
status: accepted
date: 2026-05-04
---

# ADR-0008 — Dependency injection

## Context

Atalaya needs dependency injection for Android apps. ViewModels need their dependencies injected (per ADR-0002), services need access to the inference engine, the rule engine, the alert transport, and persistence. M1 research confirmed Bitwarden uses Hilt; Now in Android (Google's reference) uses Hilt. Both treat it as the default for modern Android.

## Decision

Use **Hilt** (`com.google.dagger:hilt-android`) as the DI framework for all Android apps in the monorepo.

- ViewModels: `@HiltViewModel class FooViewModel @Inject constructor(...) : BaseViewModel<S, E, A>(...)`
- Singletons: `@Singleton @InstallIn(SingletonComponent::class)`
- App entry: `@HiltAndroidApp class AtalayaApplication : Application()`
- Service injection: `@AndroidEntryPoint class WatcherService : Service()` — gives us field injection inside services
- `SavedStateHandle` is auto-provided by Hilt for ViewModels — used for state-restoration after process death (per ADR-0002 pattern)

For non-Android modules in the monorepo (`packages/core-protocol`, `packages/core-rules`, `packages/core-ml`), Hilt is not used — those are pure Kotlin libraries with constructor injection that the Android apps wire up via Hilt modules.

## Alternatives considered

- **Koin.** Pros: simpler runtime DI, Kotlin-first, no kapt/ksp. Cons: runtime resolution (no compile-time graph validation), smaller ecosystem of patterns, conflicts with Bitwarden's Hilt convention. Rejected — losing compile-time safety for marginal build-time savings.
- **Manual DI.** Pros: zero dependencies. Cons: doesn't scale past a couple of screens, ViewModel + Compose + Service injection becomes painful by Phase 2. Rejected.
- **Dagger directly (no Hilt).** Pros: more flexibility. Cons: more boilerplate, more component setup. Hilt is opinionated Dagger and the opinions match Google's. Rejected.

## Consequences

**Easier:**
- Bitwarden's BaseViewModel pattern (ADR-0002) ports directly — Hilt is the assumed wiring.
- Now in Android module patterns port directly — same DI framework.
- Compile-time graph validation catches missing bindings before runtime.
- KSP-based code gen is fast on modern Gradle (3-4x faster than legacy kapt).

**Harder:**
- Hilt requires KSP setup and slows clean builds slightly. Acceptable.
- New contributors must understand `@InstallIn`, scopes, qualifiers — but this is well-documented.

**New constraints:**
- All Android apps in `apps/` use Hilt. No mix-and-match DI within the Android side.
- Pure-Kotlin packages don't use Hilt — they expose interfaces and accept dependencies via constructor.
- KSP version pinned in `gradle/libs.versions.toml` (lands in roadmap Step 2).

## Related

- Supersedes: none
- Superseded by: none
- Informed by: [`../research/M1-bitwarden-baseviewmodel.md`](../research/M1-bitwarden-baseviewmodel.md), [Now in Android](https://github.com/android/nowinandroid)
- Triggers: `apps/node/` skeleton in roadmap Step 2 (Hilt setup), `apps/control/` future Phase 4 (same Hilt setup)
