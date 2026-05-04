# `packages/core-ui-theme`

Atalaya's Material 3 theme. Colors, typography, dimensions, dark/light schemes. Every Atalaya app uses this.

**Phase:** 1 (current)
**Stack:** Kotlin + Compose Material 3
**Status:** 🔵 skeleton

## What it owns

- Light + dark color schemes (Material 3 dynamic + brand-locked variants)
- Typography (Material 3 type scale + brand customizations)
- Dimensions (spacing scale, corner radii, elevation)
- A `AtalayaTheme` Composable that wraps `MaterialTheme` with our defaults
- Brand assets that Compose consumes — primary color, accent, surface tints

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
├── build.gradle.kts          # Phase 1 Step 2
└── src/main/kotlin/dev/digitalgnosis/atalaya/ui/theme/
    ├── AtalayaTheme.kt
    ├── Colors.kt             # light + dark schemes
    ├── Typography.kt
    ├── Dimensions.kt         # spacing, radii, elevation
    └── BrandTokens.kt        # primary, accent, surface tints
```
