# Phase N — Roadmap

The ordered build plan. Each step links to its research note, the ADR(s) it produces, and the module(s) it lands.

## The arc

```
Research (3 days/module, parallel)
         ↓
Decisions (ADRs commit the research)
         ↓
Module skeletons
         ↓
Vertical slices (each ships an end-to-end demo)
         ↓
Hardening
         ↓
Release
```

## Step-by-step

### Step 0 — Phase open
_(this document is part of step 0)_

### Step 1 — Research sprint
| Module | Research stub | Output (ADR) |
|--------|---------------|--------------|
| M1 | [`research/M1-...md`](research/M1-...md) | ADR-NNNN |

### Step 2 — Module skeletons
_(what's added)_

### Step 3 — Vertical slice 1
_(what runs end-to-end)_

### Step N — Hardening
_(what gets polished)_

### Step N+1 — Release
_(versioning, changelog, distribution)_

## Daily rhythm

- Morning: pick the next slice, write a "what I'm doing today" comment in the slice's issue.
- Afternoon: open a draft PR.
- End of day: status update.

## Definition of done (per slice)

- Vertical: a debug deliverable demonstrates the new behavior end-to-end.
- Linted, tested, documented, reviewed, merged.
