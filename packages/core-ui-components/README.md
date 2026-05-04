# `packages/core-ui-components`

Reusable Compose components shared across Atalaya apps. Hint cards, status badges, alert items, loading states, dialog primitives.

**Phase:** 1 (current)
**Stack:** Kotlin + Compose Material 3
**Status:** 🔵 skeleton

## What it owns

- `AtalayaCard` — branded card variant
- `HintCard` — wraps a `Hint` from `core-onboarding` for display
- `StatusBadge` — armed/disarmed/error
- `LoadingOverlay`, `EmptyState`, `ErrorState`
- `AlertListItem`, `RuleListItem`
- Common dialog shells (confirmation, single-input)
- Permission explainer card layouts

## What it does NOT own

- Theme tokens (those come from `core-ui-theme`)
- Per-screen UI (lives in app modules)
- App-specific composables that aren't reusable

## Dependencies

| On | Why |
|----|-----|
| [`packages/core-ui-theme`](../core-ui-theme/) | Theme tokens for consistent styling |
| [`packages/core-onboarding`](../core-onboarding/) | `Hint` type for `HintCard` |
| [`packages/core-protocol`](../core-protocol/) | `Alert`, `Rule` types for list items |
| `androidx.compose.material3` | Material 3 baseline |

## Consumers

| Module | Use |
|--------|-----|
| [`apps/node`](../../apps/node/) | Every screen consumes these instead of rolling its own card/list/dialog |
| `apps/control` (Phase 4) | Same components, same brand |

## Why a shared components module

Without this, every screen reinvents the same card layout. Drift creeps in. Updating the brand color in one card and forgetting another is a category of bug we eliminate by centralizing. New components land here; per-screen variations stay per-screen.

## Source layout (planned)

```
packages/core-ui-components/
├── README.md
├── build.gradle.kts          # Phase 1 Step 2
└── src/main/kotlin/dev/digitalgnosis/atalaya/ui/components/
    ├── cards/
    │   ├── AtalayaCard.kt
    │   └── HintCard.kt
    ├── status/
    │   ├── StatusBadge.kt
    │   └── EmptyState.kt
    ├── lists/
    │   ├── AlertListItem.kt
    │   └── RuleListItem.kt
    ├── dialogs/
    │   ├── ConfirmationDialog.kt
    │   └── InputDialog.kt
    └── permissions/
        └── PermissionExplainerCard.kt
```
