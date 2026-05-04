# Atalaya

> **Atalaya** _(noun, Spanish/Arabic origin)_: a watchtower at the edge of the territory.

**Your old phones. Your security system. Your code.**
AI-native, multi-modal, self-hosted, open source. SimpliSafe meets Supabase.

---

## What it is

Atalaya turns junked Android phones into a complete, self-hosted, AI-native security system.

Old phones become camera nodes, audio nodes, contact sensors, motion sensors, presence detectors, keypads, and sirens — orchestrated by a hub that runs anywhere (your spare Pi, your Linux box, our cloud, you pick).

The vision layer runs **Gemma 4** on-device. Sound classification, scene description, voice commands — all local, all private, no cloud upload of anything you don't choose to send.

Multi-modal AI confirmation kills false positives. Natural-language rules: _"Alert me if anyone except family enters the garage after 9 PM."_ Atalaya understands what it sees.

## What it isn't (yet)

A monitored alarm replacement. Atalaya is DIY infrastructure for people who want to own their security stack. We do not — today — partner with a 24/7 monitoring service, and we don't pretend to.

## Why this exists

| Existing option | Why it falls short |
|----------------|--------------------|
| SimpliSafe / Ring | Closed, cloud-only, monthly fee, your data is theirs |
| Frigate / Scrypted / DeepCamera | Server-based, IP-camera-first, not phones |
| Alfred / Manything / AtHome | Cloud upload, AI is bolted-on premium, motion-only |
| Haven (Snowden + Guardian Project) | Brilliant ancestor, dormant since 2021, no AI |
| Off Grid / Google AI Edge Gallery | Phones can run Gemma 4 today, but they're chat apps not surveillance |

The intersection — **phone-as-AI-camera-node forming a complete security system** — was empty. Atalaya fills it.

## How it compares

**Like Supabase:** open source, self-hostable, with an optional managed cloud. Same code self-hosted or hosted.

**Like SimpliSafe:** a real, complete security system. Cameras, sensors, keypad, siren, alerts, history.

**Unlike either:** your data, your hardware, your rules. Old phones, not new sensors. Local AI, not cloud APIs.

---

## Status

🚧 **Phase 0** — foundations. Repo bootstrapping, license, architecture, conventions.

See [`ARCHITECTURE.md`](ARCHITECTURE.md) for the full design and [`docs/PRD-PHASES.md`](docs/PRD-PHASES.md) for the per-phase plan.

## License

[AGPL-3.0-only](LICENSE). Hosted services on top of Atalaya must publish their source. Built by [Digital Gnosis](https://github.com/DigitalGnosis).

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md). Operating principles: no scratch builds, port best-in-class patterns, modularity is the prime directive.
