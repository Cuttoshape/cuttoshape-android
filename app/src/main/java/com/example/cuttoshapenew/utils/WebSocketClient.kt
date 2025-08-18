// utils/WebSocketClient.kt
package com.example.cuttoshapenew.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*

class WebSocketClient(
    private val url: String,
    private val token: String? = null,
    private val scope: CoroutineScope,
    private val onOpen: () -> Unit = {},
    private val onMessage: (String) -> Unit = {},
    private val onClosed: (code: Int, reason: String) -> Unit = { _, _ -> },
    // NEW: surface response info
    private val onFailure: (Throwable, Int?, String?) -> Unit = { _, _, _ -> }
) {
    private val client = OkHttpClient.Builder()
        .pingInterval(java.time.Duration.ofSeconds(15))  // keepalive
        .build()

    private var webSocket: WebSocket? = null
    @Volatile private var isOpen = false
    private val pending = ArrayDeque<String>()           // queue while connecting

    fun connect() {
        val reqBuilder = Request.Builder()
            .url(url)
            // some servers require Origin for WS handshake (even from native apps)
            .addHeader("Origin", "https://web.cuttoshape.com")

        if (!token.isNullOrBlank()) {
            reqBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = reqBuilder.build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                isOpen = true
                scope.launch(Dispatchers.Main) { onOpen() }
                flushQueue()
            }

            override fun onMessage(ws: WebSocket, text: String) {
                // Marshal back to Main for Compose state updates
                scope.launch(Dispatchers.Main) { onMessage(text) }
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d("WS", "onClosed $code $reason")
                isOpen = false
                scope.launch(Dispatchers.Main) { onClosed(code, reason) }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                isOpen = false
                val code = response?.code
                val body = try { response?.body?.string() } catch (_: Exception) { null }
                scope.launch(Dispatchers.Main) { onFailure(t, code, body) }
            }
        })
    }

    fun sendMessage(text: String): Boolean {
        val safe = text.trim()
        val ws = webSocket
        return when {
            safe.isEmpty() -> false
            isOpen && ws != null -> ws.send(safe)
            else -> { pending.addLast(safe); true }      // queue until open
        }
    }

    private fun flushQueue() {
        val ws = webSocket ?: return
        while (pending.isNotEmpty()) {
            val item = pending.removeFirst()
            ws.send(item)
        }
    }

    fun close() {
        try { webSocket?.close(1000, "closing") } catch (_: Exception) {}
        isOpen = false
        pending.clear()
    }

    fun isConnected(): Boolean = isOpen
}
