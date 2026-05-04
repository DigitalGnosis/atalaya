---
adr: 0009
title: Federated package strategy — packages stay internal until proven elsewhere, then extract to DG-published repos
status: accepted
date: 2026-05-04
---

# ADR-0009 — Federated package strategy

## Context

Digital Gnosis ships multiple Android apps that share architecture, infrastructure, and likely future patterns: Atalaya, Connect, Route 33, Dispatch, AndyClaw, StacheApp, ChessCoach, and more. Each app today reinvents pieces — its own theme code, its own Hilt module patterns, its own approach to FCM and observability — except where one piece has already been extracted to a shared library (like `cipherware-sdk-android`).

The question: how should Atalaya's packages relate to the broader DG codebase?

Three architectures are viable:

1. **Per-app monorepo (current Atalaya).** Each app's repo has its own `packages/`. Cross-app sharing requires copy or republish.
2. **Org-level monorepo.** One giant `digitalgnosis/` repo holds every app and every package. Maximum reuse, but a unified repo for Atalaya + Connect + Route 33 + everything else gets unwieldy fast and unifies release cadences awkwardly.
3. **Federated packages.** Each app stays its own repo. Shared packages live in their own DG-owned repos, published as Maven artifacts. Apps depend on shared packages by version. `cipherware-sdk-android` already proves this pattern.

## Decision

We adopt **federated packages**. Specifically:

1. **Atalaya's `packages/` stay internal to the Atalaya repo until a second DG app wants the package.**
2. **When a second DG app wants a package**, we extract it: create a new `DigitalGnosis/dg-<name>` repo, move the code, publish as a Maven artifact, both apps depend on it by version.
3. **Apps never depend on each other directly.** Cross-app sharing happens only through extracted packages.
4. **Existing DG-published libraries** (`cipherware-sdk-android`) are consumed directly — Atalaya's `core-observability` is a thin facade over the published SDK, not a re-implementation.
5. **The extraction trigger is concrete demand**, not speculative reusability. If only Atalaya uses `core-rules`, it stays in Atalaya. The day Connect wants the rule engine, we extract.

The pattern's first proof is `cipherware-sdk-android`: separate DG repo, Apache + DG-internal license posture, consumed by Connect today, consumed by Atalaya going forward.

## Alternatives considered

- **Org-level monorepo.** Pros: maximum visibility, atomic cross-app refactors, single PR can update everything. Cons: build times balloon, release cadences couple, Bazel/Gradle composite-build setup is significant infrastructure. Rejected — we're not at the scale where the trade-off pays off, and the federated pattern already works.
- **Per-app monorepo with copy-on-share.** Pros: simplest, no publishing infra. Cons: drift between copies is inevitable, security patches require N updates. Rejected as long-term strategy though it's where Atalaya starts.
- **Single-repo "starter kit"** (one repo with all DG Android conventions; new apps fork it). Pros: convention enforcement on day one. Cons: forks drift, upstream improvements don't propagate. Rejected.

## Consequences

**Easier:**
- Atalaya ships independently. Phase 1 doesn't need to coordinate with any other DG app's release.
- `cipherware-sdk-android` consumption is already proven; we follow that template.
- Extraction is a deliberate, measurable event ("Connect now wants core-ui-theme") not a speculative architecture exercise.
- Apps can evolve their conventions internally before we crystallize them into a shared package.

**Harder:**
- DG needs a Maven artifact server eventually. For Phase 1 we can use GitHub Packages (free for public repos under the org); for production paid tier or self-hosted Sonatype Nexus when scale demands.
- Versioning discipline matters. Once `dg-ui-theme` is consumed by three apps, breaking changes hurt three apps. Semver matters.
- Cross-app refactors take more PRs (one per consuming app) instead of one atomic monorepo commit.

**New constraints:**
- Apps in `apps/` must NEVER import from another app. Cross-app comms strictly through `packages/core-protocol` (per ADR-0001) or — once extracted — through DG-shared packages.
- Atalaya's `packages/core-observability` is the shape of a future shared `dg-observability` if any other DG app adopts CipherWare wrapping the same way. For now it's internal.
- Likely first extraction candidates (in priority order if demand surfaces): `core-ui-theme` (when DG brand lands and Connect/Dispatch/Route 33 want it), `core-protocol` (when a non-Atalaya app needs to talk to the Atalaya hub), `core-onboarding` (whenever any DG app wants normie-friendly onboarding).

## Related

- Supersedes: none
- Superseded by: none
- Informed by: existence of `cipherware-sdk-android` as the proof; the DG ecosystem section of [ARCHITECTURE.md](../../ARCHITECTURE.md#atalaya-in-the-digital-gnosis-ecosystem)
- Triggers: Maven artifact server setup whenever first extraction lands (Phase 2+); package versioning conventions; potential `dg-ui-theme` extraction once DG brand exists
