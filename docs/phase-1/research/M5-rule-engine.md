---
module: M5
title: Natural-language rule engine
status: findings
opened: 2026-05-04
time_box_ends: 2026-05-04
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

This is design research, not external-code research — the field is empty enough that there is no off-the-shelf NL surveillance rule engine to port. We're synthesizing from three adjacent traditions: (1) classical home-automation rule engines (Home Assistant automations, Node-RED) which are structured DSLs, (2) LLM-as-judge patterns from the eval-tooling world (Anthropic's `judge` patterns, OpenAI's `evals`, FactScore, TruLens), and (3) the prompt-as-code patterns from production agents like Cursor, Cline, and the Claude Code system prompts.

### 1 — Three architectures, with cost estimates

| Approach | Per-frame cost | UX | Implementation cost | False-positive risk |
|----------|---------------|----|--------------------|---------------------|
| Pure NL (Gemma judges) | +1 inference (text-only, fast) | Zero learning curve | Low (one prompt template) | Medium — Gemma has to interpret consistently |
| Structured DSL | ~free | Users learn syntax | Medium (parser, validator, UI) | Low — deterministic |
| Hybrid (NL → structured at create time) | ~free at runtime, +1 inference at rule-create time | Best UX | High (parser + LLM compiler + repair loop) | Low at runtime, depends on compile quality |

**For Phase 1 with one role (camera) and one judgment ("does the description match this rule"), the per-frame cost of Pure NL is acceptable.** A text-only Gemma 4 prompt is much faster than vision inference (~50-200ms vs ~2-5s on Pixel 6) — we're already paying for vision; the rule eval is in the noise.

### 2 — The two-prompt pipeline

```
Frame → [Vision Gemma: "describe this scene"] → description
                                                      ↓
                                  [Text Gemma: "does <description> match <rule>? Y/N + why"]
                                                      ↓
                                              MatchDecision { matched: Bool, confidence: 0..1, reason: String }
```

Two passes through the same model. The vision pass is the expensive one. The text-judge pass is fast because: no vision, output is constrained to a small JSON shape, can be capped at ~50 output tokens.

Constrained output via grammar (llama.cpp supports GBNF) gives us machine-parseable judgments without prompt-engineering against hallucinated JSON.

### 3 — Confidence handling

Gemma's output isn't probabilistic in the open-source runtimes (no native logprob streaming exposed cleanly). Two paths:

- **Self-reported confidence in the judge prompt.** Ask the model to output `confidence: 0..1`. Studies show LLMs' self-reported confidence correlates loosely with correctness — usable as a coarse signal, not a precise probability.
- **Sampled judgments.** Run the judge prompt N times at temperature > 0, count agreement. Expensive (N× cost), but more robust. Reserve for v2.

For v1: ship self-reported confidence + a fixed user threshold ("alert at confidence >= 0.7"). Document the limitation. Sampled judgments land in v1.1 if false-positive rates demand it.

### 4 — Temporal constraints (time of day, day of week)

Two viable patterns:

- **In-rule.** "Alert if a person is visible AFTER 9 PM" — let the model parse temporal constraints. Tested informally in similar models — they handle this well *if* we inject the current time/day into the prompt context.
- **Separate fields.** Rule data structure has a free-text `condition` plus structured `activeWindow: { startHour, endHour, daysOfWeek }`. Engine pre-filters on the active window before invoking the LLM.

Separate fields wins for v1 because: (a) it eliminates a category of LLM ambiguity, (b) it cuts inference cost — no point asking the model whether to alert if we're outside the active window, (c) the UI can render "active 9 PM – 6 AM, Mon–Fri" cleanly.

### 5 — Multi-rule scheduling

User has N rules. Two questions: (a) parallel or sequential evaluation? (b) short-circuit on first match?

For v1, evaluate sequentially, short-circuit on first match. Reasons: (a) Gemma inference can't truly parallelize on a single phone (one model in memory, one inference at a time), (b) once we've decided to alert, additional rule evaluation is wasted compute. Order-of-evaluation becomes the priority order — document this clearly.

In Phase 2 with the hub, multi-rule evaluation can fan out across hub-side inference if available.

### 6 — Learning loop

Out of scope for v1. v1 ships with: feedback button on every alert (✓ "real" / ✗ "false alarm"). Feedback persists locally as `RuleFeedback { ruleId, observationRef, userVerdict }`. v1.1 uses that data to: (a) suggest threshold adjustments to the user, (b) suggest rule edits when a rule has many false positives.

True learning (model fine-tuning, embedding-based similarity matching) is Phase 5+ and depends on a real corpus of feedback.

### 7 — Rule data structure (proposed)

```kotlin
@Serializable
data class Rule(
    val id: RuleId,
    /** What the user typed. Source of truth for editing. */
    val condition: String,
    /** Temporal window when this rule is active. Null = always active. */
    val activeWindow: TimeWindow? = null,
    /** Minimum self-reported confidence for a match to fire. 0.0..1.0. */
    val minConfidence: Double = 0.7,
    /** User-set severity for matches of this rule. */
    val severity: Severity = Severity.NORMAL,
    /** Order matters — lower number = higher priority. */
    val priority: Int = 100,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Serializable
data class TimeWindow(
    val startHour: Int,    // 0..23
    val endHour: Int,      // 0..23, may wrap (e.g. 21..6)
    val daysOfWeek: Set<DayOfWeek> = DayOfWeek.values().toSet(),
)
```

### 8 — The judge prompt (draft, to be tuned with M4 measurements)

```
You are evaluating whether a security camera observation matches a user's alert rule.

Observation: {description}
Rule: {rule.condition}
Current time: {now}

Output JSON only:
{
  "matched": true | false,
  "confidence": 0.0..1.0,
  "reason": "<one short sentence>"
}
```

GBNF grammar to constrain output is straightforward — we land it in `core-rules`.

## Tried that didn't work

n/a — design research, no implementations attempted in this round.

## Recommendation

For Phase 1, adopt **pure NL evaluation with a structured wrapper**: the rule's free-text `condition` is judged by Gemma with a fixed JSON-output prompt, while temporal constraints (`activeWindow`), confidence threshold, severity, and priority live as structured fields. The rule engine is a `core-rules` package that takes an `Observation` and a `List<Rule>`, pre-filters by active window, then iterates rules in priority order, short-circuiting on the first confident match. ADR-0005 commits this; the LLM judge prompt and GBNF grammar live in `core-rules` as data files so they evolve without code changes. Defer learning loops, sampled judgments, and multi-modal rule combining to Phase 2+.
