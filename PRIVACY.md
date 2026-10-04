# Privacy & Telemetry Policy — Advanced SEO Audit

**Effective Date:** October 5, 2026  
**Vendor:** SEOWebChecker  
**Website:** [https://seowebchecker.com/](https://seowebchecker.com/)

---

## 1. Local Processing Commitment
The **Advanced SEO Audit** plugin is architected with a strict privacy-first, developer-first philosophy:
- **Zero Remote Source Exfiltration:** All source code scanning, template analysis (`.html`, `.php`, `.jsx`, `.tsx`, `.vue`, `.svelte`), and workspace inspections occur 100% locally on your machine within the JetBrains IDE process.
- **No Third-Party Telemetry:** We do not collect, transmit, or monetize your keystrokes, source files, proprietary URLs, codebase contents, or search data.
- **No Unsolicited External Connections:** The plugin does not initiate outbound network traffic unless you explicitly instruct it to crawl a live website URL via the tool window or crawler dialog.

---

## 2. Crawler Operation & SSRF Defense
When you initiate an active URL crawl:
- HTTP requests are sent exclusively to the target website specified by the developer.
- Built-in Server-Side Request Forgery (SSRF) filters block requests destined for local loopback addresses (`127.0.0.1`, `localhost`), link-local IPs, private subnets (RFC 1918), and cloud metadata endpoints (`169.254.169.254`).
- No crawl results are uploaded to external databases. Results are stored in memory or local IDE storage for the duration of your session.

---

## 3. Reporting & Exports
Audit reports generated via the Export tool window (HTML, JSON, CSV, Markdown) are saved exclusively to the local file path you select on your computer.

---

## 4. Inquiries & Support
For security notices, vulnerability disclosures, or privacy questions, visit:  
**Website:** [https://seowebchecker.com/](https://seowebchecker.com/)
