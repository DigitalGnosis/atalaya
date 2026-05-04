# Contributing to Atalaya

Welcome. A few things to know before you cut a PR.

---

## Operating Principles (Non-Negotiable)

1. **No scratch builds.** Every phase opens with research. Find the app that already solves the piece, study it, port the pattern. Reinventing is rude to the users whose problem we're trying to solve.
2. **Modularity is the prime directive.** Every role, sensor, transport, and storage backend is a plugin. Swap any piece without touching others.
3. **Bitwarden's BaseViewModel is the view-layer convention.** Every Atalaya app (node, hub admin, control) uses Bitwarden's MVVM + state pattern. Don't roll your own state container.
4. **Time-boxed research.** Each phase's research opens with a hard cap (default 3 working days). After the cap: stop researching, start porting.
5. **Public from week one.** Repo open. Issues open. Decisions discussed in the open.

## What "port" means here

Porting is not stealing. It is studying an existing solution, citing it, adapting it to our license and our shape, and crediting the source. When you port:

- Cite the source repo and commit hash in the file header
- Match licenses (AGPL-3 is compatible with GPL-3, MIT, Apache, BSD)
- Adapt naming and abstractions to Atalaya's module boundaries
- Open an issue describing what you ported and why

## What we will not accept

- Code copied without attribution
- New global state containers (use the existing BaseViewModel pattern)
- Modules that import other modules' internals (cross only through public interfaces)
- Vendor-locked dependencies (no Google-only, no AWS-only — we self-host)
- Telemetry without explicit opt-in

## How to propose a change

1. **Open an issue first** for anything bigger than a typo. State the problem, the proposed approach, and any references you plan to port from.
2. **Wait for a thumbs-up** from a maintainer before starting bigger work.
3. **Cut small PRs.** One concept per PR. Easier to review, easier to merge, easier to revert.
4. **Tests pass, lints clean.** CI must be green before review.
5. **Sign off on commits.** `git commit -s` adds a Developer Certificate of Origin line; we require it.

## Architecture

Read [`ARCHITECTURE.md`](ARCHITECTURE.md) before proposing structural changes. The three planes (node / hub / control) and the role-pluggable model are load-bearing. Changes that cross those boundaries need a discussion first.

## License of contributions

Contributions are accepted under [AGPL-3.0-only](LICENSE). By submitting a PR, you certify the DCO sign-off and agree your contribution carries this license.

## Conduct

Don't be a jerk. Disagree with evidence and humility. Help newcomers. Credit collaborators. Take feedback like a colleague.
