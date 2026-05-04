---
adr: 0013
title: First dogfood device — Samsung Galaxy S21 (pending physical confirmation)
status: superseded by ADR-0014
date: 2026-05-04
---

> ⚠️ **Superseded by [ADR-0014](ADR-0014-dogfood-device-correction.md).** This ADR contained an unverified claim that the Galaxy S21's Snapdragon 888 supports llama.rn's Hexagon HTP NPU path. **It does not.** Snapdragon 888 has Hexagon V68; llama.rn requires V69+. The S21 is still a valid dogfood device but for different reasons — ADR-0014 has the corrected reasoning.

# ADR-0013 — First dogfood device

## Context

Atalaya v1's first dogfood test is the founder running it on an old phone from his drawer. The choice of device matters because it sets the implicit minimum spec we exercise against. A phone that runs Atalaya well becomes proof; a phone that struggles informs hardware-floor decisions.

The founder named **Galaxy S21** as the working choice (pending physical confirmation that it's actually in the drawer).

## Decision

**Working device: Samsung Galaxy S21 (2021).** Spec ID for the device's role as the dogfood reference.

If physical confirmation reveals a different model in the drawer, this ADR is superseded by a new one naming the actual device. The reasoning below stays in place — it's why the Galaxy S21 is a *good* choice, not just a chosen one.

## Why the Galaxy S21 is a strong dogfood pick

| Spec | Value | Implication |
|------|-------|-------------|
| Chipset | Snapdragon 888 (US) / Exynos 2100 (intl) | Snapdragon 888 has Hexagon HTP — our preferred NPU path per ADR-0003 |
| RAM | 8 GB | Plenty for E2B (~1.5 GB) + foreground service overhead |
| Android | Shipped 11, currently 14+ | Far above Min SDK 26 per ADR-0007 |
| Battery | 4000 mAh | Decent for always-on tests |
| Camera | 12MP wide + 64MP telephoto + 12MP ultrawide | More than enough for security framing |
| Release | Jan 2021 | 5 years old as of 2026 — represents the "old phone" target |

**Bonus reason: Samsung is notoriously aggressive at killing background services.** If Atalaya survives Samsung's OEM-kill (autostart restrictions, battery optimizer, "lock in recents" requirement), it'll survive most other Android OEMs. The S21 is a stress test, not a soft target. Forces our ADR-0007 OEM-kill mitigation work to land for real.

## Alternatives considered

- **Pixel 6 / Pixel 9.** Pros: clean Android, NPU on Tensor G1+, friendly to background services. Cons: Pixel doesn't expose the Samsung/Xiaomi OEM-kill quirks we need to validate against.
- **OnePlus 9 / 10.** Pros: Snapdragon 888 + 8GB. Cons: similar to S21 but smaller user base.
- **Wait for Pixel 6 specifically.** Pros: optimal for inference. Cons: doesn't represent the "old phone in the drawer" target audience.

S21 wins because it represents the target audience AND it's the founder's actual hardware AND it stress-tests the OEM-kill problem.

## Consequences

**Easier:**
- We have a real reference device for "does it work" tests.
- Samsung OEM-kill is exercised early; can't ship without solving it.
- Snapdragon 888 NPU lets us validate the Hexagon HTP path from llama.rn.

**Harder:**
- Samsung OEM-kill mitigation is mandatory for v1 — can't be deferred. ADR-0007's onboarding screen must work on Samsung specifically.
- 4000 mAh is on the smaller side; battery duty-cycle measurements will be conservative.

**New constraints:**
- Phase 1 Step 10 hardening must include Samsung-specific OEM-kill onboarding screen tested on the S21.
- The supported-device documentation includes the S21 explicitly as a known-working reference.
- If physical confirmation reveals a different device, supersede this ADR with the actual model.

## Open

- ⏳ **Physical confirmation:** founder to verify the actual phone in the drawer is a Galaxy S21 (and not a different Samsung model or a different brand). Trivial check, fits in a 30-second moment.

## Related

- Supersedes: none
- Superseded by: none (until physical confirmation reveals otherwise)
- Informed by: ADR-0003 (inference runtime — Hexagon HTP path), ADR-0007 (Min SDK + OEM-kill mitigation)
- Triggers: Samsung-specific onboarding screen testing in Phase 1 Step 10
