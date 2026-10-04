package com.seowebchecker.seoaudit.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * IDE Settings configurable panel for SEO Audit preferences.
 */
class SeoAuditConfigurable : Configurable {

    private var myMainPanel: JPanel? = null
    private val maxPagesField = JBTextField()
    private val maxDepthField = JBTextField()
    private val timeoutField = JBTextField()
    private val userAgentField = JBTextField()
    private val respectRobotsTxtBox = JBCheckBox("Respect robots.txt crawl directives by default")
    private val allowSubdomainsBox = JBCheckBox("Allow crawling subdomains")
    private val bypassSsrfBox = JBCheckBox("Allow local / private IP targets (for local dev testing only)")

    override fun getDisplayName(): String = "Advanced SEO Audit"

    override fun createComponent(): JComponent {
        val settings = SeoAuditSettings.instance

        maxPagesField.text = settings.crawlerMaxPages.toString()
        maxDepthField.text = settings.crawlerMaxDepth.toString()
        timeoutField.text = settings.crawlerTimeoutMs.toString()
        userAgentField.text = settings.crawlerUserAgent
        respectRobotsTxtBox.isSelected = settings.respectRobotsTxt
        allowSubdomainsBox.isSelected = settings.allowSubdomains
        bypassSsrfBox.isSelected = settings.bypassSsrfForLocalTesting

        myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Maximum Crawl Pages: "), maxPagesField, 1, false)
            .addLabeledComponent(JBLabel("Maximum Crawl Depth: "), maxDepthField, 1, false)
            .addLabeledComponent(JBLabel("Request Timeout (ms): "), timeoutField, 1, false)
            .addLabeledComponent(JBLabel("Crawler User-Agent: "), userAgentField, 1, false)
            .addComponent(respectRobotsTxtBox, 1)
            .addComponent(allowSubdomainsBox, 1)
            .addComponent(bypassSsrfBox, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return myMainPanel!!
    }

    override fun isModified(): Boolean {
        val settings = SeoAuditSettings.instance
        return maxPagesField.text != settings.crawlerMaxPages.toString() ||
                maxDepthField.text != settings.crawlerMaxDepth.toString() ||
                timeoutField.text != settings.crawlerTimeoutMs.toString() ||
                userAgentField.text != settings.crawlerUserAgent ||
                respectRobotsTxtBox.isSelected != settings.respectRobotsTxt ||
                allowSubdomainsBox.isSelected != settings.allowSubdomains ||
                bypassSsrfBox.isSelected != settings.bypassSsrfForLocalTesting
    }

    override fun apply() {
        val settings = SeoAuditSettings.instance
        settings.crawlerMaxPages = maxPagesField.text.toIntOrNull() ?: 50
        settings.crawlerMaxDepth = maxDepthField.text.toIntOrNull() ?: 3
        settings.crawlerTimeoutMs = timeoutField.text.toIntOrNull() ?: 10000
        settings.crawlerUserAgent = userAgentField.text
        settings.respectRobotsTxt = respectRobotsTxtBox.isSelected
        settings.allowSubdomains = allowSubdomainsBox.isSelected
        settings.bypassSsrfForLocalTesting = bypassSsrfBox.isSelected
    }

    override fun reset() {
        val settings = SeoAuditSettings.instance
        maxPagesField.text = settings.crawlerMaxPages.toString()
        maxDepthField.text = settings.crawlerMaxDepth.toString()
        timeoutField.text = settings.crawlerTimeoutMs.toString()
        userAgentField.text = settings.crawlerUserAgent
        respectRobotsTxtBox.isSelected = settings.respectRobotsTxt
        allowSubdomainsBox.isSelected = settings.allowSubdomains
        bypassSsrfBox.isSelected = settings.bypassSsrfForLocalTesting
    }
}
