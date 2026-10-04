package com.seowebchecker.seoaudit.rules

import com.seowebchecker.seoaudit.model.*
import java.util.UUID

/**
 * On-Page SEO rules for Title, Meta Description, Canonical, Robots, Language, Viewport, Open Graph, etc.
 */
object OnPageRules {

    class TitleTagRule(
        private val minLength: Int = 30,
        private val maxLength: Int = 60
    ) : SeoRule {
        override val id = "ONPAGE-TITLE"
        override val name = "Title Tag Validation"
        override val description = "Checks for existence, uniqueness, and recommended length of the <title> tag."

        override fun evaluate(context: PageAuditContext): List<SeoFinding> {
            val findings = mutableListOf<SeoFinding>()
            val title = context.title?.trim()

            if (title.isNullOrEmpty()) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.ON_PAGE,
                        ruleId = id,
                        severity = Severity.CRITICAL,
                        title = "Missing or Empty Title Tag",
                        explanation = "The page is missing a <title> element or the tag is completely empty. Search engines use the title tag to understand page topic and display snippet headlines.",
                        affectedUrlOrFile = context.url,
                        evidence = "Title: <none>",
                        expected = "A descriptive, unique <title> tag between $minLength and $maxLength characters.",
                        recommendation = "Add a concise, compelling <title> tag inside the <head> section highlighting primary keywords and branding.",
                        codeExample = "<title>Primary Keyword - Brand Name</title>",
                        hasAutomatedFix = true,
                        quickFixId = "ADD_TITLE"
                    )
                )
            } else {
                if (title.length < minLength) {
                    findings.add(
                        SeoFinding(
                            id = UUID.randomUUID().toString(),
                            category = AuditCategory.ON_PAGE,
                            ruleId = id,
                            severity = Severity.LOW,
                            title = "Title Tag Too Short",
                            explanation = "The title tag is shorter than $minLength characters (${title.length} chars). Short titles may miss opportunities for secondary keywords and search intent context.",
                            affectedUrlOrFile = context.url,
                            evidence = "Title: \"$title\" (${title.length} chars)",
                            expected = "Title length between $minLength and $maxLength characters.",
                            recommendation = "Expand title to provide better context, product naming, or branding without keyword stuffing.",
                            codeExample = "<title>$title - Detailed Category | SEOWebChecker</title>"
                        )
                    )
                } else if (title.length > maxLength) {
                    findings.add(
                        SeoFinding(
                            id = UUID.randomUUID().toString(),
                            category = AuditCategory.ON_PAGE,
                            ruleId = id,
                            severity = Severity.LOW,
                            title = "Title Tag May Truncate in SERPs",
                            explanation = "The title tag exceeds $maxLength characters (${title.length} chars). Longer titles may be truncated in desktop and mobile search engine results.",
                            affectedUrlOrFile = context.url,
                            evidence = "Title: \"$title\" (${title.length} chars)",
                            expected = "Title length around $minLength - $maxLength characters (typically <= 600px).",
                            recommendation = "Shorten the title by placing the primary keyword near the beginning and removing superfluous filler words.",
                            codeExample = "<title>${title.take(55)}...</title>"
                        )
                    )
                }
            }
            return findings
        }
    }

    class MetaDescriptionRule(
        private val minLength: Int = 70,
        private val maxLength: Int = 160
    ) : SeoRule {
        override val id = "ONPAGE-META-DESC"
        override val name = "Meta Description Validation"
        override val description = "Checks for missing, empty, or sub-optimal meta description length."

        override fun evaluate(context: PageAuditContext): List<SeoFinding> {
            val findings = mutableListOf<SeoFinding>()
            val desc = context.metaDescription?.trim()

            if (desc.isNullOrEmpty()) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.ON_PAGE,
                        ruleId = id,
                        severity = Severity.HIGH,
                        title = "Missing Meta Description",
                        explanation = "The page does not declare a meta description. Search engines may generate snippets from random body copy.",
                        affectedUrlOrFile = context.url,
                        evidence = "Meta description: <none>",
                        expected = "A persuasive meta description summarizing page intent between $minLength and $maxLength characters.",
                        recommendation = "Add a <meta name=\"description\" content=\"...\"> tag to encourage click-through rates.",
                        codeExample = "<meta name=\"description\" content=\"Discover comprehensive insights and actionable guides.\">",
                        hasAutomatedFix = true,
                        quickFixId = "ADD_META_DESC"
                    )
                )
            } else {
                if (desc.length < minLength) {
                    findings.add(
                        SeoFinding(
                            id = UUID.randomUUID().toString(),
                            category = AuditCategory.ON_PAGE,
                            ruleId = id,
                            severity = Severity.LOW,
                            title = "Short Meta Description",
                            explanation = "Meta description is under $minLength characters (${desc.length} chars). It may not fully communicate page value.",
                            affectedUrlOrFile = context.url,
                            evidence = "Description: \"$desc\" (${desc.length} chars)",
                            expected = "Between $minLength and $maxLength characters.",
                            recommendation = "Expand description with a call-to-action or unique value proposition.",
                            codeExample = null
                        )
                    )
                } else if (desc.length > maxLength) {
                    findings.add(
                        SeoFinding(
                            id = UUID.randomUUID().toString(),
                            category = AuditCategory.ON_PAGE,
                            ruleId = id,
                            severity = Severity.LOW,
                            title = "Meta Description May Truncate",
                            explanation = "Meta description is ${desc.length} characters long (exceeds $maxLength). It will likely be clipped in search snippets.",
                            affectedUrlOrFile = context.url,
                            evidence = "Description: \"$desc\" (${desc.length} chars)",
                            expected = "Around $minLength to $maxLength characters.",
                            recommendation = "Keep core benefit and keywords within the first 150-160 characters.",
                            codeExample = null
                        )
                    )
                }
            }
            return findings
        }
    }

    class CanonicalTagRule : SeoRule {
        override val id = "ONPAGE-CANONICAL"
        override val name = "Canonical Tag Validation"
        override val description = "Checks for missing canonical, multiple conflicting tags, and malformed canonical URLs."

        override fun evaluate(context: PageAuditContext): List<SeoFinding> {
            val findings = mutableListOf<SeoFinding>()
            val canonicals = context.canonicalTags

            if (canonicals.isEmpty()) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.ON_PAGE,
                        ruleId = id,
                        severity = Severity.MEDIUM,
                        title = "Missing Canonical Tag",
                        explanation = "No <link rel=\"canonical\"> tag found. Canonical URLs prevent duplicate content issues across query parameters, tracking tags, and protocol variants.",
                        affectedUrlOrFile = context.url,
                        evidence = "Canonical: <none>",
                        expected = "A single canonical tag pointing to the authoritative URL.",
                        recommendation = "Specify a self-referencing canonical tag on primary pages.",
                        codeExample = "<link rel=\"canonical\" href=\"${context.url}\" />",
                        hasAutomatedFix = true,
                        quickFixId = "ADD_CANONICAL"
                    )
                )
            } else if (canonicals.size > 1) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.ON_PAGE,
                        ruleId = id,
                        severity = Severity.CRITICAL,
                        title = "Multiple Conflicting Canonical Tags",
                        explanation = "The page specifies ${canonicals.size} canonical link tags. Conflicting canonicals cause search engines to ignore all canonical declarations.",
                        affectedUrlOrFile = context.url,
                        evidence = "Found: ${canonicals.joinToString(", ")}",
                        expected = "Exactly one canonical tag per page.",
                        recommendation = "Remove duplicate <link rel=\"canonical\"> tags and keep only the intended canonical target.",
                        codeExample = null
                    )
                )
            } else {
                val canon = canonicals.first()
                if (!canon.startsWith("http://") && !canon.startsWith("https://")) {
                    findings.add(
                        SeoFinding(
                            id = UUID.randomUUID().toString(),
                            category = AuditCategory.ON_PAGE,
                            ruleId = id,
                            severity = Severity.HIGH,
                            title = "Relative Canonical URL",
                            explanation = "The canonical URL is relative ($canon). Search engines strongly recommend absolute URLs to avoid interpretation ambiguities.",
                            affectedUrlOrFile = context.url,
                            evidence = "Canonical: $canon",
                            expected = "Absolute URL including scheme (https://) and domain.",
                            recommendation = "Convert the canonical link href into an absolute URL.",
                            codeExample = "<link rel=\"canonical\" href=\"https://seowebchecker.com/page\" />"
                        )
                    )
                }
            }
            return findings
        }
    }

    class RobotsDirectivesRule : SeoRule {
        override val id = "ONPAGE-ROBOTS"
        override val name = "Robots Directives & Indexability"
        override val description = "Checks for meta robots directives, conflicting directives, and noindex signals."

        override fun evaluate(context: PageAuditContext): List<SeoFinding> {
            val findings = mutableListOf<SeoFinding>()
            val directives = (context.robotsMeta + context.xRobotsTag)
                .flatMap { it.lowercase().split(",") }
                .map { it.trim() }

            val hasNoIndex = directives.contains("noindex")
            val hasIndex = directives.contains("index")
            val hasNoFollow = directives.contains("nofollow")

            if (hasNoIndex && hasIndex) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.ON_PAGE,
                        ruleId = id,
                        severity = Severity.CRITICAL,
                        title = "Conflicting Robots Directives (index and noindex)",
                        explanation = "Page has conflicting directives containing both 'index' and 'noindex'. Search engines will resolve conflict by prioritizing the more restrictive 'noindex'.",
                        affectedUrlOrFile = context.url,
                        evidence = "Directives: ${directives.joinToString(", ")}",
                        expected = "Consistent indexing directives.",
                        recommendation = "Remove either 'index' or 'noindex' based on intended page visibility.",
                        codeExample = "<meta name=\"robots\" content=\"index, follow\" />"
                    )
                )
            }

            if (hasNoIndex) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.TECHNICAL,
                        ruleId = id,
                        severity = Severity.INFORMATIONAL,
                        title = "Page Marked as NoIndex",
                        explanation = "The page explicitly declares 'noindex' in meta tags or X-Robots-Tag headers. This prevents the page from appearing in search engine indexes.",
                        affectedUrlOrFile = context.url,
                        evidence = "Directives: ${directives.joinToString(", ")}",
                        expected = "Intentional noindex application only on non-public, admin, or thin utility pages.",
                        recommendation = "Ensure this page is deliberately excluded from search results.",
                        codeExample = null
                    )
                )
            }

            return findings
        }
    }

    class LanguageAndViewportRule : SeoRule {
        override val id = "ONPAGE-LANG-VIEWPORT"
        override val name = "HTML Lang and Viewport Metadata"
        override val description = "Checks for html lang attribute and responsive viewport configuration."

        override fun evaluate(context: PageAuditContext): List<SeoFinding> {
            val findings = mutableListOf<SeoFinding>()

            if (context.htmlLang.isNullOrBlank()) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.ON_PAGE,
                        ruleId = id,
                        severity = Severity.MEDIUM,
                        title = "Missing HTML Language Attribute",
                        explanation = "The <html> element does not have a 'lang' attribute. Language tags assist search engines, browser translation features, and screen readers.",
                        affectedUrlOrFile = context.url,
                        evidence = "<html lang=\"\">",
                        expected = "<html lang=\"en\"> (or appropriate ISO 639-1 language code).",
                        recommendation = "Add the lang attribute to the root <html> tag.",
                        codeExample = "<html lang=\"en\">",
                        hasAutomatedFix = true,
                        quickFixId = "ADD_HTML_LANG"
                    )
                )
            }

            if (context.viewport.isNullOrBlank()) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.ON_PAGE,
                        ruleId = id,
                        severity = Severity.HIGH,
                        title = "Missing Viewport Meta Tag",
                        explanation = "The page does not declare a viewport meta tag. Mobile responsiveness is a critical SEO and Core Web Vitals ranking factor.",
                        affectedUrlOrFile = context.url,
                        evidence = "Viewport: <none>",
                        expected = "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">",
                        recommendation = "Add a responsive viewport meta tag to the <head> element.",
                        codeExample = "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">",
                        hasAutomatedFix = true,
                        quickFixId = "ADD_VIEWPORT"
                    )
                )
            }

            return findings
        }
    }

    class HeadingHierarchyRule : SeoRule {
        override val id = "ONPAGE-HEADINGS"
        override val name = "Heading Structure & H1 Hierarchy"
        override val description = "Evaluates presence and hierarchy of H1 and H2 heading elements."

        override fun evaluate(context: PageAuditContext): List<SeoFinding> {
            val findings = mutableListOf<SeoFinding>()
            val h1s = context.h1Headings

            if (h1s.isEmpty()) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.ON_PAGE,
                        ruleId = id,
                        severity = Severity.HIGH,
                        title = "Missing H1 Heading",
                        explanation = "The document has no <h1> heading. An H1 gives search engines and readers the primary topical anchor of the page.",
                        affectedUrlOrFile = context.url,
                        evidence = "H1 count: 0",
                        expected = "At least one descriptive <h1> tag describing page subject.",
                        recommendation = "Add a main <h1> heading at the start of page content.",
                        codeExample = "<h1>Comprehensive Technical SEO Guide</h1>",
                        hasAutomatedFix = true,
                        quickFixId = "ADD_H1"
                    )
                )
            } else if (h1s.size > 1) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.ON_PAGE,
                        ruleId = id,
                        severity = Severity.INFORMATIONAL,
                        title = "Multiple H1 Headings Detected",
                        explanation = "The page has ${h1s.size} <h1> elements. While modern HTML5 permits multiple H1s, best practice favors a single primary H1 with sub-headings structured under H2/H3.",
                        affectedUrlOrFile = context.url,
                        evidence = "H1 tags: ${h1s.joinToString(" | ")}",
                        expected = "Single primary <h1> with subordinate sections using <h2>.",
                        recommendation = "Consider downgrading secondary H1s to <h2> elements to clarify document outline.",
                        codeExample = null
                    )
                )
            }

            return findings
        }
    }

    class ImageAltTextRule : SeoRule {
        override val id = "ONPAGE-IMAGE-ALT"
        override val name = "Image Alt Attributes and Accessibility"
        override val description = "Checks images for missing alt attributes or empty non-decorative alt text."

        override fun evaluate(context: PageAuditContext): List<SeoFinding> {
            val findings = mutableListOf<SeoFinding>()
            val missingAltImages = context.images.filter { it.alt == null }
            val emptyAltNonDecorative = context.images.filter { it.alt != null && it.alt.isBlank() && !it.isDecorative }

            if (missingAltImages.isNotEmpty()) {
                val sample = missingAltImages.take(3).joinToString(", ") { it.src }
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.ON_PAGE,
                        ruleId = id,
                        severity = Severity.MEDIUM,
                        title = "Images Missing Alt Attributes (${missingAltImages.size} found)",
                        explanation = "Found ${missingAltImages.size} image(s) lacking an 'alt' attribute. Alt attributes are essential for image search indexing and screen reader accessibility.",
                        affectedUrlOrFile = context.url,
                        evidence = "Samples: $sample",
                        expected = "All <img> elements must have an 'alt' attribute.",
                        recommendation = "Add descriptive alt attributes to informational images, or alt=\"\" with role=\"presentation\" for decorative images.",
                        codeExample = "<img src=\"chart.png\" alt=\"Annual organic traffic growth chart\" />",
                        hasAutomatedFix = true,
                        quickFixId = "ADD_ALT"
                    )
                )
            }

            return findings
        }
    }
}
