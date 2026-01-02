## [1.0.5]
- Added comprehensive autocomplete feature:
  - Identifier completion: Suggests defined objects and connections from the current file
  - Node property completion: Suggests `shape`, `icon`, `style`, and `label` properties when inside node blocks, excluding already-defined properties
  - Shape value completion: Suggests all 18 available D2 shapes (rectangle, circle, diamond, etc.) after `shape:` property
  - Dynamic refresh: Autocomplete list updates as you type, picking up newly added identifiers
  - Context-aware: Excludes the current node name when completing inside that node's block

## [1.0.4]
- Improved syntax highlighting: numbers with units (e.g., `283.56USD`) are no longer incorrectly highlighted as numeric literals.

## [1.0.3]
- Added live SVG preview support for rendering D2 diagrams with compositions.
- Added a preview mode toggle (PNG or SVG/HTML) to the preview toolbar.
- Export now matches the active preview mode (.png or .svg).
- Changing D2 settings now automatically re-renders the preview.
- Added <code>--animate-interval=1000</code> to support multi-step diagrams (layers/scenarios/steps).
- Added a configurable auto-refresh debounce delay in D2 settings.