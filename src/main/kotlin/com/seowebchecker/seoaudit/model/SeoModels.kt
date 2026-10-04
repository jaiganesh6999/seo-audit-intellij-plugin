package com.seowebchecker.seoaudit.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Finding severity classification.
 */
enum class Severity(val displayName: String, val weight: Int) {
    CRITICAL("Critical", 15),
    HIGH("High", 10),
    MEDIUM("Medium", 5),
    LOW("Low", 2),
    INFORMATIONAL("Informational", 0);

    companion object {
        fun fromString(name: String): Severity {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: INFORMATIONAL
        }
    }
}

/**
 * Audit categories representing various technical SEO areas.
 */
enum class AuditCategory(val displayName: String) {
    ON_PAGE("On-Page SEO"),
    TECHNICAL("Technical SEO"),
    STRUCTURED_DATA("Structured Data & Schema"),
    CORE_WEB_VITALS("Performance & Core Web Vitals"),
    CONTENT_QUALITY("Content & Keywords"),
    LINK_ANALYSIS("Internal & External Links"),
    SECURITY("Security & Hygiene"),
    LOCAL_SOURCE("IDE Source Template");

    companion object {
        fun fromString(name: String): AuditCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: TECHNICAL
        }
    }
}

/**
 * Source coordinate pointing to an exact file location in an open project.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SourceLocation(
    val filePath: String,
    val line: Int = 1,
    val column: Int = 1,
    val endLine: Int? = null,
    val endColumn: Int? = null
)

/**
 * Represents an individual actionable SEO finding.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SeoFinding(
    val id: String,
    val category: AuditCategory,
    val ruleId: String,
    val severity: Severity,
    val title: String,
    val explanation: String,
    val affectedUrlOrFile: String,
    val location: SourceLocation? = null,
    val evidence: String,
    val expected: String,
    val recommendation: String,
    val codeExample: String? = null,
    val confidence: String = "High",
    val hasAutomatedFix: Boolean = false,
    val docUrl: String = "https://seowebchecker.com/",
    val quickFixId: String? = null
)

/**
 * Parsed HTML page context with metadata, links, headings, and schema for rules evaluation.
 */
data class PageAuditContext(
    val url: String,
    val statusCode: Int = 200,
    val headers: Map<String, List<String>> = emptyMap(),
    val rawHtml: String = "",
    val title: String? = null,
    val metaDescription: String? = null,
    val canonicalUrl: String? = null,
    val canonicalTags: List<String> = emptyList(),
    val robotsMeta: List<String> = emptyList(),
    val xRobotsTag: List<String> = emptyList(),
    val viewport: String? = null,
    val htmlLang: String? = null,
    val hreflangs: List<Pair<String, String>> = emptyList(),
    val h1Headings: List<String> = emptyList(),
    val h2Headings: List<String> = emptyList(),
    val h3Headings: List<String> = emptyList(),
    val images: List<ImageInfo> = emptyList(),
    val internalLinks: List<LinkInfo> = emptyList(),
    val externalLinks: List<LinkInfo> = emptyList(),
    val jsonLdBlocks: List<String> = emptyList(),
    val openGraphTags: Map<String, String> = emptyMap(),
    val twitterTags: Map<String, String> = emptyMap(),
    val textContent: String = "",
    val wordCount: Int = 0,
    val responseTimeMs: Long = 0,
    val isRedirect: Boolean = false,
    val redirectChain: List<String> = emptyList(),
    val redirectDestination: String? = null,
    val isSoft404Candidate: Boolean = false,
    val contentType: String? = null,
    val charset: String? = null
)

data class ImageInfo(
    val src: String,
    val alt: String?,
    val isDecorative: Boolean = false,
    val loading: String? = null,
    val width: String? = null,
    val height: String? = null
)

data class LinkInfo(
    val href: String,
    val anchorText: String,
    val rel: String? = null,
    val target: String? = null,
    val isNofollow: Boolean = false
)

/**
 * Audit result summary and score for a page or an entire project.
 */
data class AuditSummary(
    val totalFindings: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val infoCount: Int,
    val healthScore: Int,
    val categoryScores: Map<AuditCategory, Int> = emptyMap()
)
