---
module: M1
title: Bitwarden BaseViewModel pattern
status: stub
opened: 2026-05-04
time_box_ends: 2026-05-07
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

_(fill in as you investigate)_

### Repo state

_(commit hash you read at, last commit date, build status)_

### Architecture summary

_(your description in plain language)_

### Code skeleton extracted

_(the minimal pattern shape with placeholder names)_

### Tests we'd inherit

_(how Bitwarden tests their ViewModels — what we'd port)_

### Disentanglement work needed

_(what depends on Bitwarden-specific code that we don't want)_

## Tried that didn't work

_(important — log dead ends so the next person doesn't repeat them)_

## Recommendation

_(one paragraph, informs ADR-0002)_
