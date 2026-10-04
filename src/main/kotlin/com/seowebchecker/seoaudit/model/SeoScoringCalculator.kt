package com.seowebchecker.seoaudit.model

import kotlin.math.max

/**
 * Calculates a configurable SEO Health Score (0 - 100) based on severity deductions
 * and category breakdown.
 */
object SeoScoringCalculator {

    fun calculateScore(findings: List<SeoFinding>): Int {
        if (findings.isEmpty()) return 100

        var deductions = 0
        for (finding in findings) {
            deductions += when (finding.severity) {
                Severity.CRITICAL -> 25
                Severity.HIGH -> 12
                Severity.MEDIUM -> 5
                Severity.LOW -> 2
                Severity.INFORMATIONAL -> 0
            }
        }

        return max(0, 100 - deductions)
    }

    fun buildSummary(findings: List<SeoFinding>): AuditSummary {
        val critical = findings.count { it.severity == Severity.CRITICAL }
        val high = findings.count { it.severity == Severity.HIGH }
        val medium = findings.count { it.severity == Severity.MEDIUM }
        val low = findings.count { it.severity == Severity.LOW }
        val info = findings.count { it.severity == Severity.INFORMATIONAL }

        val categoryScores = AuditCategory.entries.associateWith { category ->
            val catFindings = findings.filter { it.category == category }
            calculateScore(catFindings)
        }

        return AuditSummary(
            totalFindings = findings.size,
            criticalCount = critical,
            highCount = high,
            mediumCount = medium,
            lowCount = low,
            infoCount = info,
            healthScore = calculateScore(findings),
            categoryScores = categoryScores
        )
    }
}
