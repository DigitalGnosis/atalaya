---
adr: 0010
title: Mesh strategy — Headscale + WireGuard-Android, branded as Atalaya Mesh
status: accepted
date: 2026-05-04
phase: implementation deferred to Phase 2
---

# ADR-0010 — Mesh strategy

## Context

Atalaya Phase 2 introduces the hub. When a hub exists, nodes need to talk to it across the network — and if the user is away from home, the control phone needs to reach the hub too. We need a mesh VPN. The constraint from the founder is firm: **no Tailscale Inc dependency, no third-party SaaS, no central service we don't control.** Atalaya users get a mesh that's all theirs.

Three architectures are viable:

1. **Build our own mesh from scratch.** Crypto, NAT traversal, discovery, identity, routing, persistence, cross-platform clients. 6-12 months of expert work; NAT traversal alone is brutal.
2. **WireGuard direct, no coordinator.** Bundle WireGuard, generate keys, ship configs. Works on the user's LAN. Cross-network requires the user to manually peer or run their own relay — too much friction for a normie audience.
3. **Headscale + WireGuard-Android.** Headscale is the open-source Tailscale control plane (BSD-3, Go binary). It speaks Tailscale's protocol but is fully self-hostable. The hub embeds Headscale; node and control apps embed WireGuard-Android directly and drive it with Headscale-managed keys. Result: a fully self-hosted mesh, no Tailscale Inc involvement, proven crypto and NAT traversal under the hood. ~2 weeks of integration work.

Phase 1 has zero network — local notifications only, per ADR-0006. So this decision doesn't block Phase 1 work; it locks the architecture so Phase 2's design doesn't drift.

## Decision

We adopt **Headscale + WireGuard-Android, branded as "Atalaya Mesh."** Implementation lands in Phase 2.

- **Hub (Phase 2)** — Atalaya hub Docker image embeds Headscale alongside the hub services. One Docker compose, one mesh, one self-hosted everything.
- **Node and control apps (Phase 2+)** — embed WireGuard-Android directly (LGPL — kept as a dynamic dependency or repackaged following its license rules). Driven by Headscale-managed keys, configured by the hub.
- **Onboarding** — pairing a new node generates a Headscale auth key on the hub, the user scans a QR code on the node, the node joins the mesh. Users never see the words "Tailscale" or "Headscale" — UI is "Atalaya Mesh."
- **Cross-network reachability** — Headscale's DERP-equivalent (relay through the hub when direct peering fails) is configured with the hub itself acting as the relay. No third-party DERP servers.

## Alternatives considered

- **Build our own mesh.** Rejected — 6-12 months of expert work to reinvent solved problems. We would not ship Atalaya v1 in 2026.
- **WireGuard direct, no coordinator.** Rejected for normie target — manual peering and missing NAT traversal disqualifies it. Possible LAN-only fallback for power users who turn the mesh off.
- **Tailscale Inc client + Tailscale-the-company.** Rejected per the founder constraint — Atalaya users should not need to create a Tailscale account.
- **Yggdrasil mesh.** Rejected — IPv6 overlay, more academic, less battle-tested for our use case, smaller ecosystem.
- **Nebula (Slack's mesh).** Rejected — a viable second choice but ecosystem is smaller than WireGuard's, and embedding it in Android is less proven.

## Consequences

**Easier:**
- Cross-network reachability solved (Headscale's relay logic is proven).
- NAT traversal solved (WireGuard does this well).
- Crypto solved (WireGuard's crypto is audited and current).
- Self-hosting story is "docker compose up" — Headscale is the entire control plane.
- Future Phase 4 control app on iOS can use the existing Tailscale iOS client pointed at the user's Headscale, even if our app embeds WireGuard-Android directly.

**Harder:**
- Two new dependencies — Headscale (Go) embedded in the hub container, WireGuard-Android (LGPL/MIT) embedded in node and control apps.
- LGPL compliance for WireGuard-Android — must be replaceable by users (dynamic linking or rebuild path documented). Doesn't conflict with our AGPL-3.0; just a discipline.
- Hub Docker image gets bigger (Headscale binary is ~30 MB).
- One more thing to update — Headscale releases need to be tracked alongside our own versions.

**New constraints:**
- The hub is the SINGLE point of authority for the mesh. If the hub is destroyed, all nodes need to re-pair against a new hub. Mitigation: backups of Headscale's database; eventual support for hub replicas in Phase 5+.
- Headscale's DB lives alongside the hub's other state — backups must include it.
- Mesh transport is opt-in for users who want LAN-only. UI surfaces "Use mesh" toggle.

## Phase 2 implementation outline (preview)

1. New package `core-mesh` — Kotlin abstraction over WireGuard-Android. Interface for "join mesh by auth key", "leave mesh", "current peers", "is connected".
2. Hub Docker image extension — Headscale alongside the existing hub services. Reverse proxy routes Headscale's API on a subpath of the hub's URL.
3. Node arm flow — pairing screen scans hub QR code, calls `core-mesh.joinMesh(...)`.
4. Alert transport `MeshTransport` — pluggable `AlertTransport` impl that ships alerts over the mesh from hub to control.
5. iOS notes — Phase 4b iOS control app uses Tailscale iOS client pointed at Headscale (same protocol). No WireGuard-iOS embedding needed for v1.

Detailed Phase 2 ADRs land when Phase 2 opens; this ADR locks the strategic direction.

## Related

- Supersedes: none (no prior mesh decision)
- Superseded by: none
- Informed by: founder constraint (no Tailscale Inc dependency); existing DG infrastructure patterns (cipherware-sdk-android federated package proof)
- Triggers: Phase 2 ADRs for hub Docker structure, `core-mesh` package design, pairing UX

## License notes

- Headscale: BSD-3-Clause — compatible with AGPL-3.0
- WireGuard-Android: LGPL-3 + MIT for some pieces — compatible with AGPL-3.0; LGPL discipline preserved (replaceable by users)
- WireGuard userspace Go: MIT — compatible
