---
adr: 0005
title: Rule format — NL condition with structured envelope
status: accepted
date: 2026-05-04
---

# ADR-0005 — Rule format

## Context

Users write rules to decide when alerts fire. The rule format choice has cascading effects: UX (zero learning curve vs. learning a syntax), per-frame cost (extra inference vs. deterministic eval), false-positive risk, and how Phase 2's multi-modal rules combine signals.

M5 research ([`../research/M5-rule-engine.md`](../research/M5-rule-engine.md)) compared three architectures: pure NL evaluation (LLM judges per frame), structured DSL, and hybrid (NL compiled to structured at create time).

## Decision

We adopt **NL condition with a structured envelope.** The rule's `condition` is free-text English, judged by a fast text-only Gemma pass per frame. The structured envelope holds temporal constraints (`activeWindow`), confidence threshold, severity, and priority — fields the engine uses without LLM involvement.

```kotlin
@Serializable
data class Rule(
    val id: RuleId,
    val condition: String,                      // NL — judged by Gemma
    val activeWindow: TimeWindow? = null,       // structured — pre-filter
    val minConfidence: Double = 0.7,            // structured — threshold
    val severity: Severity = Severity.NORMAL,   // structured — applied on match
    val priority: Int = 100,                    // structured — eval order
    val createdAt: Instant,
    val updatedAt: Instant,
)
```

The rule engine pipeline:

1. Capture frame → vision Gemma → `description: String`
2. For each rule (sorted by priority):
   - If `activeWindow` is set and current time is outside it → skip
   - Run text-only Gemma judge prompt: `"Does <description> match <condition>? JSON output: matched/confidence/reason"` constrained by GBNF grammar
   - If `matched && confidence >= minConfidence` → fire alert, short-circuit
3. No match → discard frame

Lives in `packages/core-rules/`. The judge prompt and GBNF grammar are data files in that package (so they evolve without code changes).

## Alternatives considered

- **Pure structured DSL** (`when scene contains person and time after 21:00`). Pros: deterministic, fast, no LLM eval cost. Cons: users learn a syntax, less flexible. Rejected — UX cost too high for the initial product, and the LLM eval cost is small (text-only Gemma is ~50-200ms vs ~2-5s for vision, lost in noise).
- **Pure NL evaluation including time** ("Alert if a person is visible AFTER 9 PM" — let Gemma parse the temporal constraint). Pros: simplest data model. Cons: extra LLM ambiguity surface, can't pre-filter to skip inference in inactive windows. Rejected — we move temporal logic to a structured field.
- **Hybrid (NL → structured at create time, via an LLM compiler).** Pros: best of both. Cons: implementation cost (parser + compiler + repair loop), and v1 doesn't need it. Deferred to potential v2.

## Consequences

**Easier:**
- UX: users type rules in English. Onboarding friction is near zero.
- Pre-filter cuts inference cost significantly when rules have `activeWindow`s.
- Per-rule structured fields (severity, priority, confidence threshold) are editable in a clean UI without parsing free-text.
- Phase 2 multi-modal rules can extend by adding more structured fields (e.g. `requiresConfirmation: List<RuleId>` for "camera + audio confirms") without changing the format.

**Harder:**
- Per-frame inference includes the judge step. Acceptable per M4 measurements but it's a real cost.
- Self-reported confidence from Gemma is loose. v1 documents the limitation and ships a fixed user threshold.
- Multi-rule eval is sequential (single in-memory model). N rules → up to N judge passes per frame. Mitigated by short-circuit and `activeWindow` pre-filter.

**New constraints:**
- Rules must have unique `priority` per-screen, or order-of-evaluation is undefined.
- Editing a rule's `condition` doesn't migrate historical alerts — old alerts retain their original `condition` snapshot in the `Alert` record.
- The judge prompt is a data file. Changes to it are versioned; user-facing rules don't change shape.

## Related

- Supersedes: none
- Superseded by: none
- Informed by: [`../research/M5-rule-engine.md`](../research/M5-rule-engine.md)
- Triggers: `packages/core-rules/` skeleton in roadmap Step 2; Phase 2 rule combining
