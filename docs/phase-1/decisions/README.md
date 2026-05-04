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
| 0002 | [View-layer convention (BaseViewModel pattern)](ADR-0002-baseviewmodel-pattern.md) | accepted | M1 research |
| 0003 | [Inference runtime selection](ADR-0003-inference-runtime.md) | accepted | M4 research |
| 0004 | Default frame interval | deferred | Needs real device benchmarks |
| 0005 | [Rule format](ADR-0005-rule-format.md) | accepted | M5 research |
| 0006 | [Push transport](ADR-0006-push-transport.md) | accepted | M6 research |
| 0007 | [Min/target SDK + foreground service strategy](ADR-0007-min-sdk-and-service.md) | accepted | M2 + M4 research |
| 0008 | [DI framework — Hilt](ADR-0008-di-hilt.md) | accepted | M1 research |
| 0009 | [Federated package strategy](ADR-0009-federated-package-strategy.md) | accepted | DG ecosystem context |
| 0010 | [Mesh strategy — Headscale + WireGuard-Android](ADR-0010-mesh-strategy.md) | accepted (Phase 2 implementation) | Founder constraint: no Tailscale Inc dependency |
| 0011 | [APK signing key custody — BWS DG-Shared](ADR-0011-signing-key-custody.md) | accepted | Pre-release lock-in |
| 0012 | [Crash reporting — self-hosted Sentry on dg-core, opt-in](ADR-0012-crash-reporting.md) | accepted | Pre-Step-10 lock-in |
| 0013 | [First dogfood device — Galaxy S21](ADR-0013-dogfood-device.md) | ⚠️ superseded by ADR-0014 | (Hexagon HTP claim wrong) |
| 0014 | [Dogfood device correction — S21 exercises CPU/OpenCL not NPU](ADR-0014-dogfood-device-correction.md) | accepted | Verification of llama.rn HTP requirements |
