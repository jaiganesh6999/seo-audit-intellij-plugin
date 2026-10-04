package com.seowebchecker.seoaudit.rules

import com.seowebchecker.seoaudit.model.*
import java.util.UUID

/**
 * Technical SEO rules for HTTP status codes, redirects, HTTPS, URL normalization, and indexing signals.
 */
object TechnicalRules {

    class HttpStatusCodeRule : SeoRule {
        override val id = "TECH-HTTP-STATUS"
        override val name = "HTTP Status Code and Redirect Chains"
        override val description = "Checks for 4xx client errors, 5xx server errors, and excessive redirect chains."

        override fun evaluate(context: PageAuditContext): List<SeoFinding> {
            val findings = mutableListOf<SeoFinding>()

            when {
                context.statusCode in 400..499 -> {
                    findings.add(
                        SeoFinding(
                            id = UUID.randomUUID().toString(),
                            category = AuditCategory.TECHNICAL,
                            ruleId = id,
                            severity = Severity.CRITICAL,
                            title = "Client Error HTTP ${context.statusCode}",
                            explanation = "The requested URL returned a client error HTTP ${context.statusCode}. Search engines cannot index broken pages, and users encounter dead ends.",
                            affectedUrlOrFile = context.url,
                            evidence = "Status code: ${context.statusCode}",
                            expected = "HTTP 200 OK or appropriate permanent redirect (301).",
                            recommendation = "Fix the broken link or route, or restore the missing resource.",
                            codeExample = null
                        )
                    )
                }
                context.statusCode in 500..599 -> {
                    findings.add(
                        SeoFinding(
                            id = UUID.randomUUID().toString(),
                            category = AuditCategory.TECHNICAL,
                            ruleId = id,
                            severity = Severity.CRITICAL,
                            title = "Server Error HTTP ${context.statusCode}",
                            explanation = "The server returned HTTP ${context.statusCode}. Server errors halt crawling and degrade search visibility.",
                            affectedUrlOrFile = context.url,
                            evidence = "Status code: ${context.statusCode}",
                            expected = "HTTP 200 OK",
                            recommendation = "Check application server logs and database connections to resolve 5xx failures.",
                            codeExample = null
                        )
                    )
                }
                context.redirectChain.size > 2 -> {
                    findings.add(
                        SeoFinding(
                            id = UUID.randomUUID().toString(),
                            category = AuditCategory.TECHNICAL,
                            ruleId = id,
                            severity = Severity.HIGH,
                            title = "Excessive Redirect Chain (${context.redirectChain.size} hops)",
                            explanation = "URL went through ${context.redirectChain.size} redirects before reaching destination. Redirect chains waste crawl budget and slow down page delivery.",
                            affectedUrlOrFile = context.url,
                            evidence = "Chain: ${context.redirectChain.joinToString(" -> ")}",
                            expected = "Direct link or at most 1 redirect hop.",
                            recommendation = "Update internal links to target the final destination directly (${context.redirectDestination ?: "destination"}).",
                            codeExample = null
                        )
                    )
                }
            }

            return findings
        }
    }

    class HttpsAndSecurityRule : SeoRule {
        override val id = "TECH-HTTPS-SECURITY"
        override val name = "HTTPS Usage and Mixed Content"
        override val description = "Checks whether page is served securely over HTTPS and flags insecure mixed content."

        override fun evaluate(context: PageAuditContext): List<SeoFinding> {
            val findings = mutableListOf<SeoFinding>()

            if (context.url.startsWith("http://")) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.SECURITY,
                        ruleId = id,
                        severity = Severity.CRITICAL,
                        title = "Insecure HTTP Protocol",
                        explanation = "The page is served over plain HTTP rather than HTTPS. HTTPS is a mandatory baseline for user security, modern web features, and search ranking signals.",
                        affectedUrlOrFile = context.url,
                        evidence = "URL: ${context.url}",
                        expected = "HTTPS protocol: https://${context.url.removePrefix("http://")}",
                        recommendation = "Configure an SSL/TLS certificate and implement a 301 redirect from HTTP to HTTPS.",
                        codeExample = null
                    )
                )
            }

            // Check for insecure mixed content resources
            val insecureImages = context.images.filter { it.src.startsWith("http://") }
            if (insecureImages.isNotEmpty() && context.url.startsWith("https://")) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.SECURITY,
                        ruleId = id,
                        severity = Severity.HIGH,
                        title = "Mixed Content (Insecure Images Detected)",
                        explanation = "Page served over HTTPS loads ${insecureImages.size} insecure image(s) via HTTP. Browsers may block mixed content or show security warnings.",
                        affectedUrlOrFile = context.url,
                        evidence = "Insecure URLs: ${insecureImages.take(2).joinToString { it.src }}",
                        expected = "All assets loaded over HTTPS or protocol-relative paths.",
                        recommendation = "Update asset URLs to use https://.",
                        codeExample = "<img src=\"https://example.com/image.jpg\" />"
                    )
                )
            }

            return findings
        }
    }

    class CanonicalConsistencyRule : SeoRule {
        override val id = "TECH-CANONICAL-CONSISTENCY"
        override val name = "Canonical Consistency and Normalization"
        override val description = "Validates that canonical destination matches requested URL or points to clean normalized variant."

        override fun evaluate(context: PageAuditContext): List<SeoFinding> {
            val findings = mutableListOf<SeoFinding>()
            val canonical = context.canonicalUrl ?: return findings

            // Check if page canonical points to a different URL (non-self-canonical)
            val normalizedCurrent = context.url.trimEnd('/')
            val normalizedCanon = canonical.trimEnd('/')

            if (normalizedCurrent != normalizedCanon && !context.isRedirect) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.TECHNICAL,
                        ruleId = id,
                        severity = Severity.INFORMATIONAL,
                        title = "Page Canonicalizes to a Different URL",
                        explanation = "This URL canonicalizes to '$canonical'. Search engines will treat the canonical target as the authoritative version for indexing.",
                        affectedUrlOrFile = context.url,
                        evidence = "Current: ${context.url} -> Canonical: $canonical",
                        expected = "Self-referencing canonical unless this page is an intentional duplicate or variant.",
                        recommendation = "Verify if this page should be folded into the canonical target.",
                        codeExample = null
                    )
                )
            }

            return findings
        }
    }
}
