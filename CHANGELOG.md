## [1.0.6] - 2026-01-02
### Fixed
- Removed deprecated API usage in completion auto-popup implementation.

## [1.0.5] - 2026-01-02
### Added
- Comprehensive autocomplete feature:
  - Identifier completion: Suggests defined objects and connections from the current file
  - Node property completion: Suggests `shape`, `icon`, `style`, and `label` properties when inside node blocks, excluding already-defined properties
  - Shape value completion: Suggests all 18 available D2 shapes (rectangle, circle, diamond, etc.) after `shape:` property
  - Dynamic refresh: Autocomplete list updates as you type, picking up newly added identifiers
  - Context-aware: Excludes the current node name when completing inside that node's block

## [1.0.4] - 2026-01-01
### Fixed
- Improved syntax highlighting: numbers with units (e.g., `283.56USD`) are no longer incorrectly highlighted as numeric literals.

## [1.0.3] - 2025-12-31
### Added
- Live SVG preview support for rendering D2 diagrams with compositions.
- Preview mode toggle (PNG or SVG/HTML) to the preview toolbar.
- Export now matches the active preview mode (.png or .svg).
- Configurable auto-refresh debounce delay in D2 settings.
- `--animate-interval=1000` support for multi-step diagrams (layers/scenarios/steps).

### Changed
- Changing D2 settings now automatically re-renders the preview.