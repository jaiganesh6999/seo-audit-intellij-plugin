package com.seowebchecker.seoaudit.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.seowebchecker.seoaudit.crawler.CrawlerConfig
import com.seowebchecker.seoaudit.crawler.WebsiteCrawler
import com.seowebchecker.seoaudit.model.AuditCategory
import com.seowebchecker.seoaudit.model.SeoFinding
import com.seowebchecker.seoaudit.model.SeoScoringCalculator
import com.seowebchecker.seoaudit.model.Severity
import com.seowebchecker.seoaudit.quickfix.SeoQuickFixManager
import com.seowebchecker.seoaudit.reports.ReportExporter
import com.seowebchecker.seoaudit.rules.SeoAuditEngine
import com.seowebchecker.seoaudit.scanner.ProjectSourceScanner
import com.seowebchecker.seoaudit.settings.SeoAuditSettings
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.io.File
import javax.swing.*
import javax.swing.table.DefaultTableModel

/**
 * Main Interactive Tool Window Panel containing the dashboard, scan controls,
 * findings table, filtering, details inspection, quick-fix actions, and export buttons.
 */
class SeoAuditMainPanel(private val project: Project) {

    val contentPanel: JPanel = JPanel(BorderLayout())

    // Models & State
    private val findings = mutableListOf<SeoFinding>()
    private val tableModel = FindingsTableModel()
    private val findingsTable = JBTable(tableModel)

    // UI Components
    private val urlInputField = JBTextField("https://seowebchecker.com/", 24)
    private val scoreLabel = JBLabel("Score: -- / 100")
    private val summaryLabel = JBLabel("Total: 0 (Critical: 0, High: 0, Medium: 0, Low: 0)")
    private val statusLabel = JBLabel("Ready")

    // Filter controls
    private val categoryFilterBox = JComboBox<String>()
    private val severityFilterBox = JComboBox<String>()
    private val searchField = JBTextField(12)

    // Details panel
    private val detailsTitleLabel = JBLabel("Select a finding to inspect details")
    private val detailsExplanationArea = JTextArea()
    private val detailsEvidenceArea = JTextArea()
    private val detailsRecommendationArea = JTextArea()
    private val quickFixButton = JButton("Apply Quick Fix")
    private val openInEditorButton = JButton("Open in Editor")

    private val reportExporter = ReportExporter()

    init {
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // 1. Top Control Bar (URL crawl, Project scan, Export)
        val topPanel = JPanel(BorderLayout())
        val controlsRow = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))

        controlsRow.add(JBLabel("URL / Site:"))
        controlsRow.add(urlInputField)

        val crawlButton = JButton("Crawl & Audit")
        crawlButton.addActionListener { runLiveCrawl() }
        controlsRow.add(crawlButton)

        val projectScanButton = JButton("Audit Project Files")
        projectScanButton.addActionListener { runProjectScan() }
        controlsRow.add(projectScanButton)

        val exportHtmlButton = JButton("Export HTML")
        exportHtmlButton.addActionListener { exportReport("html") }
        controlsRow.add(exportHtmlButton)

        val exportJsonButton = JButton("Export JSON")
        exportJsonButton.addActionListener { exportReport("json") }
        controlsRow.add(exportJsonButton)

        val exportCsvButton = JButton("Export CSV")
        exportCsvButton.addActionListener { exportReport("csv") }
        controlsRow.add(exportCsvButton)

        // Health Score & Status Bar
        val scoreRow = JPanel(FlowLayout(FlowLayout.LEFT, 12, 4))
        scoreLabel.font = scoreLabel.font.deriveFont(Font.BOLD, 14f)
        scoreLabel.foreground = JBColor(0x007ACC, 0x4DA6FF)
        scoreRow.add(scoreLabel)
        scoreRow.add(summaryLabel)
        scoreRow.add(JSeparator(SwingConstants.VERTICAL))
        scoreRow.add(statusLabel)

        topPanel.add(controlsRow, BorderLayout.NORTH)
        topPanel.add(scoreRow, BorderLayout.SOUTH)
        contentPanel.add(topPanel, BorderLayout.NORTH)

        // 2. Filter Bar
        val filterRow = JPanel(FlowLayout(FlowLayout.LEFT, 6, 4))
        filterRow.add(JBLabel("Category:"))
        categoryFilterBox.addItem("All Categories")
        AuditCategory.entries.forEach { categoryFilterBox.addItem(it.displayName) }
        filterRow.add(categoryFilterBox)

        filterRow.add(JBLabel("Severity:"))
        severityFilterBox.addItem("All Severities")
        Severity.entries.forEach { severityFilterBox.addItem(it.displayName) }
        filterRow.add(severityFilterBox)

        filterRow.add(JBLabel("Search:"))
        filterRow.add(searchField)

        val filterWrapper = JPanel(BorderLayout())
        filterWrapper.add(filterRow, BorderLayout.NORTH)

        // Findings Table
        findingsTable.autoCreateRowSorter = true
        findingsTable.fillsViewportHeight = true
        val tableScrollPane = JBScrollPane(findingsTable)
        tableScrollPane.preferredSize = Dimension(600, 240)
        filterWrapper.add(tableScrollPane, BorderLayout.CENTER)

        // 3. Details and Action Panel
        val detailsPanel = JPanel(BorderLayout(8, 8))
        detailsPanel.border = BorderFactory.createEmptyBorder(6, 6, 6, 6)

        val detailsHeader = JPanel(BorderLayout())
        detailsTitleLabel.font = detailsTitleLabel.font.deriveFont(Font.BOLD, 12f)
        detailsHeader.add(detailsTitleLabel, BorderLayout.CENTER)

        val actionButtons = JPanel(FlowLayout(FlowLayout.RIGHT, 4, 0))
        quickFixButton.isEnabled = false
        openInEditorButton.isEnabled = false
        actionButtons.add(quickFixButton)
        actionButtons.add(openInEditorButton)
        detailsHeader.add(actionButtons, BorderLayout.EAST)
        detailsPanel.add(detailsHeader, BorderLayout.NORTH)

        val detailsContent = JPanel()
        detailsContent.layout = BoxLayout(detailsContent, BoxLayout.Y_AXIS)

        detailsExplanationArea.isEditable = false
        detailsExplanationArea.lineWrap = true
        detailsExplanationArea.wrapStyleWord = true
        detailsExplanationArea.rows = 2

        detailsEvidenceArea.isEditable = false
        detailsEvidenceArea.lineWrap = true
        detailsEvidenceArea.wrapStyleWord = true
        detailsEvidenceArea.rows = 2

        detailsRecommendationArea.isEditable = false
        detailsRecommendationArea.lineWrap = true
        detailsRecommendationArea.wrapStyleWord = true
        detailsRecommendationArea.rows = 2

        detailsContent.add(JBLabel("Explanation:"))
        detailsContent.add(JBScrollPane(detailsExplanationArea))
        detailsContent.add(Box.createVerticalStrut(4))
        detailsContent.add(JBLabel("Evidence:"))
        detailsContent.add(JBScrollPane(detailsEvidenceArea))
        detailsContent.add(Box.createVerticalStrut(4))
        detailsContent.add(JBLabel("Recommendation:"))
        detailsContent.add(JBScrollPane(detailsRecommendationArea))

        detailsPanel.add(detailsContent, BorderLayout.CENTER)
        detailsPanel.preferredSize = Dimension(600, 200)

        // Split pane between table and details
        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, filterWrapper, detailsPanel)
        splitPane.resizeWeight = 0.6
        contentPanel.add(splitPane, BorderLayout.CENTER)
    }

    private fun setupListeners() {
        categoryFilterBox.addActionListener { applyFilter() }
        severityFilterBox.addActionListener { applyFilter() }
        searchField.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = applyFilter()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = applyFilter()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = applyFilter()
        })

        findingsTable.selectionModel.addListSelectionListener {
            val selectedRow = findingsTable.selectedRow
            if (selectedRow >= 0) {
                val modelRow = findingsTable.convertRowIndexToModel(selectedRow)
                val finding = tableModel.getFindingAt(modelRow)
                displayDetails(finding)
            } else {
                clearDetails()
            }
        }

        quickFixButton.addActionListener {
            val selectedRow = findingsTable.selectedRow
            if (selectedRow >= 0) {
                val modelRow = findingsTable.convertRowIndexToModel(selectedRow)
                val finding = tableModel.getFindingAt(modelRow)
                applyQuickFix(finding)
            }
        }

        openInEditorButton.addActionListener {
            val selectedRow = findingsTable.selectedRow
            if (selectedRow >= 0) {
                val modelRow = findingsTable.convertRowIndexToModel(selectedRow)
                val finding = tableModel.getFindingAt(modelRow)
                openInEditor(finding)
            }
        }
    }

    private fun displayDetails(finding: SeoFinding) {
        detailsTitleLabel.text = "[${finding.severity.displayName}] ${finding.title} (${finding.ruleId})"
        detailsExplanationArea.text = finding.explanation
        detailsEvidenceArea.text = finding.evidence
        detailsRecommendationArea.text = finding.recommendation + if (finding.codeExample != null) "\n\nExample:\n${finding.codeExample}" else ""

        quickFixButton.isEnabled = finding.hasAutomatedFix && finding.affectedUrlOrFile.startsWith("file:") || File(finding.affectedUrlOrFile).exists()
        openInEditorButton.isEnabled = File(finding.affectedUrlOrFile).exists() || finding.affectedUrlOrFile.startsWith("file:")
    }

    private fun clearDetails() {
        detailsTitleLabel.text = "Select a finding to inspect details"
        detailsExplanationArea.text = ""
        detailsEvidenceArea.text = ""
        detailsRecommendationArea.text = ""
        quickFixButton.isEnabled = false
        openInEditorButton.isEnabled = false
    }

    private fun runLiveCrawl() {
        val url = urlInputField.text.trim()
        if (url.isEmpty()) {
            Messages.showWarningDialog("Please provide a target URL to audit.", "Missing URL")
            return
        }

        statusLabel.text = "Crawling $url..."
        val settings = SeoAuditSettings.instance
        val config = CrawlerConfig(
            maxPages = settings.crawlerMaxPages,
            maxDepth = settings.crawlerMaxDepth,
            timeoutMs = settings.crawlerTimeoutMs,
            userAgent = settings.crawlerUserAgent,
            respectRobotsTxt = settings.respectRobotsTxt,
            allowSubdomains = settings.allowSubdomains,
            bypassSsrfForLocalTesting = settings.bypassSsrfForLocalTesting
        )

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "SEO Audit: Crawling $url", true) {
            private val crawler = WebsiteCrawler(config)

            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                try {
                    val crawlFindings = crawler.crawl(url) { progress ->
                        indicator.text = "Crawling: ${progress.currentUrl}"
                        indicator.text2 = "Visited: ${progress.visitedCount} | Findings: ${progress.totalFindings}"
                        indicator.fraction = (progress.visitedCount.toDouble() / config.maxPages.toDouble()).coerceIn(0.0, 1.0)
                    }

                    ApplicationManager.getApplication().invokeLater {
                        updateFindings(crawlFindings)
                        statusLabel.text = "Audit completed for $url (${crawlFindings.size} findings)"
                    }
                } catch (e: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog("Crawl failed: ${e.message}", "Audit Error")
                        statusLabel.text = "Audit failed: ${e.message}"
                    }
                }
            }

            override fun onCancel() {
                crawler.cancel()
                statusLabel.text = "Crawl cancelled."
            }
        })
    }

    private fun runProjectScan() {
        val basePath = project.basePath
        if (basePath == null) {
            Messages.showWarningDialog("No active project folder found.", "Scan Error")
            return
        }

        statusLabel.text = "Scanning project files..."
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "SEO Audit: Scanning Project Files", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                val scanner = ProjectSourceScanner()
                val scanFindings = scanner.scanDirectory(File(basePath)) { file ->
                    indicator.text2 = "Analyzing: ${file.name}"
                }

                ApplicationManager.getApplication().invokeLater {
                    updateFindings(scanFindings)
                    statusLabel.text = "Project scan completed (${scanFindings.size} findings)"
                }
            }
        })
    }

    private fun updateFindings(newFindings: List<SeoFinding>) {
        findings.clear()
        findings.addAll(newFindings)
        applyFilter()

        val summary = SeoScoringCalculator.buildSummary(findings)
        scoreLabel.text = "Score: ${summary.healthScore} / 100"
        scoreLabel.foreground = when {
            summary.healthScore >= 80 -> JBColor(0x28A745, 0x5CD65C)
            summary.healthScore >= 50 -> JBColor(0xD9822B, 0xFFB366)
            else -> JBColor(0xDC3545, 0xFF6666)
        }
        summaryLabel.text = "Total: ${summary.totalFindings} (Critical: ${summary.criticalCount}, High: ${summary.highCount}, Medium: ${summary.mediumCount}, Low: ${summary.lowCount})"
    }

    private fun applyFilter() {
        val catFilter = categoryFilterBox.selectedItem as? String ?: "All Categories"
        val sevFilter = severityFilterBox.selectedItem as? String ?: "All Severities"
        val query = searchField.text.trim().lowercase()

        val filtered = findings.filter { finding ->
            val matchCat = catFilter == "All Categories" || finding.category.displayName == catFilter
            val matchSev = sevFilter == "All Severities" || finding.severity.displayName == sevFilter
            val matchQuery = query.isEmpty() ||
                    finding.title.lowercase().contains(query) ||
                    finding.ruleId.lowercase().contains(query) ||
                    finding.affectedUrlOrFile.lowercase().contains(query)
            matchCat && matchSev && matchQuery
        }

        tableModel.setFindings(filtered)
    }

    private fun applyQuickFix(finding: SeoFinding) {
        val path = finding.affectedUrlOrFile.removePrefix("file://")
        val file = File(path)
        if (!file.exists()) {
            Messages.showErrorDialog("Cannot locate source file: $path", "Quick Fix Error")
            return
        }

        val preview = SeoQuickFixManager.previewFix(file, finding)
        if (preview == null) {
            Messages.showWarningDialog("No automatic transformation is available for this finding.", "Quick Fix")
            return
        }

        val confirm = Messages.showYesNoDialog(
            project,
            "${preview.diffDescription}\n\nApply modification to ${file.name}?",
            "Confirm SEO Quick Fix",
            Messages.getQuestionIcon()
        )

        if (confirm == Messages.YES) {
            val success = SeoQuickFixManager.applyFix(file, finding)
            if (success) {
                Messages.showInfoMessage("Quick fix applied successfully to ${file.name}!", "Quick Fix Applied")
                // Rescan file and refresh
                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
                runProjectScan()
            } else {
                Messages.showErrorDialog("Failed to apply file modifications.", "Error")
            }
        }
    }

    private fun openInEditor(finding: SeoFinding) {
        val path = finding.affectedUrlOrFile.removePrefix("file://")
        val file = File(path)
        if (file.exists()) {
            val vFile = LocalFileSystem.getInstance().findFileByIoFile(file)
            if (vFile != null) {
                FileEditorManager.getInstance(project).openFile(vFile, true)
            }
        }
    }

    private fun exportReport(type: String) {
        if (findings.isEmpty()) {
            Messages.showWarningDialog("No findings available to export.", "Export Notice")
            return
        }

        val descriptor = FileSaverDescriptor("Export SEO Audit Report", "Choose file location", type)
        val fileChooser = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
        val target = urlInputField.text.ifBlank { project.name }
        val defaultName = "seo-audit-report-${System.currentTimeMillis()}.$type"
        val fileWrapper = fileChooser.save(null as com.intellij.openapi.vfs.VirtualFile?, defaultName)

        if (fileWrapper != null) {
            val content = when (type) {
                "html" -> reportExporter.exportHtml(target, findings)
                "json" -> reportExporter.exportJson(target, findings)
                "csv" -> reportExporter.exportCsv(findings)
                else -> reportExporter.exportMarkdown(target, findings)
            }
            fileWrapper.file.writeText(content)
            Messages.showInfoMessage("Report successfully exported to ${fileWrapper.file.name}!", "Report Exported")
        }
    }

    private class FindingsTableModel : DefaultTableModel() {
        private val columns = arrayOf("Severity", "Category", "Rule", "Title", "Affected Target")
        private val currentFindings = mutableListOf<SeoFinding>()

        override fun getColumnCount(): Int = columns.size
        override fun getColumnName(column: Int): String = columns[column]
        override fun getRowCount(): Int = currentFindings.size

        override fun getValueAt(row: Int, column: Int): Any {
            val finding = currentFindings[row]
            return when (column) {
                0 -> finding.severity.displayName
                1 -> finding.category.displayName
                2 -> finding.ruleId
                3 -> finding.title
                4 -> finding.affectedUrlOrFile
                else -> ""
            }
        }

        override fun isCellEditable(row: Int, column: Int): Boolean = false

        fun setFindings(newFindings: List<SeoFinding>) {
            currentFindings.clear()
            currentFindings.addAll(newFindings)
            fireTableDataChanged()
        }

        fun getFindingAt(row: Int): SeoFinding = currentFindings[row]
    }
}
