# Nigel's TODO

Tight list of things waiting on you. Updated as items move.

## Open

### 1. Define DG brand language

Issue: [#1](https://github.com/DigitalGnosis/atalaya/issues/1)

Founder + design deliverable. No deadline, but `core-ui-theme` polish waits on this. A markdown doc with hex codes, font names, and example screens is enough — doesn't have to be Figma.

Covers:
- Color palette (primary, secondary, surface, error in light + dark)
- Typography (display / headline / title / body / label, with chosen typefaces)
- Voice (UI copy tone)
- Logo (wordmark + icon variants)
- Iconography style (outline vs filled, corner radius)

### 2. Confirm the dogfood phone (30 seconds)

Working assumption per ADR-0013: **Samsung Galaxy S21**. Physically check your drawer when convenient — confirm it's actually an S21 and not a different model. Reply to Forge with the actual model name. If it's something different, we supersede ADR-0013 with the real model. The S21 is a great test target either way (Snapdragon 888 NPU + Samsung's aggressive OEM-kill behavior = a stress test that exercises everything).

## Locked recently

- ✅ First dogfood device — Galaxy S21 (pending physical confirmation) (ADR-0013)
- ✅ Crash reporting — self-hosted Sentry on dg-core, opt-in only (ADR-0012)
- ✅ Signing key custody — Bitwarden Secrets Manager, DG-Shared project (ADR-0011)
- ✅ Mesh strategy — Headscale + WireGuard, branded "Atalaya Mesh" (ADR-0010)
- ✅ Federated package strategy (ADR-0009)
- ✅ DG org name — `DigitalGnosis` (PascalCase)
- ✅ Project name — Atalaya
- ✅ License — AGPL-3.0
- ✅ Monorepo + Now-in-Android-style layout (ADR-0001)
- ✅ Target user — normie, founder is first dogfood

## Optional reading (when at a keyboard)

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — full system design
- [`docs/PRD-PHASES.md`](docs/PRD-PHASES.md) — per-phase build plan
- [`docs/phase-1/README.md`](docs/phase-1/README.md) — current phase index

## What Forge is doing without you

- Writing real code starting Phase 1 Step 3 (vertical slice 1: first Compose screen with BaseViewModel)
- Filling M3 CameraX research stub before Step 5
- Generating the gradle wrapper jar (offered to do this from dg-core if Java 17+ is available)
- Spinning up Sentry on dg-core when Phase 1 Step 10 lands
- Engineering work through Step 11 release

You're not blocking any of this.

---

This file updates as items land. Forge will keep it tight.
