package com.seowebchecker.seoaudit.quickfix

import com.seowebchecker.seoaudit.model.SeoFinding
import java.io.File

/**
 * Applies deterministic, safe local quick fixes to source files.
 * Provides diff previews and ensures non-destructive edits.
 */
object SeoQuickFixManager {

    data class FixPreview(
        val originalContent: String,
        val modifiedContent: String,
        val diffDescription: String
    )

    fun previewFix(file: File, finding: SeoFinding): FixPreview? {
        if (!file.exists() || !file.isFile) return null
        val content = file.readText()

        val modified = when (finding.quickFixId) {
            "ADD_TITLE" -> {
                if (content.contains("<head>", ignoreCase = true)) {
                    content.replaceFirst(Regex("<head>", RegexOption.IGNORE_CASE), "<head>\n    <title>Page Title | SEOWebChecker</title>")
                } else null
            }
            "ADD_META_DESC" -> {
                if (content.contains("<head>", ignoreCase = true)) {
                    content.replaceFirst(Regex("<head>", RegexOption.IGNORE_CASE), "<head>\n    <meta name=\"description\" content=\"Enter descriptive, search-optimized page description here.\">")
                } else null
            }
            "ADD_CANONICAL" -> {
                if (content.contains("<head>", ignoreCase = true)) {
                    content.replaceFirst(Regex("<head>", RegexOption.IGNORE_CASE), "<head>\n    <link rel=\"canonical\" href=\"https://seowebchecker.com/\" />")
                } else null
            }
            "ADD_HTML_LANG" -> {
                if (content.contains("<html", ignoreCase = true)) {
                    content.replaceFirst(Regex("<html(\\s*)>", RegexOption.IGNORE_CASE), "<html lang=\"en\">")
                } else null
            }
            "ADD_VIEWPORT" -> {
                if (content.contains("<head>", ignoreCase = true)) {
                    content.replaceFirst(Regex("<head>", RegexOption.IGNORE_CASE), "<head>\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                } else null
            }
            "ADD_ALT" -> {
                // Add alt="" placeholder to first img lacking it
                val imgRegex = Regex("<img(?![^>]*\\balt=)([^>]*)>", RegexOption.IGNORE_CASE)
                if (imgRegex.containsMatchIn(content)) {
                    content.replaceFirst(imgRegex, "<img alt=\"\"$1>")
                } else null
            }
            "ADD_REL_NOOPENER" -> {
                val blankRegex = Regex("<a\\s+([^>]*target=[\"']_blank[\"'][^>]*)>", RegexOption.IGNORE_CASE)
                if (blankRegex.containsMatchIn(content)) {
                    content.replaceFirst(blankRegex, "<a $1 rel=\"noopener noreferrer\">")
                } else null
            }
            else -> null
        } ?: return null

        return FixPreview(
            originalContent = content,
            modifiedContent = modified,
            diffDescription = "Applied quick fix: ${finding.title}"
        )
    }

    fun applyFix(file: File, finding: SeoFinding): Boolean {
        val preview = previewFix(file, finding) ?: return false
        return try {
            file.writeText(preview.modifiedContent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
