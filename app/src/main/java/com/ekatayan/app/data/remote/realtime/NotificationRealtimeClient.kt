package com.ekatayan.app.data.remote.realtime

import com.ekatayan.app.BuildConfig
import com.ekatayan.app.data.remote.api.NotificationDto
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

sealed interface NotificationRealtimeEvent {
    data class Insert(val notification: NotificationDto) : NotificationRealtimeEvent
    data object Connected : NotificationRealtimeEvent
    data object Disconnected : NotificationRealtimeEvent
}

interface NotificationRealtimeClient {
    val events: Flow<NotificationRealtimeEvent>
    fun subscribe(accessToken: String, userId: String)
    fun ensureConnected()
    fun unsubscribe()
}

internal fun buildRealtimeHttpUrl(supabaseUrl: String, publishableKey: String) =
    supabaseUrl.toHttpUrl().newBuilder()
        .addPathSegments("realtime/v1/websocket")
        .addQueryParameter("apikey", publishableKey)
        .addQueryParameter("vsn", "1.0.0")
        .build()

/**
 * Minimal Supabase Realtime/Postgres Changes client over the app's existing
 * OkHttp stack. The channel is both JWT-authenticated and server-filtered to
 * the current user's notification rows; PostgreSQL RLS remains authoritative.
 */
@Singleton
class OkHttpNotificationRealtimeClient @Inject constructor() : NotificationRealtimeClient {
    private data class Subscription(val accessToken: String, val userId: String)

    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    private val mutableEvents = MutableSharedFlow<NotificationRealtimeEvent>(extraBufferCapacity = 32)
    override val events = mutableEvents.asSharedFlow()
    private val references = AtomicLong(0)

    @Volatile private var desired: Subscription? = null
    @Volatile private var socket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private var reconnectAttempt = 0

    @Synchronized
    override fun subscribe(accessToken: String, userId: String) {
        if (accessToken.isBlank() || userId.isBlank()) {
            unsubscribe()
            return
        }
        val next = Subscription(accessToken, userId)
        if (desired == next && socket != null) return
        closeSocket()
        desired = next
        reconnectAttempt = 0
        connect(next)
    }

    @Synchronized
    override fun ensureConnected() {
        val subscription = desired ?: return
        if (socket == null && reconnectJob?.isActive != true) connect(subscription)
    }

    @Synchronized
    override fun unsubscribe() {
        desired = null
        reconnectJob?.cancel()
        reconnectJob = null
        reconnectAttempt = 0
        closeSocket()
    }

    @Synchronized
    private fun connect(subscription: Subscription) {
        if (desired != subscription || socket != null) return
        val request = runCatching {
            Request.Builder()
                .url(realtimeUrl())
                .build()
        }.getOrElse {
            logger.severe("Notification Realtime URL is not configured correctly")
            mutableEvents.tryEmit(NotificationRealtimeEvent.Disconnected)
            return
        }
        socket = client.newWebSocket(request, listener(subscription))
        logger.info("Notification Realtime connection starting")
    }

    private fun listener(subscription: Subscription) = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            if (desired != subscription || socket !== webSocket) {
                webSocket.close(NORMAL_CLOSE, "stale session")
                return
            }
            webSocket.send(joinMessage(subscription))
            startHeartbeat(webSocket, subscription)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            runCatching { gson.fromJson(text, JsonObject::class.java) }
                .onSuccess { message -> handleMessage(webSocket, message) }
                .onFailure { logger.warning("Notification Realtime delivered malformed JSON") }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            handleDisconnect(webSocket, subscription)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            logger.warning("Notification Realtime disconnected (${response?.code ?: "network"})")
            handleDisconnect(webSocket, subscription)
        }
    }

    private fun handleMessage(webSocket: WebSocket, message: JsonObject) {
        if (socket !== webSocket) return
        when (message.string("event")) {
            "phx_reply" -> {
                val status = message.objectValue("payload")?.string("status")
                val isChannelReply = message.string("topic")?.startsWith("realtime:notifications:") == true
                if (isChannelReply && status == "ok") {
                    reconnectAttempt = 0
                    mutableEvents.tryEmit(NotificationRealtimeEvent.Connected)
                    logger.info("Notification Realtime subscribed")
                } else if (isChannelReply && status != "ok") {
                    logger.warning("Notification Realtime subscription was rejected")
                    webSocket.close(POLICY_VIOLATION_CLOSE, "subscription rejected")
                }
            }
            "postgres_changes" -> {
                val record = message.objectValue("payload")
                    ?.objectValue("data")
                    ?.objectValue("record")
                    ?: return
                runCatching { gson.fromJson(record, NotificationDto::class.java) }
                    .onSuccess { mutableEvents.tryEmit(NotificationRealtimeEvent.Insert(it)) }
                    .onFailure { logger.warning("Notification Realtime row could not be parsed") }
            }
        }
    }

    @Synchronized
    private fun handleDisconnect(webSocket: WebSocket, subscription: Subscription) {
        if (socket !== webSocket) return
        socket = null
        heartbeatJob?.cancel()
        heartbeatJob = null
        mutableEvents.tryEmit(NotificationRealtimeEvent.Disconnected)
        if (desired == subscription) scheduleReconnect(subscription)
    }

    @Synchronized
    private fun scheduleReconnect(subscription: Subscription) {
        if (reconnectJob?.isActive == true) return
        val delayMillis = minOf(30_000L, 1_000L shl minOf(reconnectAttempt++, 5))
        reconnectJob = scope.launch {
            delay(delayMillis)
            synchronized(this@OkHttpNotificationRealtimeClient) {
                reconnectJob = null
                if (desired == subscription && socket == null) connect(subscription)
            }
        }
    }

    private fun startHeartbeat(webSocket: WebSocket, subscription: Subscription) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive && desired == subscription && socket === webSocket) {
                delay(25_000)
                webSocket.send(message("phoenix", "heartbeat", emptyMap<String, Any>()))
            }
        }
    }

    @Synchronized
    private fun closeSocket() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        socket?.close(NORMAL_CLOSE, "session ended")
        socket = null
    }

    private fun joinMessage(subscription: Subscription): String {
        val changes = listOf(
            mapOf(
                "event" to "INSERT",
                "schema" to "public",
                "table" to "notifications",
                "filter" to "user_id=eq.${subscription.userId}",
            ),
        )
        val config = mapOf(
            "broadcast" to mapOf("ack" to false, "self" to false),
            "presence" to mapOf("key" to ""),
            "postgres_changes" to changes,
        )
        return message(
            topic = "realtime:notifications:${subscription.userId}",
            event = "phx_join",
            payload = mapOf("config" to config, "access_token" to subscription.accessToken),
            joinRef = nextReference(),
        )
    }

    private fun message(
        topic: String,
        event: String,
        payload: Any,
        joinRef: String? = null,
    ): String {
        val ref = joinRef ?: nextReference()
        return gson.toJson(
            mapOf(
                "topic" to topic,
                "event" to event,
                "payload" to payload,
                "ref" to ref,
                "join_ref" to joinRef,
            ),
        )
    }

    private fun nextReference() = references.incrementAndGet().toString()

    // OkHttp's newWebSocket expects an HTTP(S) request URL and performs the
    // ws/wss upgrade internally. HttpUrl deliberately rejects ws/wss schemes.
    private fun realtimeUrl() = buildRealtimeHttpUrl(
        BuildConfig.SUPABASE_URL,
        BuildConfig.SUPABASE_PUBLISHABLE_KEY,
    )

    private fun JsonObject.string(name: String) = get(name)?.takeUnless { it.isJsonNull }?.asString
    private fun JsonObject.objectValue(name: String) = get(name)?.takeIf { it.isJsonObject }?.asJsonObject

    private companion object {
        const val NORMAL_CLOSE = 1000
        const val POLICY_VIOLATION_CLOSE = 1008
        val logger: Logger = Logger.getLogger("EkataYanNotifications")
    }
}
