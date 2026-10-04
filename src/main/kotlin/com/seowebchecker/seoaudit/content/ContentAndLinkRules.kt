package com.seowebchecker.seoaudit.content

import com.seowebchecker.seoaudit.model.*
import com.seowebchecker.seoaudit.rules.SeoRule
import java.util.UUID

/**
 * Content Quality, Word Count, and Keyword Analysis rule.
 */
class ContentQualityRule(
    private val minWordCount: Int = 300
) : SeoRule {

    override val id = "CONTENT-QUALITY"
    override val name = "Content Depth & Keyword Analysis"
    override val description = "Evaluates page text volume, thin content risks, and keyword coverage."

    override fun evaluate(context: PageAuditContext): List<SeoFinding> {
        val findings = mutableListOf<SeoFinding>()

        // 1. Thin Content Check
        if (context.wordCount < minWordCount && !context.isRedirect) {
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.CONTENT_QUALITY,
                    ruleId = id,
                    severity = Severity.MEDIUM,
                    title = "Thin Content Warning (${context.wordCount} words)",
                    explanation = "The page contains only ${context.wordCount} words of readable body text. Pages with low text volume may be flagged as low-quality or thin content by search algorithms.",
                    affectedUrlOrFile = context.url,
                    evidence = "Word count: ${context.wordCount}",
                    expected = "Substantive editorial or product content (>= $minWordCount words).",
                    recommendation = "Enrich the page with helpful, relevant copy, FAQs, guides, or specifications.",
                    codeExample = null
                )
            )
        }

        return findings
    }

    companion object {
        fun analyzeKeywordOccurrences(text: String, keyword: String): Int {
            if (keyword.isBlank() || text.isBlank()) return 0
            val regex = Regex("\\b${Regex.escape(keyword.trim().lowercase())}\\b")
            return regex.findAll(text.lowercase()).count()
        }
    }
}

/**
 * Internal and external link hygiene rule.
 */
class LinkHygieneRule : SeoRule {
    override val id = "LINKS-HYGIENE"
    override val name = "Link Structure & Anchor Text Hygiene"
    override val description = "Checks for empty anchors, suspicious links, and unlinked content."

    override fun evaluate(context: PageAuditContext): List<SeoFinding> {
        val findings = mutableListOf<SeoFinding>()

        // Check for empty or generic anchor text
        val emptyAnchors = context.internalLinks.filter { it.anchorText.trim().isEmpty() }
        if (emptyAnchors.isNotEmpty()) {
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.LINK_ANALYSIS,
                    ruleId = id,
                    severity = Severity.LOW,
                    title = "Empty Anchor Text Detected (${emptyAnchors.size} links)",
                    explanation = "Found links with no anchor text. Descriptive anchor text provides critical contextual relevance to search bots and accessibility screen readers.",
                    affectedUrlOrFile = context.url,
                    evidence = "Sample empty target: ${emptyAnchors.first().href}",
                    expected = "Descriptive anchor text describing the link destination.",
                    recommendation = "Provide descriptive text or an accessible aria-label on the link.",
                    codeExample = "<a href=\"/docs\">Read API Documentation</a>"
                )
            )
        }

        // Outbound links with target="_blank" missing rel="noopener" or "noreferrer"
        val unsafeBlanks = context.externalLinks.filter {
            it.target == "_blank" && (it.rel == null || (!it.rel.contains("noopener") && !it.rel.contains("noreferrer")))
        }
        if (unsafeBlanks.isNotEmpty()) {
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.SECURITY,
                    ruleId = id,
                    severity = Severity.LOW,
                    title = "External Links with target=\"_blank\" Missing rel=\"noopener\"",
                    explanation = "External links opening in a new tab without 'noopener' can expose your page to tabnabbing security risks and performance overhead.",
                    affectedUrlOrFile = context.url,
                    evidence = "Count: ${unsafeBlanks.size}",
                    expected = "<a href=\"...\" target=\"_blank\" rel=\"noopener noreferrer\">",
                    recommendation = "Add rel=\"noopener noreferrer\" to all external target=\"_blank\" links.",
                    codeExample = "<a href=\"https://seowebchecker.com/\" target=\"_blank\" rel=\"noopener noreferrer\">SEOWebChecker</a>",
                    hasAutomatedFix = true,
                    quickFixId = "ADD_REL_NOOPENER"
                )
            )
        }

        return findings
    }
}
