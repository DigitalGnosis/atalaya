---
adr: 0011
title: APK signing key custody — Bitwarden Secrets Manager, DG-Shared project
status: accepted
date: 2026-05-04
---

# ADR-0011 — Signing key custody

## Context

Phase 1 ships an Atalaya APK. Once signed with a key, that key must NEVER change for the lifetime of the app — Android refuses to install updates signed with a different key. So the signing key is a precious asset: lose it, and we lose the ability to update the app for every existing user.

Where it lives, who can access it, and how we sign release builds — all need to be locked before Step 11 (release).

The existing Digital Gnosis credential pattern is **Bitwarden Secrets Manager** (`bws` CLI, BWS_ACCESS_TOKEN-driven, no `.env` files on disk). DG-Shared is the natural project for cross-product secrets.

## Decision

**The Atalaya APK signing key lives in Bitwarden Secrets Manager, DG-Shared project.**

- Project ID: `15ff3d7c-a0ab-499b-8b11-b3f100425d8d` (DG-Shared)
- Secret naming convention:
  - `ATALAYA_RELEASE_KEYSTORE` — the keystore file, base64-encoded
  - `ATALAYA_RELEASE_KEY_ALIAS` — the key alias inside the keystore
  - `ATALAYA_RELEASE_KEYSTORE_PASSWORD` — keystore password
  - `ATALAYA_RELEASE_KEY_PASSWORD` — key password
- Release builds invoke `bws run --project-id <DG-Shared> -- ./gradlew assembleRelease` so the secrets are injected as env vars; nothing hits disk.
- The keystore is generated once with `keytool -genkeypair`, the file is base64-encoded, and the encoded blob is stored in BWS as `ATALAYA_RELEASE_KEYSTORE`. Build pipeline decodes to a temp file, signs, deletes the temp file.
- A backup of the encoded keystore lives in a secondary BWS secret (`ATALAYA_RELEASE_KEYSTORE_BACKUP`) and a paper printout in a fireproof safe — losing this key is unrecoverable, the redundancy is mandatory.

## Alternatives considered

- **Hardware token (YubiKey + Android Key Attestation).** Pros: tamper-evident, can't be exfiltrated digitally. Cons: complicates CI builds, requires physical presence. Rejected for v1; revisit at v2.0 if we get serious about supply-chain security.
- **GitHub Actions secrets.** Pros: lives where the build runs. Cons: locked to GitHub, doesn't reuse the existing DG BWS pattern, less auditable. Rejected.
- **Local file on dg-core only.** Pros: simple. Cons: doesn't cleanly survive dg-core hardware failure, no rotation discipline. Rejected.
- **Play App Signing (Google holds the key).** Pros: Google manages the key, easy. Cons: requires Play Store distribution, third-party trust, no path to F-Droid which is our primary distribution channel. Rejected — F-Droid is the target.

## Consequences

**Easier:**
- Reuses the existing DG BWS pattern that vaultwarden, order-portal-backend, and other DG services already use.
- `bws run` injection means the key never exists on disk during a build.
- Cross-machine signing — Forge can sign from dg-core, Nigel can sign from any DG-authenticated machine, both pull the same key from BWS.

**Harder:**
- Requires DG-Shared project access to do release builds. Contributors who don't have BWS access cannot sign release APKs (only debug builds, which use the standard Android debug key).
- Loss of BWS access = loss of release ability until BWS is restored. Mitigation: paper-printout backup of the keystore in a fireproof safe.

**New constraints:**
- All release builds go through `bws run --project-id <DG-Shared> -- ./gradlew assembleRelease` (or equivalent CI path).
- Debug APKs use the default Android debug keystore — no BWS access needed for development.
- Secret rotation: if compromised, regenerate the keystore, push update to BWS, every existing user must reinstall (the app cannot self-update across signing key changes). This is a worst-case event; document the recovery procedure.

## Implementation steps (Phase 1 Step 11)

1. Generate keystore: `keytool -genkeypair -keystore atalaya-release.jks -alias atalaya -keyalg RSA -keysize 4096 -validity 36500`
2. Base64-encode: `base64 atalaya-release.jks > atalaya-release.b64`
3. Store in BWS:
   ```
   bws secret create ATALAYA_RELEASE_KEYSTORE "$(cat atalaya-release.b64)" --note "Atalaya APK release signing key (base64-encoded)" 15ff3d7c-a0ab-499b-8b11-b3f100425d8d
   bws secret create ATALAYA_RELEASE_KEY_ALIAS atalaya 15ff3d7c-a0ab-499b-8b11-b3f100425d8d
   bws secret create ATALAYA_RELEASE_KEYSTORE_PASSWORD <password> 15ff3d7c-a0ab-499b-8b11-b3f100425d8d
   bws secret create ATALAYA_RELEASE_KEY_PASSWORD <password> 15ff3d7c-a0ab-499b-8b11-b3f100425d8d
   ```
4. Print + safe-store the keystore for the worst-case backup.
5. Wire `apps/node/build.gradle.kts` release signingConfig to read from env vars.
6. Document the release procedure in `BUILDING.md`.
7. Delete every local copy of the keystore — only BWS holds the live copy + the paper backup.

## Related

- Supersedes: none
- Superseded by: none
- Informed by: existing DG BWS pattern (vaultwarden, order-portal-backend); Forge's standing orders on credential discipline
- Triggers: Phase 1 Step 11 release procedure documentation
