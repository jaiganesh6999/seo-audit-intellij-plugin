# Advanced SEO Audit — JetBrains IDE Plugin

[![JetBrains Plugin](https://img.shields.io/badge/JetBrains%20Marketplace-Advanced%20SEO%20Audit-blue)](https://seowebchecker.com/)
[![Compatibility](https://img.shields.io/badge/Compatible%20With-IntelliJ%20%7C%20WebStorm%20%7C%20PhpStorm%20%7C%20PyCharm%20%7C%20All%20IDEs-orange)](https://seowebchecker.com/)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)

Professional technical SEO auditing, on-page analysis, website crawling, performance diagnostics, structured-data validation, actionable recommendations, and developer-friendly SEO tools directly inside JetBrains IDEs.

Powered by [SEOWebChecker](https://seowebchecker.com/).

---

## 🚀 Features

### 1. On-Page SEO Audit
- **Title Tags:** Validates missing titles, length (30-60 character best practices), pixel estimation, and branding placement.
- **Meta Descriptions:** Checks presence, character limits (70-160 characters), truncation warnings, and snippet optimization.
- **Canonical Tags:** Identifies missing canonicals, duplicate conflicting tags, and flags relative URLs.
- **Robots Directives:** Evaluates `index`, `noindex`, `nofollow`, flags contradictory `index, noindex` statements, and inspects `X-Robots-Tag` HTTP response headers.
- **Heading Structure:** Validates presence of `<h1>`, hierarchical order (`<h1>` &rarr; `<h2>` &rarr; `<h3>`), and alerts on multiple H1 elements.
- **Accessibility & Image SEO:** Audits images for missing `alt` attributes, distinguishing decorative images (`role="presentation"`) from informational graphics.
- **Mobile & I18N:** Checks `<meta name="viewport">` and `<html lang="...">` declarations.

### 2. Technical SEO & Crawl Engine
- **HTTP Status Codes:** Flags 4xx dead ends and 5xx server failures.
- **Redirects:** Detects redirect chains, redirect loops, and excessive hops (> 2 hops).
- **HTTPS & Security:** Flags mixed content (`http://` resources on `https://` pages) and unencrypted URLs.
- **URL Normalization:** Audits trailing slash discrepancies and query parameter anomalies.
- **Crawler with SSRF Defense:** Bounded multithreaded crawler with built-in SSRF protections, blocking localhost (`127.0.0.1`), private RFC1918 subnets, link-local IPs, and cloud metadata endpoints (`169.254.169.254`).

### 3. Structured Data & Schema.org Validator
- **JSON-LD Engine:** Robust syntax parsing and validation of `<script type="application/ld+json">` blocks.
- **Schema.org Type Validation:** Checks mandatory properties for core schemas:
  - `Article`, `NewsArticle`, `BlogPosting` (`headline`, `author`, `datePublished`)
  - `Product` (`name`, `offers`)
  - `Organization`, `LocalBusiness` (`name`, `url`)
  - `BreadcrumbList` (`itemListElement`)
  - `WebSite`

### 4. Performance & Core Web Vitals Diagnostics
- **Time to First Byte (TTFB):** Evaluates server response latency against Google CWV thresholds (< 800ms).
- **Layout Shift (CLS) Prevention:** Detects images missing explicit `width` and `height` dimensions.
- **Lazy Loading:** Identifies off-screen image assets missing `loading="lazy"`.
- **Text Compression:** Flags HTML responses exceeding 5KB served without `gzip`, `brotli`, or `deflate`.

### 5. Project Source Code Scanner & IDE Quick Fixes
- **Static Workspace Scanner:** Recursively inspects project source files (`.html`, `.php`, `.vue`, `.jsx`, `.tsx`, `.svelte`, `.blade.php`, `.twig`, `.astro`).
- **Config & Infrastructure Validation:** Scans `robots.txt` and `sitemap.xml`.
- **Environment Leak Detection:** Flags staging and localhost URLs (`http://localhost`, `127.0.0.1`, `staging.example.com`) left in production templates.
- **One-Click Quick Fixes:** Interactive diff previews and atomic code patches for adding `<title>`, `<meta name="description">`, `<link rel="canonical">`, `alt`, and `lang`.

### 6. Interactive Tool Window & Multi-Format Exports
- **JetBrains Native UI:** Dedicated "SEO Audit" tool window with live health score dial (0-100), category breakdown cards, severity filters, search filter, and finding inspector.
- **Multi-Format Export Subsystem:** Export audit findings to sanitized **HTML Reports**, **JSON**, **CSV**, and **Markdown**.

---

## 🛠 Installation

### From JetBrains Marketplace
1. Open **Settings / Preferences** (`Ctrl+Alt+S` on Windows/Linux, `Cmd+,` on macOS).
2. Navigate to **Plugins** &rarr; **Marketplace**.
3. Search for **Advanced SEO Audit** by **SEOWebChecker**.
4. Click **Install** and restart the IDE if prompted.

### From ZIP Distribution
1. Download `advanced-seo-audit-plugin-1.0.0.zip` from releases or build locally.
2. In your IDE, go to **Settings** &rarr; **Plugins** &rarr; ⚙️ **(Gear icon)** &rarr; **Install Plugin from Disk...**.
3. Select the `.zip` archive and click **OK**.

---

## 💻 Building from Source

### Requirements
- JDK 17+ (e.g. Eclipse Temurin 17)
- Gradle 8.0+ (or use the included `gradlew` wrapper)

### Build Commands
```bash
# Clone the repository
git clone https://github.com/seowebchecker/advanced-seo-audit-plugin.git
cd advanced-seo-audit-plugin

# Run tests
./gradlew test

# Verify plugin compatibility against IntelliJ SDK
./gradlew verifyPlugin

# Package distribution ZIP
./gradlew buildPlugin
```
The packaged ZIP will be generated in `build/distributions/advanced-seo-audit-plugin-1.0.0.zip`.

---

## ⚙️ Configuration

Configure audit settings under **Settings / Preferences** &rarr; **Tools** &rarr; **Advanced SEO Audit**:
- **Crawler Concurrency:** Thread pool bounds (default: 4 threads).
- **Max Crawl Pages:** Page threshold limit (default: 100 pages).
- **Connection & Read Timeouts:** Network timeouts (default: 8000ms).
- **User-Agent:** Customizable User-Agent header (defaults to `SEOWebChecker-AuditBot/1.0`).
- **Minimum Word Count:** Threshold for thin-content warnings (default: 300 words).
- **SSRF Protection:** Toggle to block loopback, link-local, and private subnets.

---

## 🔒 Security & Privacy

The plugin executes all crawling and static code inspection locally within your machine and IDE. No source code, project files, or audit findings are uploaded to external telemetry or third-party servers.

For more information, see our [Privacy Policy](https://seowebchecker.com/) and [SEOWebChecker Documentation](https://seowebchecker.com/).

---

## 📄 License

This plugin is licensed under the [Apache License, Version 2.0](LICENSE).
Copyright (c) 2026 SEOWebChecker. All rights reserved.
