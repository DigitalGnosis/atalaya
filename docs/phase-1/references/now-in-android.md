---
project: nowinandroid
url: https://github.com/android/nowinandroid
license: Apache-2.0
read_at_commit: 7d45eae4f8720a0c77f507712ba2437ff974b6ed
read_at_date: 2026-05-04
ported_in_atalaya:
  - gradle/libs.versions.toml
  - gradle.properties
  - gradle/wrapper/gradle-wrapper.properties
  - settings.gradle.kts
  - build.gradle.kts
  - apps/node/build.gradle.kts
  - packages/*/build.gradle.kts
---

# Reference — `nowinandroid`

## What it is

Google's reference Android app demonstrating modern architecture patterns: multi-module
Gradle, Hilt DI, Compose UI, version catalog, convention plugins, KSP, configuration
cache. Atalaya borrows the *Gradle scaffolding shape* from it; the `apps/` + `packages/`
monorepo layout itself diverges per [ADR-0001](../decisions/ADR-0001-monorepo-layout.md).

## License

Apache-2.0 — compatible with Atalaya's AGPL-3.0. Confirmed in
[`docs/phase-1/references/README.md`](README.md#license-compatibility).

## What we ported

The Gradle scaffolding pattern: a single `gradle/libs.versions.toml`, plugin classpath
declared once at the root with `apply false`, `pluginManagement` + `dependencyResolutionManagement`
in `settings.gradle.kts`, and `gradle.properties` tuned for monorepo daemon size and
configuration cache. The `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` flag is
also lifted directly. Per-module `build.gradle.kts` files declare only the plugins
they apply and their dependencies, with no convention-plugin layer (Atalaya's module
count is small enough that build-logic indirection costs more than it saves at this
phase).

## Files in their repo we read

- `gradle/libs.versions.toml` — version catalog. Treated as the structure reference;
  Atalaya pins different (slightly newer) versions per Policy 004.
- `gradle.properties` — JVM args, configuration cache flags, KSP project isolation.
  Ported almost verbatim.
- `gradle/wrapper/gradle-wrapper.properties` — Gradle distribution pinning. Atalaya
  matches Bitwarden's 9.4.1 instead of NIA's 9.4.0 (newer patch).
- `settings.gradle.kts` — `pluginManagement` + `dependencyResolutionManagement` blocks,
  repo content filters (`includeGroupByRegex`), JDK version check.
- `build.gradle.kts` — root file, plugins-with-`apply-false` shape.
- `app/build.gradle.kts` — Android application module shape. Atalaya's `apps/node/build.gradle.kts`
  takes the structure but flattens the convention-plugin layer.
- `core/data/build.gradle.kts` — representative library module. Atalaya's `packages/*/build.gradle.kts`
  follow the same minimal-declaration shape.

## Files in Atalaya that reflect this

- `gradle/libs.versions.toml` — same catalog format, different pins.
- `gradle.properties` — near-verbatim (with NIA's comments preserved as inline notes).
- `gradle/wrapper/gradle-wrapper.properties` — same shape, Gradle 9.4.1.
- `settings.gradle.kts` — same `pluginManagement` and `dependencyResolutionManagement`
  blocks, includes for Atalaya's modules.
- `build.gradle.kts` — same root-plugins-with-apply-false pattern.
- `apps/node/build.gradle.kts` — Android application shape inspired by NIA's
  `app/build.gradle.kts`, simplified (no flavors, no Firebase, no baseline profiles
  for now).
- `packages/*/build.gradle.kts` — Android library shape inspired by NIA's `core/*` modules.

## Required attribution

```
// Adapted from Now in Android (https://github.com/android/nowinandroid)
// Licensed under Apache-2.0 at the time we read it (commit 7d45eae).
// Modifications © Digital Gnosis, AGPL-3.0-only.
```

Headers were not added to Gradle files because they're scaffolding — the
attribution lives in this reference file rather than in every `.gradle.kts`.

## Differences

- **No `build-logic/` convention plugins.** NIA factors AGP setup into
  `nowinandroid.android.application`, `nowinandroid.android.library` etc. Atalaya
  has ten modules; the convention layer adds friction without payoff at this size.
  Revisit when modules cross ~25.
- **`apps/` + `packages/` instead of `:core/:feature`.** Per ADR-0001 — Atalaya is
  multi-platform (Android + JVM hub + web) so an Android-flavored layout doesn't fit.
- **Different version pins.** Bitwarden's pins are slightly newer (Kotlin 2.3.21,
  AGP 9.2.0) and we follow them where they diverge from NIA (Kotlin 2.3.0, AGP 9.0.0)
  because Bitwarden ships to real users on these pins.
- **JUnit 5 not 4.** NIA defaults to JUnit 4. Atalaya standardizes on JUnit 5
  (Jupiter) per the testing setup in our build files. JUnit 4 is still pinned for
  Android instrumented tests where the runner requires it.
- **No Firebase, no flavors, no baseline profiles, no Roborazzi.** Phase 1 ships
  the minimum to compile and run; the rest joins as needs land.

## Notes for next time

- NIA's convention plugins live in `build-logic/convention/`. If module count grows,
  port the `AndroidApplicationConventionPlugin` shape rather than rewriting from scratch.
- NIA uses `compose-bom-alpha`. Atalaya uses the stable BOM (`androidx.compose:compose-bom`).
- NIA pins Compose Material 3 Adaptive separately from the BOM. Skipped here — Phase 1
  doesn't ship adaptive layouts.
- The `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` flag means `projects.packages.coreProtocol`
  works (camelCased from `:packages:core-protocol`). NIA uses this everywhere.
