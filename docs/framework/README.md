# Phase Documentation Framework

This directory holds the conventions and templates for Atalaya's per-phase documentation. Every phase folder follows the same shape so navigation is predictable.

## Why this exists

Atalaya has six phases. Each phase has research, decisions, module specs, and references. Without a shared shape, every phase reinvents its own structure and contributors waste time hunting for things. This framework fixes the shape.

## How to start a new phase

1. Copy `PHASE-TEMPLATE/` to `docs/phase-N/` where N is the next phase number.
2. Open the new `phase-N/README.md` and replace placeholders.
3. Update `docs/README.md` so the phase table links to your new folder.
4. Open a tracking issue announcing the phase.

```bash
cp -r docs/framework/PHASE-TEMPLATE docs/phase-2
```

## Folder shape

```
phase-N/
├── README.md          # Index with quick-nav table, modules list, ADR queue
├── 01-goal.md         # Success criteria, quality gates, what we measure
├── 02-roadmap.md      # Vertical slices in order, daily rhythm, definition of done
├── research/          # Time-boxed module-level findings, source-cited
├── decisions/         # ADRs — immutable, dated, linked
├── modules/           # Module specs with code examples
└── references/        # Code we ported from with attribution
```

## Conventions

### Files

- Lowercase-with-hyphens for filenames
- Numeric prefixes (`01-`, `02-`, `M01-`, `ADR-0001-`) for sort order
- Front matter (YAML) at the top of every doc — captures status, dates, related artifacts

### Status badges

- 🔵 Open / In progress
- ✅ Complete / Shipped / Accepted
- ⚪ Pending / Future
- 🟡 Blocked / Stalled
- ❌ Cancelled / Superseded

### Cross-linking

- ADRs cite the research notes that informed them
- Module specs cite the ADRs they implement
- References cite the files in our repo that reflect their patterns
- Roadmap steps cite the modules they touch

This is the audit trail. Anyone can follow a piece of code back to the decision, the research, and the source we ported from.

## What a "good" phase looks like

- Every research stub has Findings, Tried-that-didn't-work, and a Recommendation
- Every ADR exists in `decisions/` with the standard template
- Every committed module has a spec in `modules/`
- Every external project we used appears in `references/` with license and commit hash
- The roadmap matches what was actually built (update it as you go)

When a phase closes:
- Update the phase status in `docs/README.md`
- Mark the phase folder's README with `Status: ✅ Closed` and the closure date
- The folder stays — never delete it. Future phases reference it.
