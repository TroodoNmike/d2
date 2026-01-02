# D2 Diagram Plugin

[![JetBrains Plugins](https://img.shields.io/badge/JetBrains-Plugin-blue)](https://plugins.jetbrains.com/plugin/29542-d2-diagram)
**JetBrains Marketplace:** https://plugins.jetbrains.com/plugin/29542-d2-diagram

Comprehensive D2 language support for IntelliJ-based IDEs. Create beautiful diagrams with syntax highlighting, live preview, and interactive editing.

## ✨ Features

- 🎨 **Syntax Highlighting** - Color-coded syntax for D2 diagram files
- 👁️ **Live Preview** - Real-time diagram rendering with auto-refresh
- ⚡ **Split Editor** - Edit D2 code and preview side-by-side
- 🖼️ **Preview Modes** - Toggle preview rendering between **SVG (HTML)** and **PNG**
- 🖱️ **Interactive Preview** - Pan/drag to move, zoom controls (SVG also supports Ctrl/Cmd + scroll)
- 📤 **Export** - Export respects the active preview mode (**.svg** or **.png**)
- 🔧 **Auto-format** - Automatic code formatting using `d2 fmt`
- ⏱️ **Configurable Auto-refresh Delay** - Adjust the debounce delay used for auto-refresh
- ⌨️ **Smart Editing** - Brace matching, commenting, and code style settings
- 💡 **Autocomplete** - Smart completion for identifiers, node properties, and shape values
- 🎯 **File Type Icon** - Custom icon for `.d2` files in project tree
- ⚙️ **Configurable D2 CLI** - Set the D2 executable path and additional CLI arguments (e.g., `--animate-interval=1000`)

## 📋 Requirements

**D2 CLI** must be installed on your system.

### Installation Options

**Using install script:**
```bash
curl -fsSL https://d2lang.com/install.sh | sh -s --
```

**Using Homebrew (macOS/Linux):**
```bash
brew install d2
```

For other installation methods, visit [d2lang.com](https://d2lang.com/tour/install).

## 🚀 Getting Started

1. **Install the plugin** from JetBrains Marketplace
2. **Install D2 CLI** (see installation options above)
3. **Configure D2 path** in `Settings → Tools → D2 Diagram`
4. **Create a `.d2` file** or open an existing one
5. **Start diagramming!** Your preview will update in real-time as you type

## 💡 About D2

D2 is a modern diagram scripting language that turns text into diagrams. It's designed to be easy to learn, powerful, and flexible.

**Example D2 code:**
```d2
x -> y: hello world
```

Learn more at [d2lang.com](https://d2lang.com).

## 🤝 Contributing

Contributions are welcome! Feel free to submit issues or pull requests on [GitHub](https://github.com/TroodoNmike/d2).
