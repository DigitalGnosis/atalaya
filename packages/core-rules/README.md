# `packages/core-rules`

The natural-language rule engine. Takes an `Observation` and a `List<Rule>`, decides match / no-match.

**Phase:** 1 (current)
**Stack:** Kotlin (depends on `core-ml` for the LLM judge)
**Status:** 🔵 skeleton — ADR-0005 committed, no code yet

## What it owns

- `RuleEngine` — the evaluator
- The judge prompt (data file)
- The GBNF grammar that constrains the judge's JSON output (data file)
- Active-window pre-filter (skip eval when current time is outside the rule's window)
- Priority-based short-circuit evaluation
- Confidence threshold gating
- Match → `Alert` construction

## What it does NOT own

- Rule storage (per-app concern)
- Rule editing UI (per-app concern)
- Inference itself (delegated to `core-ml`)
- Alert transport (delegated to `core-protocol`'s `AlertTransport` impls)

## Dependencies

| On | Why |
|----|-----|
| [`packages/core-protocol`](../core-protocol/) | `Observation`, `Rule`, `Alert` types |
| [`packages/core-ml`](../core-ml/) | The LLM judge that decides match |
| `kotlinx.coroutines` | Async eval pipeline |
| `kotlinx.datetime` | TimeWindow comparisons |

## Specs

- ADR (rule format): [`../../docs/phase-1/decisions/ADR-0005-rule-format.md`](../../docs/phase-1/decisions/ADR-0005-rule-format.md)
- Research note: [`../../docs/phase-1/research/M5-rule-engine.md`](../../docs/phase-1/research/M5-rule-engine.md)

## Consumers

| Module | Use |
|--------|-----|
| [`apps/node`](../../apps/node/) | Calls `ruleEngine.evaluate(observation, activeRules)` after every Gemma description |
| `apps/hub` (Phase 2) | Hub-side multi-modal rule combining (extends the same engine) |

## Source layout (planned)

```
packages/core-rules/
├── README.md
├── build.gradle.kts          # Phase 1 Step 2
└── src/main/
    ├── kotlin/dev/digitalgnosis/atalaya/rules/
    │   ├── RuleEngine.kt
    │   ├── JudgeOutput.kt
    │   └── ActiveWindow.kt
    └── resources/
        ├── prompts/judge.txt          # the LLM judge prompt template
        └── grammars/judge-output.gbnf # GBNF constraining JSON output
```
