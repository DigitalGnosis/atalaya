# Atalaya Docs

Welcome. This directory holds everything that isn't code.

## Top-level reference

| Doc | What's inside |
|-----|---------------|
| [`../ARCHITECTURE.md`](../ARCHITECTURE.md) | The full system design. Three planes, role-based nodes, AI weaponization, competitive map. Read this first. |
| [`MODULE-MAP.md`](MODULE-MAP.md) | Visual dependency graph between apps and packages. Mermaid diagrams + folder layout. |
| [`PRD-PHASES.md`](PRD-PHASES.md) | The per-phase build plan. Goal, success criteria, research targets, time-box per phase. |
| [`framework/`](framework/) | The phase documentation framework itself. Templates, conventions, how to start a new phase. |

## Phases

Each phase has its own directory with a consistent shape so you can land in any phase and find what you need fast.

| Phase | Status | Folder |
|-------|--------|--------|
| Phase 0 — Foundations | ✅ Shipped | _(see initial commit)_ |
| **Phase 1 — Single camera node** | 🔵 Open | [`phase-1/`](phase-1/) |
| Phase 2 — Hub + multi-node | ⚪ Pending | _(future)_ |
| Phase 3 — Role expansion | ⚪ Pending | _(future)_ |
| Phase 4 — Control app | ⚪ Pending | _(future)_ |
| Phase 5 — Cloud hub | ⚪ Pending | _(future)_ |
| Phase 6 — Hardware integrations | ⚪ Pending | _(future)_ |

## Phase folder shape

Every phase folder has the same five sections so navigation is predictable:

```
phase-N/
├── README.md          # The phase index. Start here.
├── 01-goal.md         # What done looks like. Success criteria.
├── 02-roadmap.md      # The ordered steps to get there.
├── research/          # Findings — mutable, time-boxed, source-cited.
├── decisions/         # ADRs — immutable commitments, dated, linked.
├── modules/           # Module specs with code examples.
└── references/        # Exactly which repos / commits we ported from.
```

**Steps live in `02-roadmap.md`. Findings live in `research/`. Decisions live in `decisions/`. Examples live in `modules/`. Source attributions live in `references/`.** That's the navigation contract.

## Conventions

- **Filenames:** lowercase-with-hyphens. ADRs and module specs use numeric prefixes (`ADR-0001-`, `M01-`) so they sort naturally.
- **Status badges in front matter** for stubs (`status: stub`), in-progress notes (`status: draft`), and committed material (`status: committed`).
- **Every research note cites its sources.** No untraceable claims.
- **Every decision is an ADR.** Pattern: context → decision → consequences. Once committed, you supersede an old ADR by writing a new one that references it; you don't edit history.
- **Code examples in module specs are illustrative.** They show the shape we want; the real code lives in `apps/` and `packages/`.

## Start a new phase

Copy `framework/PHASE-TEMPLATE/` to `phase-N/`, replace placeholders, link from this README's phase table, open an issue announcing the phase.
