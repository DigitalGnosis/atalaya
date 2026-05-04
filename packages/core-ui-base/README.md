# `packages/core-ui-base`

The view-layer convention. Bitwarden's `BaseViewModel` ported verbatim.

**Phase:** 1 (current)
**Stack:** Kotlin + Compose (Android-targeting — Compose Multiplatform later for iOS)
**Status:** 🔵 skeleton — ADR-0002 committed, no code yet

## What it owns

- `BaseViewModel<S, E, A>` — the abstract base class (~91 lines, ports verbatim)
- `BackgroundEvent` and `DeferredBackgroundEvent` marker interfaces
- `EventsEffect` Compose helper for lifecycle-aware event consumption
- `BaseViewModelTest` and `MainDispatcherExtension` for test scaffolding

## Vocabulary (locked by ADR-0002)

- **State (S)** — current screen state, `MutableStateFlow`
- **Event (E)** — VM-to-UI one-shot side effect (navigation, snackbar)
- **Action (A)** — UI-to-VM dispatched intent

This is **Bitwarden's swap.** Most teams call "Action" what they call "Event." Don't fight it — every Atalaya app uses this naming.

## What it does NOT own

- Specific ViewModel implementations (those live per-screen in app modules)
- Compose theming / Material 3 setup (per-app)

## Dependencies

| On | Why |
|----|-----|
| `androidx.lifecycle:lifecycle-viewmodel-ktx` | The `ViewModel` base class |
| `androidx.compose.runtime:runtime` | `EventsEffect` composable |
| `androidx.lifecycle:lifecycle-runtime-compose` | `collectAsStateWithLifecycle` |
| `kotlinx.coroutines` | Channels + StateFlow |

## Attribution

Code in this module is ported from [Bitwarden Android](https://github.com/bitwarden/android) at commit `6ba5159` (read 2026-05-04). Bitwarden's code is GPL-3.0; AGPL-3.0 is a compatible upgrade. Each ported file carries an attribution header.

## Specs

- ADR: [`../../docs/phase-1/decisions/ADR-0002-baseviewmodel-pattern.md`](../../docs/phase-1/decisions/ADR-0002-baseviewmodel-pattern.md)
- Research note: [`../../docs/phase-1/research/M1-bitwarden-baseviewmodel.md`](../../docs/phase-1/research/M1-bitwarden-baseviewmodel.md)
- Reference: [`../../docs/phase-1/references/`](../../docs/phase-1/references/) — Bitwarden citation lands here when first port commits

## Consumers

| Module | Use |
|--------|-----|
| [`apps/node`](../../apps/node/) | Every ViewModel extends `BaseViewModel<S, E, A>` |
| `apps/control` (Phase 4) | Same pattern |

## Source layout (planned)

```
packages/core-ui-base/
├── README.md
├── build.gradle.kts          # Phase 1 Step 2
└── src/main/kotlin/dev/digitalgnosis/atalaya/ui/base/
    ├── BaseViewModel.kt              # ports Bitwarden's
    ├── BackgroundEvent.kt
    ├── EventsEffect.kt                # Compose helper
    └── test/
        ├── BaseViewModelTest.kt
        └── MainDispatcherExtension.kt
```
