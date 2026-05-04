---
adr: 0002
title: View-layer convention — Bitwarden BaseViewModel ported verbatim
status: accepted
date: 2026-05-04
---

# ADR-0002 — View-layer convention

## Context

Atalaya has multiple Android apps (node, control) plus an admin web (separate). Every Android app needs a consistent way to manage screen state, fire events, and produce side effects. Without a shared convention each screen's state management drifts — every contributor reinvents how they handle navigation, side-effects, and configuration changes. Locked in Phase 0: we use Bitwarden's pattern.

M1 research ([`../research/M1-bitwarden-baseviewmodel.md`](../research/M1-bitwarden-baseviewmodel.md)) confirmed the pattern is portable, ~250 lines including tests, zero domain coupling, and license-compatible.

## Decision

We will port Bitwarden's `BaseViewModel<S, E, A>` verbatim into `packages/core-ui-base/` (or equivalent), under our AGPL-3.0 license with attribution headers preserving the GPL-3.0 origin. We adopt their vocabulary swap deliberately:

- **`State` (S)** — current screen state, held in a `MutableStateFlow`, observed by Compose with `collectAsStateWithLifecycle`
- **`Event` (E)** — one-shot side-effect emitted by the VM (navigation, snackbar, open-external-app), consumed via `EventsEffect` composable that respects screen lifecycle
- **`Action` (A)** — UI-dispatched intent, processed serially by the VM's `handleAction`

Critical invariant: **`handleAction` is synchronous**. Async work must launch a coroutine that re-posts a follow-up `Action` via `sendAction`. State mutations are serialized through the single action-consumer coroutine started in `init {}`.

We also port the optional `BackgroundEvent` and `DeferredBackgroundEvent` marker interfaces and the `EventsEffect` composable.

## Alternatives considered

- **Roll our own.** Pros: no vocabulary swap. Cons: violates the operating principle of "no scratch builds when a proven pattern exists." Bitwarden's pattern has shipped to millions of users — battle-tested. Rejected.
- **Use Google's official ViewModel + StateFlow without the action loop.** Pros: zero new abstraction. Cons: every ViewModel reinvents the action dispatch and state-mutation discipline. The serialized-action invariant is the load-bearing guarantee against concurrent state mutation. Rejected.
- **Adopt Orbit MVI or another Kotlin MVI library.** Pros: more features. Cons: external dependency, more API surface, less direct match to our needs. Bitwarden's class is 91 lines and we own it. Rejected.

## Consequences

**Easier:**
- Every Atalaya Android screen has the same shape: a State, an Event sealed type, an Action sealed type, and a ViewModel that extends `BaseViewModel<S, E, A>`. New screens are scaffold-able.
- Testing is uniform — `BaseViewModelTest` ports along, providing `runTest` + `Turbine`-style state/event collection.
- Onboarding contributors: point them at the Bitwarden file and the ADR; the pattern is documented.

**Harder:**
- Vocabulary swap is non-obvious. Every contributor must learn that "Action" = UI-to-VM and "Event" = VM-to-UI. We mitigate by linking the ADR from every BaseViewModel-derived file's KDoc and by having `CONTRIBUTING.md` reference this convention.
- Bitwarden's `BaseViewModelTest.kt` couples to JUnit 5 via Bitwarden's `MainDispatcherExtension`. We port that too (~30 lines).

**New constraints:**
- All Android screens must use this pattern. Custom state containers in feature modules are not accepted (called out in `CONTRIBUTING.md`).
- Async work re-posts actions — if a contributor does async work directly inside `handleAction`, that's a bug.
- Hilt with `@HiltViewModel` is the wiring convention (informs ADR-0008).

## Related

- Supersedes: none
- Superseded by: none
- Informed by: [`../research/M1-bitwarden-baseviewmodel.md`](../research/M1-bitwarden-baseviewmodel.md) (Bitwarden Android, commit `6ba5159`)
- Triggers: ADR-0008 (DI framework — Hilt), and the `packages/core-ui-base/` skeleton in roadmap Step 2
- License lineage: GPL-3.0 → AGPL-3.0 (compatible). Each ported file gets a header attributing Bitwarden + commit hash + GPL-3.0 origin, modifications under AGPL-3.0.
