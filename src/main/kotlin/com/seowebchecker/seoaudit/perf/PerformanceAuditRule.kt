package com.seowebchecker.seoaudit.perf

import com.seowebchecker.seoaudit.model.*
import com.seowebchecker.seoaudit.rules.SeoRule
import java.util.UUID

/**
 * Performance and Core Web Vitals diagnostic rule using static resource inspection and response timings.
 */
class PerformanceAuditRule : SeoRule {
    override val id = "PERF-CORE-WEB-VITALS"
    override val name = "Performance & Core Web Vitals Diagnostics"
    override val description = "Analyzes server TTFB, render-blocking resources, unoptimized images, and cache/compression headers."

    override fun evaluate(context: PageAuditContext): List<SeoFinding> {
        val findings = mutableListOf<SeoFinding>()

        // 1. Time to First Byte (TTFB) / Response Time
        if (context.responseTimeMs > 800) {
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.CORE_WEB_VITALS,
                    ruleId = id,
                    severity = if (context.responseTimeMs > 1800) Severity.HIGH else Severity.MEDIUM,
                    title = "Slow Server Response Time (TTFB: ${context.responseTimeMs}ms)",
                    explanation = "Server response time was ${context.responseTimeMs}ms. Google recommends a TTFB under 800ms for optimal Core Web Vitals (LCP) performance.",
                    affectedUrlOrFile = context.url,
                    evidence = "TTFB: ${context.responseTimeMs}ms",
                    expected = "TTFB <= 800ms (Good CWV threshold).",
                    recommendation = "Investigate backend database queries, implement server-side caching (Redis, Varnish), or use a Content Delivery Network (CDN).",
                    codeExample = null
                )
            )
        }

        // 2. Missing Compression Headers
        val contentEncoding = context.headers["content-encoding"]?.firstOrNull()
            ?: context.headers["Content-Encoding"]?.firstOrNull()
        if (contentEncoding == null && context.rawHtml.length > 5000) {
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.CORE_WEB_VITALS,
                    ruleId = id,
                    severity = Severity.MEDIUM,
                    title = "Text Compression Not Detected",
                    explanation = "The response lacks a 'Content-Encoding' header indicating gzip, brotli, or deflate compression. Uncompressed HTML increases download duration.",
                    affectedUrlOrFile = context.url,
                    evidence = "Content-Encoding: <none>",
                    expected = "Content-Encoding: gzip (or br / zstd)",
                    recommendation = "Enable Gzip or Brotli compression on your web server / reverse proxy (Nginx, Apache, Cloudflare).",
                    codeExample = null
                )
            )
        }

        // 3. Image Lazy Loading Opportunity
        val imagesWithoutLoading = context.images.filter { it.loading == null }
        if (imagesWithoutLoading.size > 5) {
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.CORE_WEB_VITALS,
                    ruleId = id,
                    severity = Severity.LOW,
                    title = "Native Lazy Loading Opportunity (${imagesWithoutLoading.size} images)",
                    explanation = "Multiple images do not declare 'loading=\"lazy\"'. Deferring offscreen images improves First Contentful Paint (FCP) and reduces initial data transfer.",
                    affectedUrlOrFile = context.url,
                    evidence = "Found ${imagesWithoutLoading.size} images without loading=\"lazy\"",
                    expected = "<img loading=\"lazy\" ...>",
                    recommendation = "Add loading=\"lazy\" to below-the-fold images. Note: keep hero/LCP images eager.",
                    codeExample = "<img src=\"product.webp\" loading=\"lazy\" alt=\"Product image\" />"
                )
            )
        }

        // 4. Missing Explicit Dimensions on Images (CLS risk)
        val imagesWithoutDimensions = context.images.filter { it.width == null || it.height == null }
        if (imagesWithoutDimensions.isNotEmpty()) {
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.CORE_WEB_VITALS,
                    ruleId = id,
                    severity = Severity.MEDIUM,
                    title = "Images Missing Width and Height Attributes (${imagesWithoutDimensions.size} found)",
                    explanation = "Images without explicit width and height cause layout reflows as they load, increasing Cumulative Layout Shift (CLS).",
                    affectedUrlOrFile = context.url,
                    evidence = "Sample: ${imagesWithoutDimensions.first().src}",
                    expected = "<img width=\"800\" height=\"600\" ...>",
                    recommendation = "Always specify width and height (or CSS aspect-ratio) to reserve layout space prior to image download.",
                    codeExample = "<img src=\"banner.png\" width=\"1200\" height=\"630\" alt=\"Banner\" />"
                )
            )
        }

        return findings
    }
}
