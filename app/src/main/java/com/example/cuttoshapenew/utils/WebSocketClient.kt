package com.example.cuttoshapenew.utils

import okhttp3.*
import okio.ByteString

class WebSocketClient(
    private val url: String,
    private val onMessageReceived: (String) -> Unit
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun connect() {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessageReceived(text)
            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket opened")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket failed: ${t.message}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("WebSocket closing: $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("WebSocket closed: $reason")
            }
        })
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun close() {
        webSocket?.close(1000, "User closed")
    }
}