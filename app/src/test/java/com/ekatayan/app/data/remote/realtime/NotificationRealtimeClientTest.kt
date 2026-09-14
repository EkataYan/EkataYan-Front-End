package com.ekatayan.app.data.remote.realtime

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationRealtimeClientTest {
    @Test
    fun websocketRequestUsesHttpsUrlForOkHttpUpgrade() {
        val url = buildRealtimeHttpUrl(
            supabaseUrl = "https://project.supabase.co/",
            publishableKey = "publishable-test-key",
        )

        assertEquals("https", url.scheme)
        assertEquals("/realtime/v1/websocket", url.encodedPath)
        assertEquals("publishable-test-key", url.queryParameter("apikey"))
        assertEquals("1.0.0", url.queryParameter("vsn"))
    }
}
