---
module: M5
title: Natural-language rule engine
status: stub
opened: 2026-05-04
time_box_ends: 2026-05-07
sources: []
related_adrs:
  - ADR-0005
---

# M5 — NL rule engine

## Why this matters

Users write rules in English: _"Alert me if anyone except Melanie enters the office after 9 PM."_ The rule engine evaluates each Gemma description against the user's rules and decides match / no-match. The format choice — pure NL, structured DSL, or LLM-judged — has cascading effects on UX, false-positive rates, and how Phase 2's multi-modal rules combine signals.

## Specific questions

1. **Pure NL evaluation** — pass description + rule to a small LLM (Gemma 4 E2B itself, or a smaller text-only model), ask "does this match?" Pros: zero learning curve. Cons: extra inference per frame, costs latency and battery.
2. **Structured DSL** — `when scene contains person and time after 21:00`. Pros: deterministic, fast. Cons: users have to learn a syntax, less flexible.
3. **Hybrid** — rules are NL but compiled to a structured form on creation. Pros: best of both. Cons: implementation complexity.
4. **Confidence handling** — Gemma's description isn't probabilistic. How do we express "alert only when very sure"?
5. **Time-of-day, day-of-week constraints** — rules need temporal context. Where does that live, in the rule string or as separate fields?
6. **Multi-rule scheduling** — if a user has 5 rules, do they all evaluate every frame, or do we short-circuit?
7. **Learning loop** — user marks an alert as false positive. Does the rule auto-adjust, or do we just collect feedback for v1.1?

## Findings

_(fill in)_

## Tried that didn't work

_(log dead ends)_

## Recommendation

_(one paragraph, informs ADR-0005)_
