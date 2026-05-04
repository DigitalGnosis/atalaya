---
project: bitwarden-android
url: https://github.com/bitwarden/android
license: GPL-3.0-only
read_at_commit: 6ba51599222a54957e2e0a3c637bc2e089940424
read_at_date: 2026-05-04
ported_in_atalaya:
  - gradle/libs.versions.toml
  - gradle/wrapper/gradle-wrapper.properties
  - apps/node/build.gradle.kts
---

# Reference — `bitwarden-android`

## What it is

Bitwarden's official Android password manager. Modern Android, Kotlin + Compose,
shipping to real users. It's also the source of Atalaya's BaseViewModel pattern
(per [ADR-0002](../decisions/ADR-0002-baseviewmodel-pattern.md)) — that port is
its own commit; here we cite only what was lifted into the *build* configuration.

## License

GPL-3.0-only — compatible with Atalaya's AGPL-3.0 (AGPL is an AGPL-compatible
upgrade of GPL-3.0). Confirmed in
[`docs/phase-1/references/README.md`](README.md#license-compatibility).

## What we ported

Version pins. Where Bitwarden and Now in Android disagreed, we preferred Bitwarden's
pins because they're battle-tested in production: Kotlin 2.3.21, AGP 9.2.0,
Hilt 2.59.2, KSP 2.3.6, Gradle wrapper 9.4.1, Compose BOM 2026.04.01, JUnit 6.0.3,
Turbine 1.2.1, MockK 1.14.9, CameraX 1.6.0, Lifecycle 2.10.0, Activity 1.13.0,
Navigation 2.9.8. Also: the new AGP 9 SDK declaration DSL pattern (`compileSdk = N`
read from the catalog via `libs.versions.compileSdk.get().toInt()`).

## Files in their repo we read

- `gradle/libs.versions.toml` — version catalog. Atalaya copied the *pin levels*
  for shared dependencies; Bitwarden's catalog also declares many things Atalaya
  doesn't need yet (Glide, Firebase Crashlytics, Bitwarden SDK, billing, ML kit).
- `gradle/wrapper/gradle-wrapper.properties` — Gradle 9.4.1 distribution.
  Used directly.
- `app/build.gradle.kts` — Bitwarden's Android application module. Atalaya borrowed
  the AGP 9 idiom of reading SDK levels from the catalog
  (`libs.versions.compileSdk.get().toInt()`) but did not port Bitwarden's signing
  config, flavor matrix, Crashlytics, or `user.properties`/`ci.properties` plumbing.

## Files in Atalaya that reflect this

- `gradle/libs.versions.toml` — version pin levels for shared dependencies.
- `gradle/wrapper/gradle-wrapper.properties` — Gradle 9.4.1.
- `apps/node/build.gradle.kts` — read-SDK-from-catalog idiom; minSdk/targetSdk/compileSdk
  resolved from `libs.versions.*`.

## Required attribution

```
// Adapted from Bitwarden Android (https://github.com/bitwarden/android)
// Licensed under GPL-3.0-only at the time we read it (commit 6ba5159).
// Modifications © Digital Gnosis, AGPL-3.0-only.
```

Attribution will land in source files when the BaseViewModel port commits;
build-config files cite via this reference doc rather than per-file headers.

## Differences

- **No flavors.** Bitwarden ships `standard` (Play) and `fdroid` flavors plus
  `beta`/`debug`/`release` build types. Atalaya ships a single AOSP debug/release
  pair for Phase 1.
- **No Crashlytics/Firebase.** Atalaya's observability path is CipherWare via
  `core-observability`, not Firebase. ADR-0008 noted Hilt; observability is
  separately decided.
- **No `user.properties` / `ci.properties`** plumbing yet. May come back when
  signing config and CI land.
- **No Room.** Atalaya's persistence story is per-app; Bitwarden's monolithic
  Room database doesn't fit our packages-vs-apps split.

## Notes for next time

- Bitwarden's `compileSdk { version = release(libs.versions.compileSdk.get().toInt()) }`
  block is AGP 9's new DSL but is currently *inside* `configure<ApplicationExtension> { ... }`
  rather than the conventional `android { ... }` block. Atalaya kept the conventional
  `android { ... }` form; both compile.
- Bitwarden is on `compileSdk = 36` and `minSdk = 29`. Atalaya is on `35`/`26` per
  ADR-0007 (Atalaya's audience includes older mid-range phones; Bitwarden's tighter
  pin is fine for a security-first vault but not for a watcher targeting cheap
  spare hardware).
- Bitwarden's `kotlinxKover` and Detekt setup are skipped in Phase 1; revisit when
  we add static analysis.
