# Nigel's TODO

Tight list of things waiting on you. Updated as items move.

## 🔴 Project state — pivot in progress

We caught an architectural flaw on 2026-05-04: the original "phones run Gemma 4 locally" premise doesn't hold — old phones can't actually do that. Pivoted to **phones-as-sensors, hub-as-brain.** Phase 1 vertical slice work is paused. Research is queued. The bones (project name, license, monorepo, BaseViewModel, mesh, federated packages, DG ecosystem) all stay valid.

**Read first:** [`HANDOFF.md`](HANDOFF.md) at the repo root — full pivot context.
**Research queue:** [`docs/phase-1/research/PIVOT-RESEARCH.md`](docs/phase-1/research/PIVOT-RESEARCH.md) — seven items, prioritized.

You don't need to do anything urgent on this. Forge will pick this up next session.

## Open (your items)

### 1. Define DG brand language

Issue: [#1](https://github.com/DigitalGnosis/atalaya/issues/1)

Founder + design deliverable. No deadline. A markdown doc with hex codes, font names, and example screens is enough.

Covers: color palette (light + dark), typography, voice, logo, iconography style.

### 2. Confirm the dogfood phone (30 seconds, low priority now)

Working assumption was Galaxy S21. After the pivot the dogfood criteria changed entirely — what we test against now is camera quality + mesh client behavior + OEM-kill, not on-device inference. So whatever phone you have in your drawer is probably fine. Reply with the actual model when convenient.

## Locked (still valid after pivot)

- ✅ Project name — Atalaya
- ✅ License — AGPL-3.0
- ✅ GitHub org — DigitalGnosis (PascalCase)
- ✅ Monorepo + Now-in-Android-style layout (ADR-0001)
- ✅ BaseViewModel pattern from Bitwarden (ADR-0002)
- ✅ Rule format — NL with structured envelope (ADR-0005) — moves to hub-side
- ✅ Push transport — pluggable AlertTransport (ADR-0006) — interface unchanged
- ✅ DI Hilt (ADR-0008)
- ✅ Federated package strategy (ADR-0009)
- ✅ Mesh — Headscale + WireGuard (ADR-0010) — promoted from Phase 2 to Phase 1 essential
- ✅ Signing keys BWS DG-Shared (ADR-0011)
- ✅ Crash reporting Sentry on dg-core, opt-in (ADR-0012)
- ✅ Target user — normie, founder is first dogfood
- ✅ DG ecosystem integration (CipherWare, dispatch path, dg-helm)

## Stale (under pivot review — DO NOT act on these without the new ADR)

- ⚠️ ADR-0003 — inference runtime (was llama.rn on phone; now hub-side)
- ⚠️ ADR-0004 — frame interval (was deferred for phone benchmarks; now hub-benchmark question)
- ⚠️ ADR-0007 — Min SDK + service (service shape still right; SDK floor may relax)
- ⚠️ ADR-0013 — Galaxy S21 (already superseded by ADR-0014)
- ⚠️ ADR-0014 — S21 corrected (now stale because the dogfood criteria changed)

## Optional reading (when at a keyboard)

- [`HANDOFF.md`](HANDOFF.md) — full pivot context, single source of truth
- [`docs/phase-1/research/PIVOT-RESEARCH.md`](docs/phase-1/research/PIVOT-RESEARCH.md) — research queue, seven items
- [`ARCHITECTURE.md`](ARCHITECTURE.md) — design doc with pivot banner
- [`docs/phase-1/README.md`](docs/phase-1/README.md) — phase index showing PAUSED state

## What Forge is doing on the next session

1. Pick the highest-leverage research item from PIVOT-RESEARCH.md (Item 1: streaming protocol)
2. Time-box 3 days
3. Produce findings, write the new ADR that supersedes ADR-0003
4. Repeat for items 2-7
5. Once all research lands, rewrite ARCHITECTURE.md cleanly, redesign Phase 1 goal/roadmap, resume vertical slice work

You're not blocking any of this. The pivot is captured. Forge picks up where this leaves off.

---

This file updates as items land. Forge will keep it tight.
