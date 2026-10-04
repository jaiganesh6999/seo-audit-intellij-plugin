package com.seowebchecker.seoaudit.rules

import com.seowebchecker.seoaudit.model.PageAuditContext
import com.seowebchecker.seoaudit.model.SeoFinding

/**
 * Base interface for SEO Audit Rules.
 */
interface SeoRule {
    val id: String
    val name: String
    val description: String

    fun evaluate(context: PageAuditContext): List<SeoFinding>
}
