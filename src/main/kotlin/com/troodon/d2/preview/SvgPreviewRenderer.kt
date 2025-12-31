package com.troodon.d2.preview

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.browser.CefMessageRouter
import org.cef.handler.CefMessageRouterHandlerAdapter
import java.awt.BorderLayout
import java.io.File
import java.nio.file.Files
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class SvgPreviewRenderer : PreviewRenderer {

    private val LOG = Logger.getInstance(SvgPreviewRenderer::class.java)
    private val browser: JBCefBrowser? = if (JBCefApp.isSupported()) JBCefBrowser() else null
    private val panel = JPanel(BorderLayout())

    private val zoomStep = 0.1
    private val minZoom = 0.1
    private val maxZoom = 5.0

    @Volatile
    private var isBrowserReady = false
    private var pendingRenderTask: (() -> Unit)? = null

    // Track zoom and scroll state (volatile for thread safety)
    @Volatile
    private var savedZoom: Double = 1.0
    @Volatile
    private var savedScrollLeft: Int = 0
    @Volatile
    private var savedScrollTop: Int = 0

    init {
        if (browser != null) {
            panel.add(browser.component, BorderLayout.CENTER)
            LOG.info("JCEF browser initialized successfully")

            // Add message router to capture console messages
            val msgRouter = CefMessageRouter.create()
            msgRouter.addHandler(object : CefMessageRouterHandlerAdapter() {
                override fun onQuery(
                    browser: CefBrowser?,
                    frame: CefFrame?,
                    queryId: Long,
                    request: String?,
                    persistent: Boolean,
                    callback: org.cef.callback.CefQueryCallback?
                ): Boolean {
                    if (request?.startsWith("D2_STATE:") == true) {
                        try {
                            val json = request.substring(9)
                            val zoomMatch = Regex("\"zoom\":(\\d+\\.?\\d*)").find(json)
                            val scrollLeftMatch = Regex("\"scrollLeft\":(\\d+)").find(json)
                            val scrollTopMatch = Regex("\"scrollTop\":(\\d+)").find(json)

                            zoomMatch?.groupValues?.get(1)?.toDoubleOrNull()?.let {
                                val oldZoom = savedZoom
                                savedZoom = it
                                LOG.info("Updated savedZoom: $oldZoom -> $it")
                            }
                            scrollLeftMatch?.groupValues?.get(1)?.toIntOrNull()?.let {
                                val oldScrollLeft = savedScrollLeft
                                savedScrollLeft = it
                                LOG.info("Updated savedScrollLeft: $oldScrollLeft -> $it")
                            }
                            scrollTopMatch?.groupValues?.get(1)?.toIntOrNull()?.let {
                                val oldScrollTop = savedScrollTop
                                savedScrollTop = it
                                LOG.info("Updated savedScrollTop: $oldScrollTop -> $it")
                            }
                        } catch (e: Exception) {
                            LOG.warn("Failed to parse state", e)
                        }
                        callback?.success("")
                        return true
                    }
                    return false
                }
            }, true)
            browser.jbCefClient.cefClient.addMessageRouter(msgRouter)

            // Add load handler to detect when browser is ready
            browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
                override fun onLoadEnd(cefBrowser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                    if (frame?.isMain == true) {
                        LOG.info("JCEF browser ready (httpStatusCode: $httpStatusCode)")
                        isBrowserReady = true

                        // Execute pending render if any
                        pendingRenderTask?.let { task ->
                            ApplicationManager.getApplication().invokeLater {
                                task()
                                pendingRenderTask = null
                            }
                        }
                    }
                }
            }, browser.cefBrowser)

            browser.loadHTML("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100vh;
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                            background: #f5f5f5;
                        }
                        .loader-container {
                            text-align: center;
                        }
                        .spinner {
                            width: 50px;
                            height: 50px;
                            margin: 0 auto 20px;
                            border: 4px solid #e0e0e0;
                            border-top: 4px solid #2196F3;
                            border-radius: 50%;
                            animation: spin 1s linear infinite;
                        }
                        @keyframes spin {
                            0% { transform: rotate(0deg); }
                            100% { transform: rotate(360deg); }
                        }
                        h2 {
                            color: #333;
                            font-weight: 500;
                            margin: 0;
                            font-size: 18px;
                        }
                        p {
                            color: #666;
                            font-size: 14px;
                            margin: 10px 0 0 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="loader-container">
                        <div class="spinner"></div>
                        <h2>D2 Preview Loading</h2>
                        <p>Initializing browser...</p>
                    </div>
                </body>
                </html>
            """.trimIndent())
        } else {
            val errorLabel = JLabel("<html><center>JCEF browser not supported.<br>SVG preview unavailable.</center></html>")
            errorLabel.horizontalAlignment = JLabel.CENTER
            panel.add(errorLabel, BorderLayout.CENTER)
            LOG.warn("JCEF browser not supported")
        }
    }

    override fun getComponent(): JComponent = panel

    override fun render(sourceFile: File, outputFile: File) {
        val renderTask = {
            ApplicationManager.getApplication().invokeLater {
                try {
                    if (browser == null) {
                        LOG.warn("JCEF browser not available")
                        return@invokeLater
                    }

                    if (!outputFile.exists()) {
                        LOG.warn("Output file not found: ${outputFile.absolutePath}")
                        return@invokeLater
                    }

                    // Capture state at the start to avoid race conditions
                    val currentZoom = savedZoom
                    val currentScrollLeft = savedScrollLeft
                    val currentScrollTop = savedScrollTop

                    // Read SVG content
                    val svgContent = Files.readString(outputFile.toPath())

                    LOG.info("Rendering with captured state - zoom: $currentZoom, scrollLeft: $currentScrollLeft, scrollTop: $currentScrollTop")

                // Create HTML wrapper with zoom and pan support
                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            html, body {
                                width: 100%;
                                height: 100%;
                                margin: 0;
                                padding: 0;
                                overflow: hidden;
                                background: white;
                            }

                            #viewport {
                                width: 100%;
                                height: 100%;
                                overflow: auto;
                                cursor: grab;
                            }

                            #viewport.dragging {
                                cursor: grabbing;
                            }

                            #svg-container {
                                padding: 20px;
                                transform-origin: top left;
                                display: inline-block;
                                min-width: 100%;
                            }

                            #svg-container svg {
                                display: block;
                                width: 100%;
                                height: auto;
                            }
                        </style>
                        <script>
                            let isPanning = false;
                            let startX = 0;
                            let startY = 0;
                            let scrollLeft = 0;
                            let scrollTop = 0;
                            let currentZoom = $currentZoom;
                            let isInitializing = true;
                            let updateTimeout;

                            window.addEventListener('DOMContentLoaded', function() {
                                const viewport = document.getElementById('viewport');
                                const container = document.getElementById('svg-container');

                                // Function to update Kotlin state
                                function updateKotlinState() {
                                    // Don't send updates during initialization
                                    if (isInitializing) {
                                        console.log('Skipping state update - still initializing');
                                        return;
                                    }
                                    const state = {
                                        zoom: currentZoom,
                                        scrollLeft: viewport.scrollLeft,
                                        scrollTop: viewport.scrollTop
                                    };
                                    console.log('Sending state update:', state);
                                    // Send state to Kotlin via message router
                                    if (window.cefQuery) {
                                        window.cefQuery({
                                            request: 'D2_STATE:' + JSON.stringify(state),
                                            onSuccess: function(response) {},
                                            onFailure: function(error_code, error_message) {
                                                console.error('Failed to send state:', error_code, error_message);
                                            }
                                        });
                                    } else {
                                        console.warn('window.cefQuery not available');
                                    }
                                }

                                // Throttle state updates to avoid too many messages
                                function saveScrollPosition() {
                                    clearTimeout(updateTimeout);
                                    updateTimeout = setTimeout(updateKotlinState, 100);
                                }

                                viewport.addEventListener('scroll', saveScrollPosition);

                                // Restore saved zoom and scroll from Kotlin
                                container.style.transform = 'scale(' + currentZoom + ')';

                                // Use setTimeout to ensure DOM is fully ready and layout is complete
                                setTimeout(function() {
                                    viewport.scrollLeft = $currentScrollLeft;
                                    viewport.scrollTop = $currentScrollTop;

                                    // Allow state updates after restoration is complete
                                    setTimeout(function() {
                                        isInitializing = false;
                                    }, 200);
                                }, 50);

                                // Handle mouse wheel for zoom with Ctrl key
                                viewport.addEventListener('wheel', function(e) {
                                    if (e.ctrlKey || e.metaKey) {
                                        e.preventDefault();

                                        const container = document.getElementById('svg-container');
                                        const currentTransform = window.getComputedStyle(container).transform;
                                        let currentScale = 1;

                                        if (currentTransform && currentTransform !== 'none') {
                                            const matrix = currentTransform.match(/matrix\(([^)]+)\)/);
                                            if (matrix) {
                                                currentScale = parseFloat(matrix[1].split(',')[0]);
                                            }
                                        }

                                        // Determine zoom direction
                                        const delta = -Math.sign(e.deltaY);
                                        const zoomStep = 0.1;
                                        let newScale = currentScale + (delta * zoomStep);

                                        // Clamp between min and max zoom
                                        newScale = Math.max(0.1, Math.min(5.0, newScale));

                                        currentZoom = newScale;
                                        container.style.transform = 'scale(' + newScale + ')';
                                        // Immediate update for zoom, no throttling
                                        clearTimeout(updateTimeout);
                                        updateKotlinState();
                                    }
                                }, { passive: false });

                                // Handle click and drag to pan
                                viewport.addEventListener('mousedown', function(e) {
                                    isPanning = true;
                                    viewport.classList.add('dragging');
                                    startX = e.clientX;
                                    startY = e.clientY;
                                    scrollLeft = viewport.scrollLeft;
                                    scrollTop = viewport.scrollTop;
                                    e.preventDefault();
                                });

                                viewport.addEventListener('mousemove', function(e) {
                                    if (!isPanning) return;
                                    e.preventDefault();

                                    const deltaX = e.clientX - startX;
                                    const deltaY = e.clientY - startY;

                                    viewport.scrollLeft = scrollLeft - deltaX;
                                    viewport.scrollTop = scrollTop - deltaY;
                                });

                                viewport.addEventListener('mouseup', function() {
                                    isPanning = false;
                                    viewport.classList.remove('dragging');
                                    // Delayed update to ensure scroll position is final
                                    setTimeout(updateKotlinState, 50);
                                });

                                viewport.addEventListener('mouseleave', function() {
                                    if (isPanning) {
                                        isPanning = false;
                                        viewport.classList.remove('dragging');
                                        // Save state when drag ends
                                        setTimeout(updateKotlinState, 50);
                                    }
                                });
                            });
                        </script>
                    </head>
                    <body>
                        <div id="viewport">
                            <div id="svg-container">
                                $svgContent
                            </div>
                        </div>
                    </body>
                    </html>
                """.trimIndent()

                    // Load HTML into browser
                    browser.loadHTML(html)

                    // No need to call updateZoom() - the HTML already has the saved zoom injected
                } catch (e: Exception) {
                    LOG.error("Failed to load SVG", e)
                }
            }
        }

        // If browser is ready, execute immediately; otherwise, queue for later
        if (isBrowserReady) {
            renderTask()
        } else {
            LOG.info("Browser not ready yet, queuing render task")
            pendingRenderTask = renderTask
        }
    }

    override fun zoomIn() {
        if (savedZoom < maxZoom) {
            savedZoom = (savedZoom + zoomStep).coerceAtMost(maxZoom)
            LOG.info("Zoom in button: savedZoom = $savedZoom")
            updateZoom()
        }
    }

    override fun zoomOut() {
        if (savedZoom > minZoom) {
            savedZoom = (savedZoom - zoomStep).coerceAtLeast(minZoom)
            LOG.info("Zoom out button: savedZoom = $savedZoom")
            updateZoom()
        }
    }

    override fun getFileExtension(): String = ".svg"

    override fun dispose() {
        browser?.dispose()
    }

    private fun updateZoom() {
        // Use savedZoom directly, don't overwrite it
        browser?.cefBrowser?.executeJavaScript(
            """
            (function() {
                var container = document.getElementById('svg-container');
                console.log('Applying zoom: $savedZoom, container exists:', !!container);
                if (container) {
                    currentZoom = $savedZoom;
                    container.style.transform = 'scale($savedZoom)';
                    console.log('Transform applied:', container.style.transform);

                    // Restore scroll position if saved
                    var viewport = document.getElementById('viewport');
                    if (viewport) {
                        viewport.scrollLeft = $savedScrollLeft;
                        viewport.scrollTop = $savedScrollTop;
                    }
                }
            })();
            """.trimIndent(),
            browser.cefBrowser.url, 0
        )
    }
}
