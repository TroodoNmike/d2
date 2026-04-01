package com.troodon.d2.markdown

import com.google.gson.Gson
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.JBColor
import com.troodon.d2.settings.D2SettingsState
import org.intellij.plugins.markdown.extensions.MarkdownBrowserPreviewExtension
import org.intellij.plugins.markdown.ui.preview.BrowserPipe
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel
import org.intellij.plugins.markdown.ui.preview.ResourceProvider

/**
 * Registers as a Markdown browser preview extension provider.
 *
 * When active it injects [d2-markdown-render.js] into the JCEF Markdown
 * preview. The script finds every <pre><code class="language-d2"> element,
 * sends the D2 source to Kotlin via BrowserPipe, and replaces the block with
 * the returned SVG wrapped in a Shadow DOM.
 *
 * Activated only when the Markdown plugin is present (optional dependency).
 */
class D2MarkdownPreviewExtension : MarkdownBrowserPreviewExtension.Provider {
    override fun createBrowserExtension(panel: MarkdownHtmlPanel): MarkdownBrowserPreviewExtension =
        D2BrowserExtension(panel)
}

// ---------------------------------------------------------------------------

private data class RenderRequest(val id: String, val source: String)
private data class RenderResult(val id: String, val svg: String? = null, val error: String? = null)

private class D2BrowserExtension(
    private val panel: MarkdownHtmlPanel
) : MarkdownBrowserPreviewExtension {

    private val log = Logger.getInstance(D2BrowserExtension::class.java)
    private val gson = Gson()

    @Volatile
    private var disposed = false

    /**
     * BrowserPipe.Handler is not a functional interface — must use object expression.
     * Called when JS posts 'd2/render' with payload {"id":"<req>","source":"<d2>"}.
     */
    private val renderHandler = object : BrowserPipe.Handler {
        override fun processMessageReceived(message: String): Boolean {
            val request = try {
                gson.fromJson(message, RenderRequest::class.java)
            } catch (e: Exception) {
                log.warn("D2: failed to parse render request: $message", e)
                return false
            }

            // D2 CLI is blocking — run on a pooled thread
            ApplicationManager.getApplication().executeOnPooledThread {
                if (disposed) return@executeOnPooledThread

                val resultJson = try {
                    val project = panel.project ?: return@executeOnPooledThread
                    val settings = D2SettingsState.getInstance(project)
                    // If the IDE is in dark mode and the user has not already set a --theme flag, inject D2's dark theme.
                    val themeArgs = if (!JBColor.isBright() && !settings.d2Arguments.contains("--theme")) "--theme 200" else ""
                    val effectiveArgs = listOf(themeArgs, settings.d2Arguments)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                    val svg = D2Runner.renderToSvg(
                        request.source,
                        settings.getEffectiveD2Path(),
                        effectiveArgs,
                        settings.useWsl,
                        settings.wslDistribution
                    )
                    gson.toJson(RenderResult(id = request.id, svg = svg))
                } catch (e: Exception) {
                    log.warn("D2: render failed for request ${request.id}", e)
                    gson.toJson(RenderResult(id = request.id, error = buildErrorMessage(e)))
                }

                if (!disposed) {
                    panel.browserPipe?.send("d2/result", resultJson)
                }
            }
            return true
        }
    }

    init {
        panel.browserPipe?.subscribe("d2/render", renderHandler)
    }

    override val scripts: List<String> = listOf("d2-markdown-render.js")
    override val styles: List<String> = emptyList()

    override val resourceProvider: ResourceProvider = object : ResourceProvider {
        override fun canProvide(resourceName: String): Boolean =
            resourceName == "d2-markdown-render.js"

        override fun loadResource(resourceName: String): ResourceProvider.Resource? =
            ResourceProvider.loadInternalResource(
                D2BrowserExtension::class.java,
                "/d2-markdown-render.js",
                "text/javascript"
            )
    }

    override fun dispose() {
        disposed = true
        panel.browserPipe?.removeSubscription("d2/render", renderHandler)
    }

    override fun compareTo(other: MarkdownBrowserPreviewExtension): Int = 0

    private fun buildErrorMessage(e: Exception): String {
        val msg = e.message ?: "Unknown error"
        return if (msg.contains("No such file", ignoreCase = true) ||
            msg.contains("not found", ignoreCase = true) ||
            msg.contains("cannot find", ignoreCase = true)
        ) {
            "D2 not found. Install D2 and set the path in Settings \u203A Tools \u203A D2 Diagram."
        } else {
            msg
        }
    }
}
