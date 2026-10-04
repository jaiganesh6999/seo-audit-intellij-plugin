package com.seowebchecker.seoaudit

import com.seowebchecker.seoaudit.content.ContentQualityRule
import com.seowebchecker.seoaudit.content.LinkHygieneRule
import com.seowebchecker.seoaudit.model.AuditCategory
import com.seowebchecker.seoaudit.model.PageAuditContext
import com.seowebchecker.seoaudit.model.SeoScoringCalculator
import com.seowebchecker.seoaudit.model.Severity
import com.seowebchecker.seoaudit.perf.PerformanceAuditRule
import com.seowebchecker.seoaudit.rules.OnPageRules
import com.seowebchecker.seoaudit.rules.SeoAuditEngine
import com.seowebchecker.seoaudit.rules.TechnicalRules
import com.seowebchecker.seoaudit.schema.StructuredDataValidator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SeoAuditEngineTest {

    private val engine = SeoAuditEngine()

    @Test
    fun `test valid page gets high health score and zero critical findings`() {
        val validHtml = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>SEOWebChecker: The Premier Technical SEO Audit Tool</title>
                <meta name="description" content="Audit on-page SEO, technical crawlability, structured data schemas, and Core Web Vitals directly from your desktop or IDE.">
                <link rel="canonical" href="https://seowebchecker.com/" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <script type="application/ld+json">
                {
                    "@context": "https://schema.org",
                    "@type": "WebSite",
                    "name": "SEOWebChecker",
                    "url": "https://seowebchecker.com/"
                }
                </script>
            </head>
            <body>
                <h1>Advanced SEO Audit Engine</h1>
                <h2>On-Page Verification</h2>
                <p>${"High quality comprehensive technical SEO guidance and optimization for web developers. ".repeat(60)}</p>
                <img src="https://seowebchecker.com/logo.png" alt="SEOWebChecker Official Logo" width="200" height="60" loading="lazy">
                <a href="https://seowebchecker.com/docs">Read Documentation</a>
                <a href="https://github.com/seowebchecker" target="_blank" rel="noopener noreferrer">GitHub</a>
            </body>
            </html>
        """.trimIndent()

        val findings = engine.auditHtml("https://seowebchecker.com/", validHtml, 200, 250, mapOf("content-encoding" to listOf("gzip")))
        val criticalFindings = findings.filter { it.severity == Severity.CRITICAL }

        assertTrue(criticalFindings.isEmpty(), "Valid page should have no critical findings: $criticalFindings")
        val summary = SeoScoringCalculator.buildSummary(findings)
        assertTrue(summary.healthScore >= 90, "Valid page should score >= 90, got ${summary.healthScore}. Findings: $findings")
    }

    @Test
    fun `test invalid page triggers on-page, technical, and schema findings`() {
        val brokenHtml = """
            <html>
            <head>
                <!-- Missing title, missing meta desc, missing canonical, missing viewport -->
                <meta name="robots" content="index, noindex">
                <script type="application/ld+json">
                {
                    "broken": json
                }
                </script>
            </head>
            <body>
                <!-- Missing H1 -->
                <img src="http://insecure.com/bad.png">
                <a href="/empty"></a>
            </body>
            </html>
        """.trimIndent()

        val findings = engine.auditHtml("http://example.com/test", brokenHtml, 500, 2500)

        // Check for specific triggered rules
        assertTrue(findings.any { it.ruleId == "ONPAGE-TITLE" && it.severity == Severity.CRITICAL }, "Missing Title finding not found. Findings: $findings")
        assertTrue(findings.any { it.ruleId == "ONPAGE-META-DESC" && it.severity == Severity.HIGH }, "Missing Meta Desc finding not found. Findings: $findings")
        assertTrue(findings.any { it.ruleId == "ONPAGE-CANONICAL" && it.severity == Severity.MEDIUM }, "Missing Canonical finding not found. Findings: $findings")
        assertTrue(findings.any { it.ruleId == "ONPAGE-ROBOTS" && it.severity == Severity.CRITICAL }, "Robots conflict finding not found. Findings: $findings")
        assertTrue(findings.any { it.ruleId == "ONPAGE-HEADINGS" && it.severity == Severity.HIGH }, "Missing H1 finding not found. Findings: $findings")
        assertTrue(findings.any { it.ruleId == "TECH-HTTP-STATUS" && it.severity == Severity.CRITICAL }, "HTTP Status finding not found. Findings: $findings")
        assertTrue(findings.any { it.ruleId == "TECH-HTTPS-SECURITY" && it.severity == Severity.CRITICAL }, "HTTPS Security finding not found. Findings: $findings")
        assertTrue(findings.any { it.ruleId == "SCHEMA-JSONLD" && it.severity == Severity.CRITICAL }, "Schema JSON-LD finding not found. Findings: $findings")
        assertTrue(findings.any { it.ruleId == "PERF-CORE-WEB-VITALS" }, "Perf finding not found. Findings: $findings")
        assertTrue(findings.any { it.ruleId == "CONTENT-QUALITY" }, "Content quality finding not found. Findings: $findings")

        val summary = SeoScoringCalculator.buildSummary(findings)
        assertTrue(summary.healthScore <= 30, "Severely broken page should have low health score, got ${summary.healthScore}")
    }

    @Test
    fun `test structured data validation for Article and Product schemas`() {
        val validator = StructuredDataValidator()

        val articleJson = """
            {
                "@context": "https://schema.org",
                "@type": "Article",
                "headline": "Modern SEO Practices",
                "author": { "@type": "Person", "name": "Jane Doe" }
            }
        """.trimIndent()

        val context = PageAuditContext(
            url = "https://seowebchecker.com/blog/article",
            jsonLdBlocks = listOf(articleJson)
        )

        val findings = validator.evaluate(context)
        assertTrue(findings.any { it.title.contains("Missing 'datePublished'") })
    }

    @Test
    fun `test keyword density helper`() {
        val text = "Technical SEO is essential for organic growth. Good SEO optimizes search visibility and SEO audit scores."
        val count = ContentQualityRule.analyzeKeywordOccurrences(text, "seo")
        assertEquals(3, count)
    }
}
