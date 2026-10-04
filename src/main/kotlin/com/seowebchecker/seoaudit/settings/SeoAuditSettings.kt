package com.seowebchecker.seoaudit.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent configuration state for Advanced SEO Audit.
 */
@State(
    name = "SeoAuditSettingsState",
    storages = [Storage("SeoAuditSettings.xml")]
)
class SeoAuditSettings : PersistentStateComponent<SeoAuditSettings> {

    var crawlerMaxPages: Int = 50
    var crawlerMaxDepth: Int = 3
    var crawlerTimeoutMs: Int = 10000
    var crawlerUserAgent: String = "SEOWebChecker-Bot/1.0 (+https://seowebchecker.com/)"
    var respectRobotsTxt: Boolean = true
    var allowSubdomains: Boolean = false
    var bypassSsrfForLocalTesting: Boolean = false

    var googlePageSpeedApiKey: String = ""
    var enableTelemetry: Boolean = false

    override fun getState(): SeoAuditSettings {
        return this
    }

    override fun loadState(state: SeoAuditSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: SeoAuditSettings
            get() = ApplicationManager.getApplication().getService(SeoAuditSettings::class.java)
    }
}
