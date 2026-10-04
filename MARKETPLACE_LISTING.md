# JetBrains Marketplace Listing Plan: Advanced SEO Audit

## 1. General Information
- **Plugin Name:** Advanced SEO Audit
- **Plugin ID:** `com.seowebchecker.seoaudit`
- **Vendor:** SEOWebChecker
- **Vendor URL:** [https://seowebchecker.com/](https://seowebchecker.com/)
- **Documentation & Homepage:** [https://seowebchecker.com/](https://seowebchecker.com/)
- **Category:** Web Development, Code Inspection, Tools
- **License:** Apache License 2.0
- **Supported IDEs:** IntelliJ IDEA (Ultimate & Community), WebStorm, PhpStorm, PyCharm (Professional & Community), GoLand, RubyMine, Rider, CLion, Android Studio (2023.3+ through 2024.3+).

---

## 2. Short Description (Search Card Summary)
> Professional technical SEO auditing, on-page diagnostics, Schema.org validation, website crawler, and one-click IDE quick fixes inside JetBrains IDEs.

---

## 3. Full Marketplace Description

### Transform Your JetBrains IDE into a Full Technical SEO Auditing Command Center
Catch indexing blockers, missing metadata, broken schema markup, Core Web Vitals bottlenecks, and crawl defects before shipping code to production.

Powered by [SEOWebChecker](https://seowebchecker.com/), **Advanced SEO Audit** gives web developers, frontend engineers, technical SEOs, and site architects immediate visibility into search engine optimization health without switching context or leaving the IDE.

---

### 🔍 Key Capabilities

#### 📑 1. On-Page SEO Auditing
- **Title Tag Intelligence:** Detects missing titles, pixel-width estimation, and excessive length (>60 chars).
- **Meta Description Verification:** Audits snippet readability, truncation risks, and empty tags.
- **Canonical Consistency:** Flags missing, duplicate, conflicting, or relative canonical tags.
- **Robots Directives:** Catches contradictory `index, noindex` statements and parses `X-Robots-Tag` headers.
- **Semantic Structure:** Audits `<h1>` through `<h3>` heading hierarchies and flags missing or duplicate H1s.
- **Image Optimization & Accessibility:** Detects missing `alt` attributes, distinguishing decorative graphics.
- **I18N & Mobile Readiness:** Verifies `<meta name="viewport">` and `<html lang="...">` declarations.

#### ⚙️ 2. Technical SEO & Bounded Website Crawler
- **Response Validation:** Flags 4xx dead links, 5xx backend crashes, and soft-404 patterns.
- **Redirect Chain Diagnostics:** Uncovers redirect hops, redirect loops, and latency-heavy paths.
- **Protocol & Mixed Content:** Enforces HTTPS integrity and flags insecure HTTP asset links.
- **Enterprise-Grade SSRF Defense:** Bounded multi-threaded crawler blocks private IP ranges (RFC 1918), loopback (`127.0.0.1`), link-local IPs, and cloud metadata services (`169.254.169.254`).

#### 🏷️ 3. Structured Data & Schema.org Validator
- **Native JSON-LD Parser:** Detects and parses JSON-LD scripts with strict RFC 8259 syntax validation.
- **Schema.org Type Validation:** Checks required properties for:
  - `Article`, `NewsArticle`, `BlogPosting` (`headline`, `author`, `datePublished`)
  - `Product` (`name`, `offers`)
  - `Organization`, `LocalBusiness` (`name`, `url`)
  - `BreadcrumbList` (`itemListElement`)
  - `WebSite`
- Clear separation between standard Schema.org compliance and search engine rich-result eligibility.

#### ⚡ 4. Core Web Vitals & Performance Diagnostics
- **Time to First Byte (TTFB):** Evaluates server response times against Google CWV thresholds (< 800ms).
- **Cumulative Layout Shift (CLS):** Identifies images missing explicit `width` and `height` dimensions.
- **Modern Performance Heuristics:** Audits `loading="lazy"` on offscreen images and checks text compression (`gzip`, `brotli`).

#### 💻 5. IDE Project Workspace Scanner & Quick Fixes
- **Static Code Analysis:** Scans `.html`, `.php`, `.vue`, `.jsx`, `.tsx`, `.svelte`, `.blade.php`, and `.twig`.
- **Infrastructure Auditing:** Verifies `robots.txt` syntax and `sitemap.xml` structure.
- **Environment Leak Detection:** Flags staging and localhost URLs (`http://localhost`, `127.0.0.1`) left in templates.
- **One-Click Quick Fixes:** Interactive diff previews and atomic automated fixes for missing titles, meta descriptions, canonical tags, and alt attributes.

#### 📊 6. Comprehensive Multi-Format Exports
- Export complete audit reports to sanitized **HTML Reports**, **JSON**, **CSV**, and **Markdown**.

---

### 🛡️ Privacy & Security First
- **100% Local Processing:** Source scanning runs entirely within your local IDE process.
- **Zero Remote Code Transmission:** Your code, proprietary URLs, and secrets are never sent to external servers.
- **No Telemetry / No Tracking:** Complete confidentiality for enterprise repositories.

---

## 4. Screenshot Asset Specifications & Captures
1. `screenshot_1_dashboard.png` (1280x800): **SEO Audit Tool Window** showing overall health score dial (94/100), category score breakdown cards (On-Page, Technical, Schema, CWV), and findings table.
2. `screenshot_2_findings_detail.png` (1280x800): **Finding Inspector Panel** showing detailed explanation, evidence, expected standard, code example, and recommendation.
3. `screenshot_3_quick_fix.png` (1280x800): **One-Click Quick Fix Preview** displaying IntelliJ native diff view inserting `<link rel="canonical" href="...">`.
4. `screenshot_4_crawler.png` (1280x800): **Website Crawler in Action** showing real-time progress bar, discovered URLs, status codes, and active SSRF guardrails.
5. `screenshot_5_settings.png` (1280x800): **Settings Dialog** under `Tools -> Advanced SEO Audit` configuring concurrency, timeouts, and thresholds.

---

## 5. Getting Started & Documentation
Visit the official documentation at [https://seowebchecker.com/](https://seowebchecker.com/) for tutorials, configuration guides, and best practices.
