---
module: core-onboarding
status: stub
phase: 1
---

# `core-onboarding` — Hints, tutorials, and first-run experience

## Why this exists

Atalaya targets normies — people who don't run Tailscale, don't read READMEs, and just want to install an APK and have their old phone watch the front door. That target requires the app to *teach itself* — guided permission flows, contextual hints, optional tutorials for first-time users. Power users skip everything in settings.

This module owns the primitives. The content (the actual words, screen sequences, hint locations) lives in each consuming app because tutorials are app-specific.

## Responsibility

`core-onboarding` owns:

- The shape of a `Tutorial` (an ordered list of steps with completion criteria)
- The shape of a `Hint` (a contextual pointer: where it attaches, what it says, when it's been seen)
- Persistence: which hints/tutorials a user has seen, dismissed, or completed
- A skip-everything escape hatch (a single setting toggle)
- Versioning: hint IDs are versioned so adding a feature doesn't reshow old tutorials

It does NOT own:

- The actual tutorial content (words, screenshots, screen IDs) — those live in each app
- UI rendering — Compose components consuming the API live in each app's UI module
- Localization (Phase 5+ concern; v1 ships English)

## Public API

```kotlin
package dev.digitalgnosis.atalaya.onboarding

@Serializable
@JvmInline
value class HintId(val value: String)

@Serializable
@JvmInline
value class TutorialId(val value: String)

@Serializable
data class Hint(
    val id: HintId,
    val title: String,
    val body: String,
    /** UI scope where this hint attaches — see HintScope. */
    val scope: HintScope,
    /** Increment when the hint's content meaningfully changes; treated as "unseen" again. */
    val version: Int = 1,
)

@Serializable
sealed interface HintScope {
    @Serializable data class Screen(val screenId: String) : HintScope
    @Serializable data class Element(val screenId: String, val elementId: String) : HintScope
    @Serializable data object FirstLaunch : HintScope
    @Serializable data object FirstAlert : HintScope
}

interface OnboardingState {
    suspend fun isSeen(id: HintId, version: Int): Boolean
    suspend fun markSeen(id: HintId, version: Int)
    suspend fun isCompleted(id: TutorialId): Boolean
    suspend fun markCompleted(id: TutorialId)
    /** User has chosen to skip all tutorials. Power-user escape hatch. */
    val skipAll: StateFlow<Boolean>
    suspend fun setSkipAll(skip: Boolean)
}

interface HintsManager {
    /** Returns hints visible right now for the given scope, filtered by version + seen state. */
    fun visibleHints(scope: HintScope): Flow<List<Hint>>
    suspend fun dismiss(id: HintId, version: Int)
}
```

A `Tutorial` is just an ordered list of `HintId`s plus a `completionCriteria` enum (`onLastDismissed`, `onTargetActionTaken`, etc.).

## Dependencies

| On | Why |
|----|-----|
| `kotlinx.serialization` | Persisting onboarding state to disk |
| `kotlinx.coroutines` | Flow-based hint visibility |

Nothing app-specific. Nothing Compose. Renderers are app-side.

## Consumers

| App | Use |
|-----|-----|
| `apps/node` | First-launch tutorial (permissions, model download, first rule). Contextual hints in settings ("why do we ask for camera permission?"). |
| `apps/control` (Phase 4) | Different first-launch flow (pair with hub, choose alert sounds). |
| `apps/hub-web` (Phase 2) | Web admin onboarding for self-hosters who DO want to dive in (different audience, different content). |

## Code example (illustrative — what `apps/node` consumes look like)

A Compose component on the FirstLaunch screen wires up the manager and renders whatever the manager says is visible right now:

```kotlin
// apps/node/.../FirstLaunchScreen.kt
@Composable
fun FirstLaunchScreen(viewModel: FirstLaunchViewModel = hiltViewModel()) {
    val hints by viewModel.firstLaunchHints.collectAsStateWithLifecycle()
    val skipAll by viewModel.skipAll.collectAsStateWithLifecycle()

    if (skipAll) {
        // Power-user path — show only the bare permission prompts
        return BarePermissionsFlow(viewModel)
    }

    Column {
        hints.forEach { hint ->
            HintCard(
                hint = hint,
                onDismiss = { viewModel.trySendAction(FirstLaunchAction.DismissHint(hint.id)) },
            )
        }
        // ...the actual screen content below the hints
    }
}
```

Each app declares its own hint content as a constant set:

```kotlin
// apps/node/.../onboarding/NodeHints.kt
object NodeHints {
    val WelcomeFirstLaunch = Hint(
        id = HintId("node.first-launch.welcome"),
        title = "Welcome to Atalaya",
        body = "Atalaya turns this phone into a smart security camera. Tap continue and we'll walk through three permissions.",
        scope = HintScope.FirstLaunch,
    )

    val WhyCameraPermission = Hint(
        id = HintId("node.permissions.camera-explainer"),
        title = "Why we need camera",
        body = "Atalaya watches your space using this phone's camera. Frames are described by AI on the device — they never leave your phone.",
        scope = HintScope.Screen("permissions"),
    )

    val FirstAlertCelebration = Hint(
        id = HintId("node.first-alert.celebrate"),
        title = "First alert!",
        body = "Atalaya saw something matching your rule. Tap to see what it caught.",
        scope = HintScope.FirstAlert,
    )

    val all = listOf(WelcomeFirstLaunch, WhyCameraPermission, FirstAlertCelebration)
}
```

## Open questions

- **Persistence backend.** SQLite via Room? DataStore Preferences? Phase 1 leans DataStore Preferences — simplest, no schema migrations, fine for a small set of hint IDs and seen versions. Resolve in implementation.
- **Tutorial sequencing.** Some tutorials must run before others (e.g. permissions before model download). Phase 1 hardcodes the order in app code; v2 makes it data-driven if needed.
- **A/B testing onboarding flows.** Out of scope for v1. Document only.
- **Localization.** v1 is English-only. The `title`/`body` fields are plain strings, not string resource IDs — Phase 5+ swaps to resources when we localize.

## Test surface

- **State persistence round-trip:** mark seen, restart app, verify still seen.
- **Versioning:** mark seen at v1, bump to v2, verify shown again.
- **SkipAll:** with skip on, `visibleHints` returns empty for all scopes.
- **Smoke test for each app's hint set:** every `HintId` is unique, every scope is reachable from the app's screens.

## Versioning notes

`core-onboarding` follows semver. Hint shapes and the `OnboardingState` interface are stable surface. New scopes added as new sealed subtypes — old apps ignore unknown scopes.
