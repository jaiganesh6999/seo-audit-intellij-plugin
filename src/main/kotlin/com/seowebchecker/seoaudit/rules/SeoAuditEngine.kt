package com.seowebchecker.seoaudit.rules

import com.seowebchecker.seoaudit.content.ContentQualityRule
import com.seowebchecker.seoaudit.content.LinkHygieneRule
import com.seowebchecker.seoaudit.model.*
import com.seowebchecker.seoaudit.perf.PerformanceAuditRule
import com.seowebchecker.seoaudit.schema.StructuredDataValidator
import org.jsoup.Jsoup
import java.net.URI

/**
 * Unified SEO Audit Engine that orchestrates all On-Page, Technical, Structured Data,
 * Performance, Content, and Link rules.
 */
class SeoAuditEngine(
    private val rules: List<SeoRule> = defaultRules()
) {

    fun auditHtml(url: String, html: String, statusCode: Int = 200, responseTimeMs: Long = 0, headers: Map<String, List<String>> = emptyMap()): List<SeoFinding> {
        val context = parseContext(url, html, statusCode, responseTimeMs, headers)
        return auditContext(context)
    }

    fun auditContext(context: PageAuditContext): List<SeoFinding> {
        return rules.flatMap { rule ->
            try {
                rule.evaluate(context)
            } catch (e: Exception) {
                listOf(
                    SeoFinding(
                        id = "RULE-ERR-${rule.id}",
                        category = AuditCategory.TECHNICAL,
                        ruleId = rule.id,
                        severity = Severity.LOW,
                        title = "Rule Evaluation Notice (${rule.name})",
                        explanation = "Exception occurred during rule execution: ${e.message}",
                        affectedUrlOrFile = context.url,
                        evidence = e.localizedMessage ?: "Exception",
                        expected = "Clean execution",
                        recommendation = "Review page structure or report parser edge-case.",
                        confidence = "Low"
                    )
                )
            }
        }
    }

    fun parseContext(
        url: String,
        html: String,
        statusCode: Int = 200,
        responseTimeMs: Long = 0,
        headers: Map<String, List<String>> = emptyMap()
    ): PageAuditContext {
        val doc = Jsoup.parse(html, url)

        val title = doc.select("head > title").firstOrNull()?.text()
            ?: doc.select("title").firstOrNull()?.text()

        val metaDescription = doc.select("meta").toList()
            .firstOrNull {
                val name = it.attr("name")
                val property = it.attr("property")
                (name.equals("description", ignoreCase = true) || property.equals("og:description", ignoreCase = true)) && it.hasAttr("content")
            }?.attr("content")

        val canonicalElements = doc.select("link[rel]").toList()
            .filter { it.attr("rel").equals("canonical", ignoreCase = true) }
        val canonicalTags = canonicalElements.map { it.attr("href").trim() }
        val canonicalUrl = canonicalTags.firstOrNull()

        val robotsMeta = doc.select("meta[name]").toList()
            .filter { val name = it.attr("name"); name.equals("robots", ignoreCase = true) || name.equals("googlebot", ignoreCase = true) }
            .mapNotNull { if (it.hasAttr("content")) it.attr("content") else null }

        val xRobotsTag = headers["x-robots-tag"] ?: headers["X-Robots-Tag"] ?: emptyList()

        val viewport = doc.select("meta[name]").toList()
            .firstOrNull { it.attr("name").equals("viewport", ignoreCase = true) }
            ?.attr("content")

        val htmlLang = doc.select("html").firstOrNull()?.attr("lang")

        val hreflangs = doc.select("link[rel]").toList()
            .filter { it.attr("rel").equals("alternate", ignoreCase = true) && it.hasAttr("hreflang") }
            .map { it.attr("hreflang") to it.attr("href") }

        val h1Headings = doc.select("h1").map { it.text().trim() }.filter { it.isNotEmpty() }
        val h2Headings = doc.select("h2").map { it.text().trim() }.filter { it.isNotEmpty() }
        val h3Headings = doc.select("h3").map { it.text().trim() }.filter { it.isNotEmpty() }

        val images = doc.select("img").map { img ->
            val src = img.attr("abs:src").ifEmpty { img.attr("src") }
            val alt = if (img.hasAttr("alt")) img.attr("alt") else null
            val role = img.attr("role")
            val isDecorative = role.equals("presentation", ignoreCase = true) || role.equals("none", ignoreCase = true)
            val loading = img.attr("loading").ifEmpty { null }
            val width = img.attr("width").ifEmpty { null }
            val height = img.attr("height").ifEmpty { null }
            ImageInfo(src, alt, isDecorative, loading, width, height)
        }

        // Links
        val baseHost = try { URI(url).host } catch (e: Exception) { null }
        val internalLinks = mutableListOf<LinkInfo>()
        val externalLinks = mutableListOf<LinkInfo>()

        for (link in doc.select("a[href]")) {
            val href = link.attr("abs:href").ifEmpty { link.attr("href") }
            val anchorText = link.text().trim()
            val rel = link.attr("rel").ifEmpty { null }
            val target = link.attr("target").ifEmpty { null }
            val isNofollow = rel?.lowercase()?.contains("nofollow") == true

            val linkInfo = LinkInfo(href, anchorText, rel, target, isNofollow)
            val linkHost = try { URI(href).host } catch (e: Exception) { null }

            if (linkHost == null || baseHost == null || linkHost.equals(baseHost, ignoreCase = true)) {
                internalLinks.add(linkInfo)
            } else {
                externalLinks.add(linkInfo)
            }
        }

        // JSON-LD blocks
        val jsonLdBlocks = doc.select("script[type]").toList()
            .filter { it.attr("type").equals("application/ld+json", ignoreCase = true) }
            .map { (it.data().ifEmpty { it.html() }).trim() }

        // Open Graph
        val ogMap = mutableMapOf<String, String>()
        for (og in doc.select("meta[property^=og:]")) {
            ogMap[og.attr("property")] = og.attr("content")
        }

        // Twitter
        val twitterMap = mutableMapOf<String, String>()
        for (tw in doc.select("meta[name^=twitter:], meta[property^=twitter:]")) {
            val key = tw.attr("name").ifEmpty { tw.attr("property") }
            twitterMap[key] = tw.attr("content")
        }

        val bodyText = doc.body()?.text() ?: ""
        val wordCount = bodyText.split(Regex("\\s+")).count { it.isNotBlank() }

        return PageAuditContext(
            url = url,
            statusCode = statusCode,
            headers = headers,
            rawHtml = html,
            title = title,
            metaDescription = metaDescription,
            canonicalUrl = canonicalUrl,
            canonicalTags = canonicalTags,
            robotsMeta = robotsMeta,
            xRobotsTag = xRobotsTag,
            viewport = viewport,
            htmlLang = htmlLang,
            hreflangs = hreflangs,
            h1Headings = h1Headings,
            h2Headings = h2Headings,
            h3Headings = h3Headings,
            images = images,
            internalLinks = internalLinks,
            externalLinks = externalLinks,
            jsonLdBlocks = jsonLdBlocks,
            openGraphTags = ogMap,
            twitterTags = twitterMap,
            textContent = bodyText,
            wordCount = wordCount,
            responseTimeMs = responseTimeMs
        )
    }

    companion object {
        fun defaultRules(): List<SeoRule> {
            return listOf(
                OnPageRules.TitleTagRule(),
                OnPageRules.MetaDescriptionRule(),
                OnPageRules.CanonicalTagRule(),
                OnPageRules.RobotsDirectivesRule(),
                OnPageRules.LanguageAndViewportRule(),
                OnPageRules.HeadingHierarchyRule(),
                OnPageRules.ImageAltTextRule(),
                TechnicalRules.HttpStatusCodeRule(),
                TechnicalRules.HttpsAndSecurityRule(),
                TechnicalRules.CanonicalConsistencyRule(),
                StructuredDataValidator(),
                PerformanceAuditRule(),
                ContentQualityRule(),
                LinkHygieneRule()
            )
        }
    }
}
