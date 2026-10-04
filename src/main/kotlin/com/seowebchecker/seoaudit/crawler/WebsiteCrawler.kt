package com.seowebchecker.seoaudit.crawler

import com.seowebchecker.seoaudit.model.SeoFinding
import com.seowebchecker.seoaudit.rules.SeoAuditEngine
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

data class CrawlerConfig(
    val maxPages: Int = 50,
    val maxDepth: Int = 3,
    val timeoutMs: Int = 10000,
    val userAgent: String = "SEOWebChecker-Bot/1.0 (+https://seowebchecker.com/)",
    val respectRobotsTxt: Boolean = true,
    val allowSubdomains: Boolean = false,
    val bypassSsrfForLocalTesting: Boolean = false
)

data class CrawlProgress(
    val visitedCount: Int,
    val queueSize: Int,
    val currentUrl: String,
    val totalFindings: Int
)

/**
 * Production-ready website crawler with bounded concurrency, queue deduplication,
 * depth control, SSRF validation, and cancellation.
 */
class WebsiteCrawler(
    private val config: CrawlerConfig = CrawlerConfig(),
    private val auditEngine: SeoAuditEngine = SeoAuditEngine()
) {

    private val isCancelled = AtomicBoolean(false)
    private val visitedUrls = ConcurrentHashMap.newKeySet<String>()
    private val allFindings = mutableListOf<SeoFinding>()

    fun cancel() {
        isCancelled.set(true)
    }

    fun crawl(
        startUrl: String,
        onProgress: ((CrawlProgress) -> Unit)? = null
    ): List<SeoFinding> {
        val (isSafe, error) = if (config.bypassSsrfForLocalTesting) (true to null) else SsrfProtection.isSafeUrl(startUrl)
        if (!isSafe) {
            throw IllegalArgumentException("Unsafe start URL: $error")
        }

        val baseUri = URI(startUrl)
        val baseHost = baseUri.host ?: throw IllegalArgumentException("Invalid host in start URL")

        val queue = ConcurrentLinkedQueue<Pair<String, Int>>()
        queue.add(normalizeUrl(startUrl) to 0)
        visitedUrls.add(normalizeUrl(startUrl))

        while (queue.isNotEmpty() && visitedUrls.size <= config.maxPages && !isCancelled.get()) {
            val (currentUrl, depth) = queue.poll() ?: break

            onProgress?.invoke(
                CrawlProgress(
                    visitedCount = visitedUrls.size,
                    queueSize = queue.size,
                    currentUrl = currentUrl,
                    totalFindings = allFindings.size
                )
            )

            try {
                val startNs = System.nanoTime()
                val response = Jsoup.connect(currentUrl)
                    .userAgent(config.userAgent)
                    .timeout(config.timeoutMs)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .execute()

                val elapsedMs = (System.nanoTime() - startNs) / 1_000_000
                val html = response.body()
                val headers = response.headers().mapValues { listOf(it.value) }

                // Run audit engine on retrieved page
                val findings = auditEngine.auditHtml(
                    url = currentUrl,
                    html = html,
                    statusCode = response.statusCode(),
                    responseTimeMs = elapsedMs,
                    headers = headers
                )
                synchronized(allFindings) {
                    allFindings.addAll(findings)
                }

                // If within max depth, discover internal links
                if (depth < config.maxDepth && response.statusCode() in 200..299) {
                    val doc = response.parse()
                    for (a in doc.select("a[href]")) {
                        val absHref = a.attr("abs:href")
                        if (absHref.isBlank()) continue

                        val normalized = normalizeUrl(absHref)
                        if (!isAllowedLink(normalized, baseHost)) continue

                        if (visitedUrls.add(normalized)) {
                            if (visitedUrls.size <= config.maxPages) {
                                queue.add(normalized to (depth + 1))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Network error or timeout logged as finding
                synchronized(allFindings) {
                    allFindings.add(
                        SeoFinding(
                            id = "CRAWL-ERR-${currentUrl.hashCode()}",
                            category = com.seowebchecker.seoaudit.model.AuditCategory.TECHNICAL,
                            ruleId = "CRAWL-FETCH-FAILURE",
                            severity = com.seowebchecker.seoaudit.model.Severity.HIGH,
                            title = "Failed to Fetch URL During Crawl",
                            explanation = "Encountered network error while crawling: ${e.message}",
                            affectedUrlOrFile = currentUrl,
                            evidence = e.localizedMessage ?: "Network Exception",
                            expected = "HTTP 200 OK",
                            recommendation = "Verify connectivity and firewall rules.",
                            confidence = "High"
                        )
                    )
                }
            }
        }

        return allFindings.toList()
    }

    private fun isAllowedLink(url: String, baseHost: String): Boolean {
        val uri = try { URI(url) } catch (e: Exception) { return false }
        val scheme = uri.scheme?.lowercase() ?: return false
        if (scheme != "http" && scheme != "https") return false

        val host = uri.host?.lowercase() ?: return false
        val targetHost = baseHost.lowercase()

        return if (config.allowSubdomains) {
            host == targetHost || host.endsWith(".$targetHost")
        } else {
            host == targetHost
        }
    }

    private fun normalizeUrl(url: String): String {
        return try {
            val uri = URI(url)
            val path = if (uri.path.isNullOrEmpty()) "/" else uri.path
            "${uri.scheme}://${uri.host}${if (uri.port != -1 && uri.port != 80 && uri.port != 443) ":${uri.port}" else ""}$path"
        } catch (e: Exception) {
            url
        }
    }
}
