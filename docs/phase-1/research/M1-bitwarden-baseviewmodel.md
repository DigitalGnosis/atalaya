---
module: M1
title: Bitwarden BaseViewModel pattern
status: findings
opened: 2026-05-04
time_box_ends: 2026-05-04
sources:
  - https://github.com/bitwarden/android
related_adrs:
  - ADR-0002
  - ADR-0008
---

# M1 — Bitwarden BaseViewModel pattern

## Why this matters

Atalaya has multiple apps (node, hub admin, control). Every one of them needs a consistent way to manage screen state, fire events, and produce side effects. We picked Bitwarden's pattern as the convention so we don't reinvent. This research validates we can port it cleanly.

## Specific questions

1. Where does Bitwarden's `BaseViewModel` live in the repo? Path?
2. Does it use MVI-style `state / event / effect` triples?
3. Is it Kotlin Flow-based? StateFlow? SharedFlow?
4. How are events delivered from the UI? `sealed class` events?
5. How are effects (one-shot side effects like navigation, snackbars) modeled?
6. What testing affordances does it ship — `Turbine`? `runTest`?
7. How does Hilt wire ViewModels?
8. What's the dependency surface — what would we be pulling in?
9. Are there any Bitwarden-specific assumptions (their data layer, their auth) we'd have to disentangle?

## Findings

### Repo state

- Repo: `https://github.com/bitwarden/android`
- Commit read: `6ba51599222a54957e2e0a3c637bc2e089940424`
- Commit date: `2026-05-04 14:34:14 -0500`
- Commit subject: `Chore: Remove the unused register API (#6870)`
- Clone command used: `git clone --depth 1 https://github.com/bitwarden/android.git`
- License: GPL-3.0 (port to Atalaya AGPL-3.0 is license-compatible — AGPL is a permitted strong-copyleft target for GPL-3.0 code).

### Architecture summary

Bitwarden uses a strict, opinionated unidirectional MVI variant where every ViewModel is parameterized over three types: a `State` (S), an `Event` (E), and an `Action` (A). They deliberately do **not** use the classic MVI three-letter "state/event/effect" naming — what most teams call "events from the UI" they call **Actions**, and what most teams call "one-shot effects from the VM" they call **Events**. Once you internalize that swap the model is conventional MVI: UI dispatches actions, the VM reduces actions into new state synchronously, and the VM emits events for one-shot side effects (navigation, opening external apps, etc.).

The base class lives at `ui/src/main/kotlin/com/bitwarden/ui/platform/base/BaseViewModel.kt` in a dedicated `:ui` Gradle module (separate from `:app`, `:core`, `:annotation`). It's a 91-line abstract class with no Bitwarden-domain dependencies — it imports only `androidx.lifecycle.ViewModel`, `viewModelScope`, and `kotlinx.coroutines` primitives. State is held in a `MutableStateFlow<S>` exposed read-only as `stateFlow: StateFlow<S>`. Events flow through an unbounded `Channel<E>` exposed as a single-consumer `Flow<E>` via `receiveAsFlow()` (this is intentional — events are one-shot, multi-collect is a bug). Actions flow through a second unbounded `Channel<A>` whose internal end is consumed by a coroutine started in `init {}` that calls the abstract `handleAction(action: A)` for every incoming action.

The action loop is the key invariant: `handleAction` is `protected abstract fun` and is documented to be **synchronous**. Async work is not done inside the handler — instead the handler kicks off a coroutine that, when it completes, posts a follow-up action back through `sendAction`/`trySendAction`. This means state mutations are always serialized through the single action-consumer coroutine and never race with each other. This is a stricter invariant than most MVI implementations enforce and it eliminates a whole class of "two updates ran concurrently" bugs.

UI consumption is split cleanly. Compose screens collect state with `viewModel.stateFlow.collectAsStateWithLifecycle()` and consume events through a Bitwarden-specific composable helper, `EventsEffect` (at `ui/src/main/kotlin/com/bitwarden/ui/platform/base/util/EventsEffect.kt`), which by default discards events fired while the screen is below `RESUMED` to prevent duplicate-navigation bugs. Two opt-in marker interfaces, `BackgroundEvent` and `DeferredBackgroundEvent` (in `BackgroundEvent.kt` next to `BaseViewModel.kt`), let an individual event type bypass the lifecycle filter or queue itself for resume. Actions are dispatched from the UI via `viewModel.trySendAction(...)`, typically routed through a per-screen `Handler` object created with `rememberFooHandler(viewModel)` that lifts each lambda into an action send.

Hilt wiring is the standard AndroidX template: `@HiltViewModel class FooViewModel @Inject constructor(savedStateHandle: SavedStateHandle, deps...) : BaseViewModel<S, E, A>(initialState = ...)`. `SavedStateHandle` is used not just for navigation args but as a state-persistence channel — many ViewModels write `stateFlow.onEach { savedStateHandle[KEY_STATE] = it }.launchIn(viewModelScope)` in their `init` block so process death restores state. This relies on the `State` type being `@Parcelize`d.

### Code skeleton extracted

The full BaseViewModel (essentially copyable as-is, ~90 lines):

```kotlin
package com.atalaya.ui.base // adapt namespace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * S = screen State, E = one-shot Event (navigation, etc.), A = Action from UI.
 */
abstract class BaseViewModel<S, E, A>(
    initialState: S,
) : ViewModel() {
    protected val mutableStateFlow: MutableStateFlow<S> = MutableStateFlow(initialState)
    private val eventChannel: Channel<E> = Channel(capacity = Channel.UNLIMITED)
    private val internalActionChannel: Channel<A> = Channel(capacity = Channel.UNLIMITED)

    protected val state: S get() = mutableStateFlow.value
    val stateFlow: StateFlow<S> = mutableStateFlow.asStateFlow()
    val eventFlow: Flow<E> = eventChannel.receiveAsFlow()
    val actionChannel: SendChannel<A> = internalActionChannel

    init {
        viewModelScope.launch {
            internalActionChannel.consumeAsFlow().collect { action ->
                handleAction(action)
            }
        }
    }

    /** MUST be synchronous. Async work should re-post a follow-up action. */
    protected abstract fun handleAction(action: A)

    fun trySendAction(action: A) { actionChannel.trySend(action) }
    protected suspend fun sendAction(action: A) { actionChannel.send(action) }
    protected fun sendEvent(event: E) { viewModelScope.launch { eventChannel.send(event) } }
}
```

Optional companion (also tiny — recommend porting both):

```kotlin
/** Event types that should bypass the resumed-only lifecycle filter. */
interface BackgroundEvent

/** Like BackgroundEvent but defers handling until screen is RESUMED. */
interface DeferredBackgroundEvent : BackgroundEvent
```

A representative consumer (paraphrased from `CheckEmailViewModel.kt`):

```kotlin
@HiltViewModel
class CheckEmailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<CheckEmailState, CheckEmailEvent, CheckEmailAction>(
    initialState = savedStateHandle[KEY_STATE]
        ?: CheckEmailState(email = savedStateHandle.toCheckEmailArgs().emailAddress),
) {
    init {
        stateFlow.onEach { savedStateHandle[KEY_STATE] = it }.launchIn(viewModelScope)
    }

    override fun handleAction(action: CheckEmailAction) = when (action) {
        CheckEmailAction.BackClick        -> sendEvent(CheckEmailEvent.NavigateBack)
        CheckEmailAction.OpenEmailClick   -> sendEvent(CheckEmailEvent.NavigateToEmailApp)
        CheckEmailAction.ChangeEmailClick -> sendEvent(CheckEmailEvent.NavigateBack)
    }
}

@Parcelize
data class CheckEmailState(val email: String) : Parcelable

sealed class CheckEmailEvent {
    data object NavigateBack       : CheckEmailEvent()
    data object NavigateToEmailApp : CheckEmailEvent()
}

sealed class CheckEmailAction {
    data object BackClick        : CheckEmailAction()
    data object ChangeEmailClick : CheckEmailAction()
    data object OpenEmailClick   : CheckEmailAction()
}
```

Compose-side wiring (paraphrased from `CheckEmailScreen.kt`):

```kotlin
@Composable
fun CheckEmailScreen(
    onNavigateBack: () -> Unit,
    viewModel: CheckEmailViewModel = hiltViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    EventsEffect(viewModel) { event ->
        when (event) {
            CheckEmailEvent.NavigateBack       -> onNavigateBack()
            CheckEmailEvent.NavigateToEmailApp -> /* intent */ Unit
        }
    }
    // render `state`, dispatch with viewModel.trySendAction(...)
}
```

Full source citations (commit `6ba5159`):

- `ui/src/main/kotlin/com/bitwarden/ui/platform/base/BaseViewModel.kt`
- `ui/src/main/kotlin/com/bitwarden/ui/platform/base/BackgroundEvent.kt`
- `ui/src/main/kotlin/com/bitwarden/ui/platform/base/util/EventsEffect.kt`
- `ui/src/testFixtures/kotlin/com/bitwarden/ui/platform/base/BaseViewModelTest.kt`
- `ui/src/testFixtures/kotlin/com/bitwarden/ui/platform/base/MainDispatcherExtension.kt`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/auth/feature/checkemail/CheckEmailViewModel.kt`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/auth/feature/preventaccountlockout/PreventAccountLockoutViewModel.kt`
- `app/src/main/kotlin/com/x8bit/bitwarden/ui/auth/feature/expiredregistrationlink/ExpiredRegistrationLinkViewModel.kt`
- `app/src/test/kotlin/com/x8bit/bitwarden/ui/auth/feature/checkemail/CheckEmailViewModelTest.kt`

### Tests we'd inherit

Bitwarden ships a small testFixtures harness that ports cleanly:

- **`BaseViewModelTest`** — abstract JUnit-5 base class. Registers `MainDispatcherExtension` and provides a `stateEventFlow { stateTurbine, eventTurbine -> ... }` helper that spins up Turbine `ReceiveTurbine`s for both flows in a single `turbineScope`. ~28 lines total.
- **`MainDispatcherExtension`** — a JUnit-5 `Extension` (Before/After All + Each) that calls `Dispatchers.setMain(UnconfinedTestDispatcher())` and resets it. Lets ViewModels run their `viewModelScope.launch` action-consumer immediately and synchronously inside `runTest { }`. ~45 lines.
- **Per-VM test pattern** — each ViewModel test extends `BaseViewModelTest`, uses `runTest`, calls `viewModel.eventFlow.test { trySendAction(...); awaitItem() }` (Turbine), and constructs the VM directly with a `SavedStateHandle()` and mocked deps (MockK).

The full test stack inherited is: **JUnit 5 (Jupiter) + Turbine + MockK + kotlinx-coroutines-test + Robolectric** (Robolectric is only needed for Compose tests, not pure ViewModel tests). Both `BaseViewModelTest` and `MainDispatcherExtension` are ~75 lines combined and have no Bitwarden-specific imports — copy them straight across.

### Disentanglement work needed

The good news: `BaseViewModel.kt` itself has **zero** Bitwarden-specific dependencies. Every import is from `androidx.lifecycle` or `kotlinx.coroutines`. The same is true for `BackgroundEvent.kt` and `MainDispatcherExtension.kt`. These three files port verbatim with only a package rename.

`EventsEffect.kt` depends on `androidx.lifecycle.compose.LocalLifecycleOwner` and Compose runtime — no Bitwarden internals. Also ports verbatim.

`BaseViewModelTest.kt` depends on Turbine and JUnit 5 only. Verbatim.

What we **don't** want from the surrounding `:ui` module and must avoid pulling in:

- `BitwardenScaffold`, `BitwardenTopAppBar`, `BitwardenTheme`, `BitwardenString`, `BitwardenDrawable` — Bitwarden's design system. Atalaya needs its own.
- `IntentManager` / `LocalIntentManager` — their app-shell abstraction for launching external apps. Useful pattern but separate concern from the VM base.
- `AuthRepository`, `VaultRepository`, etc. — domain repositories injected into many of their example VMs. Strip these from any sample we adapt; they're not part of the pattern, just the app.
- `BitwardenAccountSwitcher`, `BaseRobolectricTest`, `BaseComposeTest` — bigger test fixtures coupled to their navigation graph and design system. Don't port.
- `kotlinx.collections.immutable`, Glide, ML Kit, ZXing, CameraX, `com.bumptech.glide` — heavy deps in their `:ui/build.gradle.kts` we should NOT inherit. They're for Bitwarden's QR / autofill / image features, not for the VM pattern itself.

The dependency surface that **is** required for the pattern itself:

- `androidx.lifecycle:lifecycle-viewmodel-ktx` (ViewModel + viewModelScope)
- `androidx.lifecycle:lifecycle-runtime-compose` (`collectAsStateWithLifecycle`, `LocalLifecycleOwner`)
- `androidx.hilt:hilt-navigation-compose` + `com.google.dagger:hilt-android` + `dagger.hilt.android.lifecycle.HiltViewModel`
- `org.jetbrains.kotlinx:kotlinx-coroutines-core`
- `androidx.compose.runtime` (only if you want `EventsEffect`)
- Test: `app.cash.turbine:turbine`, `org.junit.jupiter:*`, `io.mockk:mockk`, `org.jetbrains.kotlinx:kotlinx-coroutines-test`

That's 6 production-dep coordinates + 4 test-dep coordinates — already in any modern Compose+Hilt Android project. Zero net-new dependencies for a project that's already on Compose+Hilt.

Bitwarden-specific assumptions baked into the *pattern itself*: **none**. The pattern is a clean, decoupled, MVI-style base. The only thing to be deliberate about is naming — adopting their **Action / Event** vocabulary rather than the more common "Event / Effect" so docs and code stay consistent across Atalaya apps. ADR-0002 should pin that vocabulary.

## Tried that didn't work

n/a — the pattern was found on the first search at `ui/src/main/kotlin/com/bitwarden/ui/platform/base/BaseViewModel.kt` exactly where the Bitwarden module structure suggested. No dead ends.

## Recommendation

Adopt Bitwarden's BaseViewModel pattern verbatim for Atalaya's view layer in ADR-0002, with three notes: (1) port `BaseViewModel.kt`, `BackgroundEvent.kt`, `EventsEffect.kt`, `BaseViewModelTest.kt`, and `MainDispatcherExtension.kt` — five files, ~250 lines total — into a shared `:ui-base` module under an Atalaya namespace, with package rename only and an SPDX header documenting the GPL-3.0 → AGPL-3.0 derivation from `bitwarden/android@6ba5159`; (2) commit explicitly to the **Action / Event** naming (UI-to-VM = Action, VM-to-UI one-shot = Event, screen state = State) so the node, hub, and control apps all share vocabulary; and (3) enforce the synchronous-`handleAction` invariant in code review — async work re-posts a follow-up action, never mutates state from inside a launched coroutine. The pattern has zero Bitwarden domain coupling, costs no net-new dependencies on a Compose+Hilt project, and gives us the persistence-via-`SavedStateHandle` and lifecycle-aware-event-delivery wins for free.
