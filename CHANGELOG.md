# Changelog

All notable changes to the **Advanced SEO Audit** JetBrains IDE plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-10-05

### Added
- **Plugin Infrastructure:**
  - Full JetBrains Platform 2023.3+ (IC / IU / WS / PS / PC) plugin configuration.
  - Native Swing tool window integration with dedicated "SEO Audit" side panel.
  - Persistent application settings under `Settings -> Tools -> Advanced SEO Audit`.
- **On-Page SEO Engine:**
  - Automated auditing for HTML title tags, character limits, and pixel width estimation.
  - Meta description validation with length recommendations (70-160 chars) and truncation alerts.
  - Canonical link tag analysis detecting missing tags, conflicting duplicates, and relative targets.
  - Robots directives auditing (`index`, `noindex`, `nofollow`, contradictory pairs, and `X-Robots-Tag`).
  - Document structure validation: HTML `lang` attributes, responsive viewport metadata, and H1/H2 heading hierarchy.
  - Image accessibility diagnostics identifying missing `alt` attributes and distinguishing decorative graphics.
- **Technical SEO Engine:**
  - HTTP status validation: detection of 4xx client errors and 5xx server issues.
  - Redirect diagnostics: hops calculation, redirect loop identification, and chain warnings.
  - Security inspection: HTTPS enforcement and mixed-content reference detection.
- **Structured Data & Schema.org Validator:**
  - Real-time JSON-LD script parsing and RFC 8259 syntax validation.
  - Entity-level schema auditing for `Article`, `NewsArticle`, `BlogPosting`, `Product`, `Organization`, `LocalBusiness`, `BreadcrumbList`, and `WebSite`.
  - Missing recommended and required schema property diagnostics.
- **Performance & Core Web Vitals:**
  - Time to First Byte (TTFB) evaluation with latency thresholds (< 800ms).
  - Cumulative Layout Shift (CLS) risk analysis via image dimension checks (`width` and `height`).
  - Resource optimization checks: lazy loading (`loading="lazy"`) and response compression (`gzip`/`brotli`).
- **Content Depth & Link Structure:**
  - Body text extraction, word count calculation, and thin content risk flags (< 300 words).
  - Keyword occurrence and density analysis.
  - Link hygiene inspection: empty anchor tags, internal vs external link categorization, and `rel="noopener"` enforcement for external targets.
- **Crawling & Workspace Scanning:**
  - High-performance bounded crawler with concurrency controls, depth bounds, and cancellation tokens.
  - Enterprise SSRF defense blocking RFC 1918 private IPs, loopback (`127.0.0.1`), link-local IPs, and cloud metadata endpoints (`169.254.169.254`).
  - Static workspace scanner traversing `.html`, `.php`, `.vue`, `.jsx`, `.tsx`, `.svelte`, `.blade.php`, `robots.txt`, and `sitemap.xml`.
  - Staging / localhost URL leak detection in templates.
- **Remediation & Exporting:**
  - One-click safe IDE quick fixes for missing title, description, canonical, alt, and lang tags.
  - Export audit findings to clean HTML reports, structured JSON, CSV, and GitHub-flavored Markdown.

---
Official documentation and technical guides available at [https://seowebchecker.com/](https://seowebchecker.com/).
