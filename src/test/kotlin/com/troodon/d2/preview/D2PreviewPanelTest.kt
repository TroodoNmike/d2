package com.troodon.d2.preview

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import kotlin.test.assertNotNull

class D2PreviewPanelTest : BasePlatformTestCase() {

    private lateinit var previewPanel: D2PreviewPanel

    override fun setUp() {
        super.setUp()

        // Create a test D2 file
        val file = myFixture.configureByText("test.d2", "a -> b")
        val editor = myFixture.editor
        val virtualFile = file.virtualFile

        previewPanel = D2PreviewPanel(project, editor)
    }

    override fun tearDown() {
        try {
            previewPanel.dispose()
        } finally {
            super.tearDown()
        }
    }

    @Test
    fun `test component is not null`() {
        val component = previewPanel.component
        assertNotNull(component)
    }

    @Test
    fun `test documentListener is not null`() {
        val listener = previewPanel.documentListener
        assertNotNull(listener)
    }

    @Test
    fun `test documentListener handles document changes`() {
        val listener = previewPanel.documentListener

        ApplicationManager.getApplication().runWriteAction {
            myFixture.editor.document.setText("x -> y")
        }

        // Should not throw
        // The listener is designed to debounce and handle changes asynchronously
    }

    @Test
    fun `test dispose does not throw exception`() {
        // Should not throw
        previewPanel.dispose()
    }

    @Test
    fun `test multiple dispose calls are safe`() {
        previewPanel.dispose()
        previewPanel.dispose() // Should be safe to call multiple times
    }

    @Test
    fun `test preview panel with empty document`() {
        val file = myFixture.configureByText("empty.d2", "")
        val editor = myFixture.editor
        val virtualFile = file.virtualFile

        val panel = D2PreviewPanel(project, editor)
        try {
            assertNotNull(panel.component)
        } finally {
            panel.dispose()
        }
    }

    @Test
    fun `test preview panel with complex D2 content`() {
        val complexContent = """
            network: {
              cell tower: {
                satellites: {
                  shape: stored_data
                  style.multiple: true
                }

                transmitter

                satellites -> transmitter: send
                satellites -> transmitter: send
                satellites -> transmitter: send
              }

              online portal: {
                ui: {shape: hexagon}
              }

              data processor: {
                storage: {
                  shape: cylinder
                  style.multiple: true
                }
              }

              cell tower.transmitter -> data processor.storage: phone logs
            }

            user: {
              shape: person
              width: 130
            }

            user -> network.cell tower: make call
            user -> network.online portal.ui: access {
              style.stroke-dash: 3
            }

            api server -> network.online portal.ui: display
            api server -> logs: persist
            logs: {shape: page; style.multiple: true}

            network.data processor -> api server
        """.trimIndent()

        val file = myFixture.configureByText("complex.d2", complexContent)
        val editor = myFixture.editor
        val virtualFile = file.virtualFile

        val panel = D2PreviewPanel(project, editor)
        try {
            assertNotNull(panel.component)
        } finally {
            panel.dispose()
        }
    }

    @Test
    fun `test document listener ignores changes during formatting`() {
        val listener = previewPanel.documentListener

        // Simulate document change
        ApplicationManager.getApplication().runWriteAction {
            myFixture.editor.document.setText("new content")
        }

        // Should not throw even with rapid changes
        ApplicationManager.getApplication().runWriteAction {
            myFixture.editor.document.setText("another change")
        }
    }

    @Test
    fun `test preview panel handles special characters in content`() {
        val specialContent = """
            title: "Test with 'quotes' and \"double quotes\""
            node1: "Content with\nnewlines"
            node2: "Unicode: 你好 🎉"
        """.trimIndent()

        val file = myFixture.configureByText("special.d2", specialContent)
        val editor = myFixture.editor
        val virtualFile = file.virtualFile

        val panel = D2PreviewPanel(project, editor)
        try {
            assertNotNull(panel.component)
        } finally {
            panel.dispose()
        }
    }
}
