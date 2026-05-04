# `packages/core-onboarding`

Hints, tutorials, and first-run experience primitives. The mechanism — apps own the content.

**Phase:** 1 (current)
**Stack:** Kotlin (Android-targeting, but UI-rendering-agnostic)
**Status:** 🔵 skeleton — spec drafted, no code yet

## What it owns

- `Hint` and `HintScope` types
- `Tutorial` (ordered list of hints with completion criteria)
- `OnboardingState` — persistence of seen / dismissed / completed
- `HintsManager` — visibility queries by scope
- `skipAll` toggle for power users

## What it does NOT own

- Tutorial *content* (words, screen IDs) — those live in each consuming app
- UI rendering (Compose components) — apps render hints however they want
- Localization (Phase 5+)

## Dependencies

| On | Why |
|----|-----|
| `kotlinx.serialization` | Persisting onboarding state |
| `kotlinx.coroutines` | Flow-based hint visibility |

## Specs

- Module spec with code examples: [`../../docs/phase-1/modules/core-onboarding.md`](../../docs/phase-1/modules/core-onboarding.md)
- Why this matters (normie target): [`../../docs/phase-1/01-goal.md`](../../docs/phase-1/01-goal.md)

## Consumers

| Module | Use |
|--------|-----|
| [`apps/node`](../../apps/node/) | First-launch tutorial, permission explainers, alert detail hints |
| `apps/control` (Phase 4) | Pairing tutorial, sound-choice walkthrough |
| `apps/hub-web` (Phase 2) | Self-host onboarding for web admin |

## Source layout (planned)

```
packages/core-onboarding/
├── README.md
├── build.gradle.kts          # Phase 1 Step 2
└── src/main/kotlin/dev/digitalgnosis/atalaya/onboarding/
    ├── Hint.kt
    ├── HintScope.kt
    ├── Tutorial.kt
    ├── OnboardingState.kt
    └── HintsManager.kt
```
