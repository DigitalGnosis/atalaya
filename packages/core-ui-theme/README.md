# `packages/core-ui-theme`

Atalaya's Material 3 theme. Colors, typography, dimensions, dark/light schemes. Every Atalaya app uses this.

**Phase:** 1 (current)
**Stack:** Kotlin + Compose Material 3
**Status:** 🔵 skeleton — placeholder palette, awaiting DG brand spec

## ⚠️ DG brand is not yet defined

There is **no canonical DG brand spec** — no agreed color palette, typography, voice, or logo across Digital Gnosis products. This module ships with Material 3 defaults (Material You dynamic where available, neutral fallback otherwise) until the founder + design define the DG brand.

When the brand lands, this module becomes the single point of update; every consuming Atalaya app inherits. Likely first candidate to extract into a DG-shared `dg-ui-theme` published repo so Connect, Route 33, Dispatch, etc. all inherit one DG brand simultaneously. See [ARCHITECTURE.md → DG brand language](../../ARCHITECTURE.md#dg-brand-language--tbd) for context.

Tracked as an open question in [`docs/phase-1/README.md`](../../docs/phase-1/README.md).

## What it owns

- Light + dark color schemes (Material 3 dynamic + neutral fallback for Phase 1; brand-locked variants when DG brand lands)
- Typography (Material 3 type scale; brand customizations TBD)
- Dimensions (spacing scale, corner radii, elevation)
- A `AtalayaTheme` Composable that wraps `MaterialTheme` with our defaults
- Brand tokens — currently neutral placeholders; replaced when DG brand lands

## What it does NOT own

- Reusable composable components (hint cards, status badges, loading states) — those live in `core-ui-components`
- Material 3 itself — we depend on the official lib, not reimplement
- Per-screen UI (lives in app modules)
- Drawable assets (icons, illustrations) — those live per-app for now; Phase 5+ may consolidate

## Dependencies

| On | Why |
|----|-----|
| `androidx.compose.material3` | Material 3 baseline |
| `androidx.compose.ui` | Compose runtime |

## Consumers

| Module | Use |
|--------|-----|
| [`apps/node`](../../apps/node/) | `AtalayaTheme { ... }` wraps every screen |
| `apps/control` (Phase 4) | Same theme, same brand |
| [`packages/core-ui-components`](../core-ui-components/) | Components reference theme tokens (colors, dimensions) |

## Source layout (planned)

```
packages/core-ui-theme/
├── README.md
├── build.gradle.kts
└── src/main/kotlin/dev/digitalgnosis/atalaya/ui/theme/
    ├── AtalayaTheme.kt
    ├── Colors.kt             # light + dark schemes
    ├── Typography.kt
    ├── Dimensions.kt         # spacing, radii, elevation
    └── BrandTokens.kt        # primary, accent, surface tints
```
