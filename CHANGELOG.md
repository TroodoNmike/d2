## [1.0.14] - 2026-06-15
### Fixed
- Markdown blocks (`|md ... |`) now render in the SVG preview again. D2 emits Markdown as `<foreignObject>`, which the preview's SVG sanitizer previously stripped, leaving such blocks blank.

### Security
- Authoritative SVG sanitization now runs in-browser via a bundled DOMPurify (3.4.10). The SVG is base64-encoded and sanitized before it is inserted into the DOM, so embedded scripts never execute. `<foreignObject>` is re-allowed with scripts, `on*` handlers, `javascript:` URLs, and `<iframe>` still removed.

## [1.0.13] - 2026-04-01
### Added
- Plugin verification configuration for local compatibility checks (`./gradlew verifyPlugin`)

### Security
- SVG sanitization and process environment hardening for preview rendering

### Removed
- Markdown preview D2 rendering (used experimental Markdown plugin APIs that caused compatibility warnings)

## [1.0.12] - 2026-02-24
### Added
- WSL2 support: run D2 CLI through Windows Subsystem for Linux with a new "Use WSL" toggle and optional distribution selector in settings

## [1.0.11] - 2026-02-23
### Added
- Style property autocomplete: suggests 19 D2 style properties (`opacity`, `fill`, `stroke`, `font-size`, `bold`, etc.) when typing inside `style { }` blocks, with automatic exclusion of already-defined properties

## [1.0.10] - 2026-02-20
### Added
- Theme-aware scrollbar styling in SVG preview that adapts to light and dark themes

## [1.0.9] - 2026-02-20
### Added
- Configurable preview background with five options: IDE Theme (matches your editor), Transparent (checkerboard), Light, Dark, and Custom (with color picker)

## [1.0.8] - 2026-02-20
### Fixed
- Preview rendering now uses stdin and the original file's directory for D2 CLI execution, fixing issues with imports and relative paths in D2 files

### Added
- Makefile with version bumping and common development tasks

## [1.0.7] - 2026-01-05
### Added
- Export support for additional formats: PDF, TXT, and PPTX (in addition to existing SVG and PNG)
- Copy to clipboard button in preview status bar for error messages

## [1.0.6] - 2026-01-02
### Fixed
- Removed deprecated API usage in the completion auto-popup implementation.

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