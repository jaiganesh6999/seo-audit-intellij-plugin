package com.seowebchecker.seoaudit.crawler

import java.net.InetAddress
import java.net.URI

/**
 * Validates URLs and IP destinations to protect against Server-Side Request Forgery (SSRF),
 * loopback scanning, and private/metadata subnet crawling.
 */
object SsrfProtection {

    private val BLOCKED_SCHEMES = setOf("file", "ftp", "gopher", "ldap", "dict", "jar", "netdoc")

    fun isSafeUrl(urlString: String): Pair<Boolean, String?> {
        val uri = try {
            URI(urlString)
        } catch (e: Exception) {
            return false to "Malformed URL: ${e.message}"
        }

        val scheme = uri.scheme?.lowercase() ?: return false to "Missing URL protocol/scheme"
        if (scheme !in listOf("http", "https")) {
            return false to "Forbidden protocol: $scheme (only HTTP and HTTPS are permitted)"
        }

        val host = uri.host ?: return false to "Missing host in URL"

        // Disallow standard loopback/metadata host names directly
        val lowerHost = host.lowercase()
        if (lowerHost == "localhost" || lowerHost.endsWith(".localhost") || lowerHost == "127.0.0.1" || lowerHost == "::1") {
            return false to "Loopback host is forbidden for security: $host"
        }
        if (lowerHost == "169.254.169.254" || lowerHost == "metadata.google.internal") {
            return false to "Cloud metadata endpoints are strictly forbidden: $host"
        }

        return try {
            val addresses = InetAddress.getAllByName(host)
            for (addr in addresses) {
                if (isPrivateOrLoopback(addr)) {
                    return false to "Host '$host' resolves to non-routable/private IP (${addr.hostAddress})"
                }
            }
            true to null
        } catch (e: Exception) {
            false to "DNS resolution failed for host '$host': ${e.message}"
        }
    }

    private fun isPrivateOrLoopback(addr: InetAddress): Boolean {
        if (addr.isLoopbackAddress || addr.isAnyLocalAddress || addr.isLinkLocalAddress || addr.isSiteLocalAddress) {
            return true
        }

        // Additional IPv4 checks for 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16, 169.254.0.0/16, 100.64.0.0/10
        val bytes = addr.address
        if (bytes.size == 4) {
            val b0 = bytes[0].toInt() and 0xFF
            val b1 = bytes[1].toInt() and 0xFF

            if (b0 == 10) return true
            if (b0 == 172 && b1 in 16..31) return true
            if (b0 == 192 && b1 == 168) return true
            if (b0 == 169 && b1 == 254) return true // link-local
            if (b0 == 127) return true // loopback
            if (b0 == 0) return true // broadcast
        }

        return false
    }
}
