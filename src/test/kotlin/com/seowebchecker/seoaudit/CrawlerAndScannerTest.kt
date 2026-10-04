package com.seowebchecker.seoaudit

import com.seowebchecker.seoaudit.crawler.CrawlerConfig
import com.seowebchecker.seoaudit.crawler.WebsiteCrawler
import com.seowebchecker.seoaudit.quickfix.SeoQuickFixManager
import com.seowebchecker.seoaudit.reports.ReportExporter
import com.seowebchecker.seoaudit.rules.SeoAuditEngine
import com.seowebchecker.seoaudit.scanner.ProjectSourceScanner
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class CrawlerAndScannerTest {

    private lateinit var mockServer: MockWebServer

    @BeforeEach
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()
    }

    @AfterEach
    fun teardown() {
        mockServer.shutdown()
    }

    @Test
    fun `test website crawler with mock server and link discovery`() {
        val page1Html = """
            <!DOCTYPE html>
            <html lang="en">
            <head><title>Page 1 Title That Meets Requirements Here</title></head>
            <body>
                <h1>Page 1</h1>
                <a href="/page2">Go to Page 2</a>
            </body>
            </html>
        """.trimIndent()

        val page2Html = """
            <!DOCTYPE html>
            <html lang="en">
            <head><title>Page 2 Title With Sufficient Character Length</title></head>
            <body>
                <h1>Page 2</h1>
                <p>Welcome to page 2</p>
            </body>
            </html>
        """.trimIndent()

        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(page1Html))
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody(page2Html))

        val crawler = WebsiteCrawler(
            config = CrawlerConfig(
                maxPages = 5,
                maxDepth = 2,
                bypassSsrfForLocalTesting = true
            )
        )

        val baseUrl = mockServer.url("/").toString()
        val findings = crawler.crawl(baseUrl)

        assertNotNull(findings)
        assertTrue(findings.isNotEmpty())
    }

    @Test
    fun `test project source scanner and quick fix on local files`() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "seo-test-" + System.currentTimeMillis())
        tempDir.mkdirs()

        try {
            val testFile = File(tempDir, "index.html")
            testFile.writeText("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Short</title>
                </head>
                <body>
                    <img src="banner.png">
                </body>
                </html>
            """.trimIndent())

            val scanner = ProjectSourceScanner()
            val findings = scanner.scanDirectory(tempDir)

            val altFinding = findings.firstOrNull { it.quickFixId == "ADD_ALT" }
            assertNotNull(altFinding, "Should detect missing alt tag")

            // Test quick fix preview and apply
            val preview = SeoQuickFixManager.previewFix(testFile, altFinding!!)
            assertNotNull(preview)
            assertTrue(preview!!.modifiedContent.contains("alt=\"\""))

            val success = SeoQuickFixManager.applyFix(testFile, altFinding)
            assertTrue(success)
            assertTrue(testFile.readText().contains("alt=\"\""))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `test report exporter outputs clean json, csv, and markdown`() {
        val engine = SeoAuditEngine()
        val findings = engine.auditHtml("https://seowebchecker.com/", "<html><head></head><body></body></html>")

        val exporter = ReportExporter()
        val json = exporter.exportJson("https://seowebchecker.com/", findings)
        assertTrue(json.contains("\"target\" : \"https://seowebchecker.com/\""))

        val csv = exporter.exportCsv(findings)
        assertTrue(csv.contains("Rule ID,Category,Severity"))

        val md = exporter.exportMarkdown("https://seowebchecker.com/", findings)
        assertTrue(md.contains("# Advanced SEO Audit Report"))
        assertTrue(md.contains("https://seowebchecker.com/"))

        val html = exporter.exportHtml("https://seowebchecker.com/", findings)
        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("Advanced Technical SEO Audit"))
    }
}
