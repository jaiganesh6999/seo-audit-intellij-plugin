package com.seowebchecker.seoaudit.scanner

import com.seowebchecker.seoaudit.model.*
import com.seowebchecker.seoaudit.rules.SeoAuditEngine
import org.jsoup.Jsoup
import java.io.File
import java.util.UUID

/**
 * Scans local project source files (HTML, PHP, JSX, TSX, Vue, Svelte, XML, robots.txt)
 * without requiring a running web server.
 */
class ProjectSourceScanner(
    private val auditEngine: SeoAuditEngine = SeoAuditEngine()
) {

    private val supportedExtensions = setOf(
        "html", "htm", "php", "phtml", "jsx", "tsx", "vue", "svelte", "xml", "txt"
    )

    fun scanDirectory(
        baseDir: File,
        onFileScanned: ((File) -> Unit)? = null
    ): List<SeoFinding> {
        val findings = mutableListOf<SeoFinding>()

        if (!baseDir.exists() || !baseDir.isDirectory) {
            return emptyList()
        }

        baseDir.walkTopDown()
            .filter { file ->
                file.isFile &&
                        file.extension.lowercase() in supportedExtensions &&
                        !file.path.contains(".git") &&
                        !file.path.contains("node_modules") &&
                        !file.path.contains("build") &&
                        !file.path.contains("dist") &&
                        !file.path.contains(".gradle")
            }
            .forEach { file ->
                onFileScanned?.invoke(file)
                val fileFindings = scanFile(file)
                findings.addAll(fileFindings)
            }

        return findings
    }

    fun scanFile(file: File): List<SeoFinding> {
        val content = try {
            file.readText()
        } catch (e: Exception) {
            return emptyList()
        }

        val ext = file.extension.lowercase()
        val findings = mutableListOf<SeoFinding>()

        when {
            file.name.equals("robots.txt", ignoreCase = true) -> {
                findings.addAll(scanRobotsTxt(file, content))
            }
            ext == "xml" && file.name.contains("sitemap", ignoreCase = true) -> {
                findings.addAll(scanSitemapXml(file, content))
            }
            ext in setOf("html", "htm", "php", "phtml", "vue", "svelte") -> {
                findings.addAll(scanHtmlLikeFile(file, content))
            }
            ext in setOf("jsx", "tsx") -> {
                findings.addAll(scanReactTemplates(file, content))
            }
        }

        // Generic hygiene checks: hardcoded localhost / staging URLs
        findings.addAll(scanHardcodedStagingUrls(file, content))

        return findings
    }

    private fun scanHtmlLikeFile(file: File, content: String): List<SeoFinding> {
        val findings = mutableListOf<SeoFinding>()

        // Only parse with full audit engine if it resembles a full document (<html or <head or <body)
        if (content.contains("<html", ignoreCase = true) || content.contains("<head", ignoreCase = true)) {
            val engineFindings = auditEngine.auditHtml("file://${file.name}", content)
            for (f in engineFindings) {
                val line = findLineForFinding(f, content)
                findings.add(
                    f.copy(
                        category = AuditCategory.LOCAL_SOURCE,
                        affectedUrlOrFile = file.absolutePath,
                        location = SourceLocation(filePath = file.absolutePath, line = line)
                    )
                )
            }
        } else {
            // Partial template / component checks
            // Missing alt tags in partials
            val doc = Jsoup.parseBodyFragment(content)
            for (img in doc.select("img")) {
                if (!img.hasAttr("alt")) {
                    findings.add(
                        SeoFinding(
                            id = UUID.randomUUID().toString(),
                            category = AuditCategory.LOCAL_SOURCE,
                            ruleId = "SRC-IMG-ALT",
                            severity = Severity.MEDIUM,
                            title = "Image Element Missing Alt Attribute in Template",
                            explanation = "Image tag in template lacks an 'alt' attribute.",
                            affectedUrlOrFile = file.absolutePath,
                            location = SourceLocation(filePath = file.absolutePath, line = findTextLine(content, img.outerHtml().take(30))),
                            evidence = img.outerHtml().take(60),
                            expected = "<img alt=\"...\" ... />",
                            recommendation = "Provide an alt attribute with dynamic binding or descriptive placeholder.",
                            hasAutomatedFix = true,
                            quickFixId = "ADD_ALT"
                        )
                    )
                }
            }
        }

        return findings
    }

    private fun scanReactTemplates(file: File, content: String): List<SeoFinding> {
        val findings = mutableListOf<SeoFinding>()
        // Check for <img> without alt in JSX
        val imgRegex = Regex("<img\\s+([^>]*?)>", RegexOption.IGNORE_CASE)
        for (match in imgRegex.findAll(content)) {
            val attrs = match.groupValues[1]
            if (!attrs.contains("alt=", ignoreCase = true)) {
                val line = content.substring(0, match.range.first).lines().size
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.LOCAL_SOURCE,
                        ruleId = "SRC-JSX-ALT",
                        severity = Severity.MEDIUM,
                        title = "JSX Image Missing 'alt' Prop",
                        explanation = "JSX element <img /> rendered without an 'alt' prop.",
                        affectedUrlOrFile = file.absolutePath,
                        location = SourceLocation(filePath = file.absolutePath, line = line),
                        evidence = match.value.take(60),
                        expected = "<img alt=\"...\" ... />",
                        recommendation = "Add alt prop or alt=\"\" for decorative icons.",
                        hasAutomatedFix = true,
                        quickFixId = "ADD_ALT"
                    )
                )
            }
        }
        return findings
    }

    private fun scanRobotsTxt(file: File, content: String): List<SeoFinding> {
        val findings = mutableListOf<SeoFinding>()
        if (content.contains("Disallow: /", ignoreCase = true) && !content.contains("Disallow: /*?", ignoreCase = true)) {
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.TECHNICAL,
                    ruleId = "ROBOTS-DISALLOW-ALL",
                    severity = Severity.HIGH,
                    title = "Robots.txt Blocks Entire Site (Disallow: /)",
                    explanation = "robots.txt contains 'Disallow: /', instructing search bots not to crawl any content.",
                    affectedUrlOrFile = file.absolutePath,
                    location = SourceLocation(filePath = file.absolutePath, line = findTextLine(content, "Disallow: /")),
                    evidence = "Disallow: /",
                    expected = "Targeted disallow rules for admin or private routes.",
                    recommendation = "Ensure site-wide blocking is intended only for non-production environments."
                )
            )
        }
        if (!content.contains("Sitemap:", ignoreCase = true)) {
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.TECHNICAL,
                    ruleId = "ROBOTS-MISSING-SITEMAP",
                    severity = Severity.INFORMATIONAL,
                    title = "Robots.txt Lacks Sitemap Declaration",
                    explanation = "Specifying 'Sitemap: https://yourdomain.com/sitemap.xml' in robots.txt facilitates search engine discovery.",
                    affectedUrlOrFile = file.absolutePath,
                    location = SourceLocation(filePath = file.absolutePath, line = 1),
                    evidence = "Sitemap declaration: <none>",
                    expected = "Sitemap: https://yourdomain.com/sitemap.xml",
                    recommendation = "Add a Sitemap directive pointing to your primary XML sitemap."
                )
            )
        }
        return findings
    }

    private fun scanSitemapXml(file: File, content: String): List<SeoFinding> {
        val findings = mutableListOf<SeoFinding>()
        if (!content.contains("<urlset", ignoreCase = true) && !content.contains("<sitemapindex", ignoreCase = true)) {
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.TECHNICAL,
                    ruleId = "SITEMAP-INVALID-ROOT",
                    severity = Severity.HIGH,
                    title = "Invalid XML Sitemap Schema",
                    explanation = "Sitemap file does not contain a valid <urlset> or <sitemapindex> root element.",
                    affectedUrlOrFile = file.absolutePath,
                    location = SourceLocation(filePath = file.absolutePath, line = 1),
                    evidence = file.name,
                    expected = "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">",
                    recommendation = "Verify the root XML namespace following sitemaps.org specification."
                )
            )
        }
        return findings
    }

    private fun scanHardcodedStagingUrls(file: File, content: String): List<SeoFinding> {
        val findings = mutableListOf<SeoFinding>()
        val stagingRegex = Regex("(http://localhost:[0-9]+|https?://[a-zA-Z0-9.-]+\\.(test|local|staging|dev))")
        for (match in stagingRegex.findAll(content)) {
            val line = content.substring(0, match.range.first).lines().size
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.SECURITY,
                    ruleId = "SRC-HARDCODED-STAGING",
                    severity = Severity.MEDIUM,
                    title = "Hardcoded Staging / Localhost Reference",
                    explanation = "Found reference to local or staging URL: '${match.value}'. Accidental publication can lead to broken links or staging environment indexing.",
                    affectedUrlOrFile = file.absolutePath,
                    location = SourceLocation(filePath = file.absolutePath, line = line),
                    evidence = match.value,
                    expected = "Environment-configurable base URLs or production domains.",
                    recommendation = "Use environment variables or relative paths instead of hardcoded dev hosts."
                )
            )
        }
        return findings
    }

    private fun findLineForFinding(finding: SeoFinding, content: String): Int {
        return when (finding.ruleId) {
            "ONPAGE-TITLE" -> findTextLine(content, "<title")
            "ONPAGE-META-DESC" -> findTextLine(content, "name=\"description\"")
            "ONPAGE-CANONICAL" -> findTextLine(content, "rel=\"canonical\"")
            "ONPAGE-HEADINGS" -> findTextLine(content, "<h1")
            else -> 1
        }
    }

    private fun findTextLine(content: String, target: String): Int {
        val idx = content.indexOf(target, ignoreCase = true)
        return if (idx >= 0) {
            content.substring(0, idx).lines().size
        } else {
            1
        }
    }
}
