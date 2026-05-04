# Building Atalaya

This is the Phase 1 Step 2 build scaffold. It produces a no-op debug APK from
`apps/node` and compiles every `packages/*` module as an empty library. Real
features land with the Phase 1 Step 3 vertical slice.

## Prerequisites

| Tool | Version | Why |
|------|---------|-----|
| JDK  | 21 (Temurin recommended) | AGP 9.x compiles with JDK 21 by default. Set `JAVA_HOME`. |
| Android SDK | 35 | Per [ADR-0007](docs/phase-1/decisions/ADR-0007-min-sdk-and-service.md). |
| Android NDK | 27.2.12479018 | Per [ADR-0007](docs/phase-1/decisions/ADR-0007-min-sdk-and-service.md). Required by `core-ml`. |
| Gradle | 9.4.1 (via wrapper) | Pinned in `gradle/wrapper/gradle-wrapper.properties`. |

Make sure `local.properties` exists with `sdk.dir` and `ndk.dir` pointing at your
SDK root, or set `ANDROID_HOME` / `ANDROID_NDK_HOME`.

## First-time setup — generate the wrapper jar

The Gradle wrapper jar (`gradle/wrapper/gradle-wrapper.jar`) is a binary that
this repository intentionally does not commit yet. Generate it once:

```bash
# Requires a system Gradle 8+ on PATH. Install via SDKMAN:
#   curl -s "https://get.sdkman.io" | bash
#   sdk install gradle 9.4.1
gradle wrapper --gradle-version=9.4.1 --distribution-type=bin
```

After that runs, `./gradlew` works from a fresh checkout.

## Common commands

```bash
# Build a debug APK
./gradlew :apps:node:assembleDebug

# Build everything
./gradlew assemble

# Unit tests (JUnit 5)
./gradlew test

# Android lint
./gradlew lint
```

The debug APK lands at
`apps/node/build/outputs/apk/debug/node-debug.apk`.

## What is and isn't wired in this scaffold

Wired:
- All ten Gradle modules listed in `settings.gradle.kts`.
- Compose, Hilt, KSP, Kotlinx Serialization plugins applied where ADRs say.
- `apps/node` declares the ADR-0007 permissions in its manifest and ships a
  `@HiltAndroidApp` `AtalayaApplication` plus an empty `MainActivity`.

Not yet wired (intentional):
- `core-ml` declares NDK ABI filters but no `externalNativeBuild { cmake { ... } }`.
  The CMakeLists and the extracted llama.rn JNI sources land in Step 3.
- `core-observability` does not yet depend on `cipherware-sdk-android`. It will
  once that artifact is published.
- No CI yet. CI lands separately.
- No release signing config. Debug builds use the auto-generated debug key.

## Reference: where conventions came from

See [`docs/phase-1/references/now-in-android.md`](docs/phase-1/references/now-in-android.md)
and [`docs/phase-1/references/bitwarden-android.md`](docs/phase-1/references/bitwarden-android.md)
for the source-by-source attribution of what was ported into this scaffold.
