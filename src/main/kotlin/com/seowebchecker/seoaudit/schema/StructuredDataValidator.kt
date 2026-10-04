package com.seowebchecker.seoaudit.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.seowebchecker.seoaudit.model.*
import com.seowebchecker.seoaudit.rules.SeoRule
import java.util.UUID

/**
 * Structured data analysis engine for JSON-LD syntax, schema types, and missing mandatory properties.
 */
class StructuredDataValidator(
    private val mapper: ObjectMapper = ObjectMapper()
) : SeoRule {

    override val id = "SCHEMA-JSONLD"
    override val name = "Structured Data & Schema.org Validation"
    override val description = "Parses JSON-LD blocks, verifies JSON syntax, identifies Schema.org types, and checks required properties."

    override fun evaluate(context: PageAuditContext): List<SeoFinding> {
        val findings = mutableListOf<SeoFinding>()

        if (context.jsonLdBlocks.isEmpty()) {
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.STRUCTURED_DATA,
                    ruleId = id,
                    severity = Severity.INFORMATIONAL,
                    title = "No JSON-LD Structured Data Detected",
                    explanation = "The page contains no JSON-LD structured data scripts (<script type=\"application/ld+json\">). Schema markup helps search engines understand entities and display rich snippets.",
                    affectedUrlOrFile = context.url,
                    evidence = "JSON-LD blocks: 0",
                    expected = "Appropriate Schema.org type (e.g. WebSite, Organization, Article, Product, BreadcrumbList).",
                    recommendation = "Add structured data relevant to the content to improve search result presentation.",
                    codeExample = """
                        <script type="application/ld+json">
                        {
                          "@context": "https://schema.org",
                          "@type": "WebPage",
                          "name": "${context.title ?: "Page Title"}"
                        }
                        </script>
                    """.trimIndent(),
                    docUrl = "https://seowebchecker.com/"
                )
            )
            return findings
        }

        for ((index, block) in context.jsonLdBlocks.withIndex()) {
            val trimmed = block.trim()
            try {
                val node: JsonNode = mapper.readTree(trimmed)
                validateNode(node, context.url, index, findings)
            } catch (e: Exception) {
                findings.add(
                    SeoFinding(
                        id = UUID.randomUUID().toString(),
                        category = AuditCategory.STRUCTURED_DATA,
                        ruleId = id,
                        severity = Severity.CRITICAL,
                        title = "Malformed JSON-LD Syntax (Block #${index + 1})",
                        explanation = "The JSON-LD snippet contains invalid JSON syntax and cannot be parsed: ${e.message}",
                        affectedUrlOrFile = context.url,
                        evidence = "Snippet: ${trimmed.take(120)}...",
                        expected = "Valid RFC 8259 JSON syntax.",
                        recommendation = "Validate and correct the JSON structure using a JSON linter or validator.",
                        codeExample = null
                    )
                )
            }
        }

        return findings
    }

    private fun validateNode(node: JsonNode, url: String, blockIndex: Int, findings: MutableList<SeoFinding>) {
        if (node.isArray) {
            for (item in node) {
                validateEntity(item, url, blockIndex, findings)
            }
        } else if (node.isObject) {
            if (node.has("@graph")) {
                val graph = node.get("@graph")
                if (graph.isArray) {
                    for (item in graph) {
                        validateEntity(item, url, blockIndex, findings)
                    }
                }
            } else {
                validateEntity(node, url, blockIndex, findings)
            }
        }
    }

    private fun validateEntity(node: JsonNode, url: String, blockIndex: Int, findings: MutableList<SeoFinding>) {
        val typeNode = node.get("@type")
        if (typeNode == null || typeNode.isNull) {
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.STRUCTURED_DATA,
                    ruleId = id,
                    severity = Severity.HIGH,
                    title = "Structured Data Missing @type",
                    explanation = "An entity in JSON-LD block #${blockIndex + 1} does not specify a '@type' attribute.",
                    affectedUrlOrFile = url,
                    evidence = "Node keys: ${node.fieldNames().asSequence().joinToString()}",
                    expected = "\"@type\": \"<SchemaType>\"",
                    recommendation = "Specify a valid Schema.org type such as Organization, Article, or Product.",
                    codeExample = "\"@type\": \"Organization\""
                )
            )
            return
        }

        val type = typeNode.asText()

        // Check common types for essential properties
        when (type) {
            "Article", "NewsArticle", "BlogPosting" -> {
                checkRequiredProperty(node, "headline", type, url, findings)
                checkRequiredProperty(node, "author", type, url, findings)
                checkRequiredProperty(node, "datePublished", type, url, findings)
            }
            "Product" -> {
                checkRequiredProperty(node, "name", type, url, findings)
                checkRequiredProperty(node, "offers", type, url, findings)
            }
            "Organization", "LocalBusiness" -> {
                checkRequiredProperty(node, "name", type, url, findings)
                checkRequiredProperty(node, "url", type, url, findings)
            }
            "BreadcrumbList" -> {
                checkRequiredProperty(node, "itemListElement", type, url, findings)
            }
        }
    }

    private fun checkRequiredProperty(
        node: JsonNode,
        prop: String,
        type: String,
        url: String,
        findings: MutableList<SeoFinding>
    ) {
        if (!node.has(prop) || node.get(prop).isNull) {
            findings.add(
                SeoFinding(
                    id = UUID.randomUUID().toString(),
                    category = AuditCategory.STRUCTURED_DATA,
                    ruleId = id,
                    severity = Severity.MEDIUM,
                    title = "$type Schema Missing '$prop' Property",
                    explanation = "The $type structured data markup is missing the recommended '$prop' property.",
                    affectedUrlOrFile = url,
                    evidence = "Type: $type, missing: $prop",
                    expected = "Include '$prop' in the $type schema definition.",
                    recommendation = "Add the missing property to maximize schema completeness and eligibility for enhanced SERP features.",
                    codeExample = "\"$prop\": \"...\""
                )
            )
        }
    }
}
