package com.troodon.d2.preview

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import java.io.File
import java.nio.file.Files
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer

class SvgPreviewRenderer(private val project: Project) : PreviewRenderer {

    private val LOG = Logger.getInstance(SvgPreviewRenderer::class.java)
    private val browser: JBCefBrowser? = if (JBCefApp.isSupported()) JBCefBrowser() else null
    private val panel = JPanel(BorderLayout())

    private var zoomLevel = 1.0
    private val zoomStep = 0.1
    private val minZoom = 0.1
    private val maxZoom = 5.0

    init {
        if (browser != null) {
            panel.add(browser.component, BorderLayout.CENTER)
            LOG.info("JCEF browser initialized successfully")
            browser.loadHTML("<html><body><h1>D2 Preview Loading...</h1></body></html>")
        } else {
            val errorLabel = JLabel("<html><center>JCEF browser not supported.<br>SVG preview unavailable.</center></html>")
            errorLabel.horizontalAlignment = JLabel.CENTER
            panel.add(errorLabel, BorderLayout.CENTER)
            LOG.warn("JCEF browser not supported")
        }
    }

    override fun getComponent(): JComponent = panel

    override fun render(sourceFile: File, outputFile: File) {
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

                // Read SVG content
                val svgContent = Files.readString(outputFile.toPath())

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

                            window.addEventListener('DOMContentLoaded', function() {
                                const viewport = document.getElementById('viewport');

                                // Save scroll position periodically
                                function saveScrollPosition() {
                                    window.savedScrollLeft = viewport.scrollLeft;
                                    window.savedScrollTop = viewport.scrollTop;
                                }

                                viewport.addEventListener('scroll', saveScrollPosition);

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

                                        container.style.transform = 'scale(' + newScale + ')';
                                        saveScrollPosition();
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
                                    saveScrollPosition();
                                });

                                viewport.addEventListener('mouseleave', function() {
                                    isPanning = false;
                                    viewport.classList.remove('dragging');
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

                // Apply zoom after a short delay to ensure content is loaded
                SwingUtilities.invokeLater {
                    Timer(100) { _ -> updateZoom() }.apply {
                        isRepeats = false
                        start()
                    }
                }
            } catch (e: Exception) {
                LOG.error("Failed to load SVG", e)
            }
        }
    }

    override fun zoomIn() {
        if (zoomLevel < maxZoom) {
            zoomLevel = (zoomLevel + zoomStep).coerceAtMost(maxZoom)
            updateZoom()
        }
    }

    override fun zoomOut() {
        if (zoomLevel > minZoom) {
            zoomLevel = (zoomLevel - zoomStep).coerceAtLeast(minZoom)
            updateZoom()
        }
    }

    override fun getFileExtension(): String = ".svg"

    override fun dispose() {
        browser?.dispose()
    }

    private fun updateZoom() {
        browser?.cefBrowser?.executeJavaScript(
            """
            (function() {
                var container = document.getElementById('svg-container');
                console.log('Applying zoom: $zoomLevel, container exists:', !!container);
                if (container) {
                    container.style.transform = 'scale($zoomLevel)';
                    console.log('Transform applied:', container.style.transform);

                    // Restore scroll position if saved
                    var viewport = document.getElementById('viewport');
                    if (viewport && window.savedScrollLeft !== undefined && window.savedScrollTop !== undefined) {
                        viewport.scrollLeft = window.savedScrollLeft;
                        viewport.scrollTop = window.savedScrollTop;
                    }
                }
            })();
            """.trimIndent(),
            browser.cefBrowser.url, 0
        )
    }
}
