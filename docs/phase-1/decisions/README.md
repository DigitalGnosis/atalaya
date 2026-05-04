# Phase 1 — Decisions (ADRs)

Architecture Decision Records. Each ADR captures a single committed decision with its context, the alternatives considered, and the consequences. ADRs are immutable once committed — to change a decision, write a new ADR that supersedes the old one.

## Format

We use the [Michael Nygard ADR template](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions). See [`TEMPLATE.md`](TEMPLATE.md).

## Status legend

- `proposed` — drafted, awaiting review
- `accepted` — committed, code follows it
- `superseded by ADR-NNNN` — older decision replaced
- `deprecated` — decision no longer applies but no replacement; explain why

## Index

| # | Title | Status | Triggered by |
|---|-------|--------|--------------|
| 0001 | [Monorepo layout](ADR-0001-monorepo-layout.md) | accepted | Phase 0 + opening of Phase 1 |
| 0002 | View-layer convention (BaseViewModel pattern) | _pending M1_ | M1 research |
| 0003 | Inference runtime selection | _pending M4_ | M4 research |
| 0004 | Default frame interval | _pending M4_ | M4 benchmarks |
| 0005 | Rule format | _pending M5_ | M5 research |
| 0006 | Push transport | _pending M6_ | M6 research |
| 0007 | Min/target SDK | _pending M2 + M4_ | M2 background-service + M4 inference compatibility |
| 0008 | DI framework | _pending M1_ | M1 research likely lands on Hilt |
