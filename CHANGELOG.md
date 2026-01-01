## [1.0.4]
- Improved syntax highlighting: numbers with units (e.g., `283.56USD`) are no longer incorrectly highlighted as numeric literals.

## [1.0.3]
- Added live SVG preview support for rendering D2 diagrams with compositions.
- Added a preview mode toggle (PNG or SVG/HTML) to the preview toolbar.
- Export now matches the active preview mode (.png or .svg).
- Changing D2 settings now automatically re-renders the preview.
- Added <code>--animate-interval=1000</code> to support multi-step diagrams (layers/scenarios/steps).
- Added a configurable auto-refresh debounce delay in D2 settings.