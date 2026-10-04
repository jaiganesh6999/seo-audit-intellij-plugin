package com.seowebchecker.seoaudit.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Tool window factory registering the Advanced SEO Audit panel in the IDE.
 */
class SeoAuditToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowPanel = SeoAuditMainPanel(project)
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(toolWindowPanel.contentPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
