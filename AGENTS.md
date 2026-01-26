# AGENTS.md

This file provides guidance to coding agents working in this repository.

## Project Overview

This is a JetBrains IntelliJ Platform plugin that provides D2 (Declarative Diagramming) language support for IntelliJ-based IDEs. The plugin offers syntax highlighting, live preview, autocomplete, and export capabilities for `.d2` files.

**Plugin ID:** `com.troodon.d2`  
**Current Version:** 1.0.7  
**Minimum IDE Build:** 243 (IntelliJ IDEA 2024.3+)  
**Target JVM:** 21  
**Primary Language:** Kotlin

## Development Commands

```bash
# Build the plugin
./gradlew build

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.troodon.d2.preview.D2PreviewPanelTest"

# Run a single test method
./gradlew test --tests "com.troodon.d2.lexer.D2LexerTest.testBasicTokens"

# Build plugin ZIP for distribution
./gradlew buildPlugin

# Clean build artifacts
./gradlew clean
```

## Architecture Overview

### Core Components

**1. Language Support (com.troodon.d2.lang)**
- `D2Language`: Defines the D2 language instance
- `D2FileType`: Registers `.d2` file type with custom icon
- `D2ParserDefinition`: Wires up lexer, parser, and PSI elements
- `D2Parser`: Parses D2 syntax into PSI tree
- `D2PsiFile`: PSI representation of D2 files
- `D2Types`: Token type definitions

**2. Lexical Analysis (com.troodon.d2.lexer)**
- `D2Lexer`: Tokenizes D2 source code
- `D2TokenType`: Custom token types for D2 syntax

**3. Editor Features (com.troodon.d2.editor)**
- `D2SyntaxHighlighter`: Provides color-coded syntax highlighting
- `D2CompletionContributor`: Autocomplete framework
  - `D2IdentifierCompletionProvider`: Suggests defined objects/connections
  - `D2NodePropertyCompletionProvider`: Suggests node properties (shape, icon, style, label)
  - `D2ShapeCompletionProvider`: Suggests shape values (rectangle, circle, etc.)
- `D2TypedHandler`: Auto-triggers completion as user types
- `D2PairedBraceMatcher`: Brace matching for `{}` pairs
- `D2Commenter`: Line and block comment support

**4. Live Preview System (com.troodon.d2.preview)**
- `D2SplitEditorProvider`: Creates split editor with code on left, preview on right
- `D2PreviewPanel`: Main preview UI with toolbar and controls
  - Auto-refresh with configurable debounce delay (default 1000ms)
  - Manual refresh button
  - Auto-format toggle (uses `d2 fmt`)
  - Preview mode toggle (SVG/PNG)
  - Zoom controls
  - Export menu (PNG, SVG, PDF, TXT, PPTX)
- `PreviewRenderer`: Interface for rendering strategies
  - `SvgPreviewRenderer`: Renders SVG in HTML viewer
  - `PngPreviewRenderer`: Renders PNG images

**5. Settings (com.troodon.d2.settings)**
- `D2SettingsState`: Project-level persistent settings
  - `d2CliPath`: Path to D2 executable (auto-detects if empty)
  - `d2Arguments`: Additional CLI args (default: `--animate-interval=1000`)
  - `debounceDelay`: Auto-refresh delay in ms (default: 1000)
- `D2SettingsConfigurable`: Settings UI panel
- `D2CliValidator`: Validates D2 installation and finds executable
- `D2LanguageCodeStyleSettingsProvider`: Code style configuration
- `D2ColorSettingsPage`: Color scheme customization

## Testing

Tests are located in `src/test/kotlin/com/troodon/d2/` mirroring the main source structure.

## Notes

- Plugin changelog and description are auto-generated from `CHANGELOG.md` and `README.md` during build (`build.gradle.kts`).
- Preview uses temp files in `java.io.tmpdir` for D2 CLI input/output coordination.
- Auto-refresh uses debounce to avoid excessive re-rendering.
