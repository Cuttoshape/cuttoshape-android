package com.example.cuttoshapenew.views.tailorviews.Chat

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cuttoshapenew.apiclients.ChatContent
import com.example.cuttoshapenew.apiclients.ChatDetails
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.utils.DataStoreManager
import com.example.cuttoshapenew.utils.WebSocketClient
import com.example.cuttoshapenew.utils.parseMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val CHAT_TAG = "WS_CHAT"

@Composable
fun ChatScreen(
    receiverId: String,
    userId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Keep messages in order (oldest -> newest)
    var messages by remember { mutableStateOf(listOf<ChatDetails>()) }
    var input by remember { mutableStateOf("") }
    var isSocketOpen by remember { mutableStateOf(false) }
    var socket by remember { mutableStateOf<WebSocketClient?>(null) }
    val connected = isSocketOpen
    var token by remember { mutableStateOf<String?>(null) }


        val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    // 1) Initial history via HTTP
    LaunchedEffect(receiverId, userId) {
        try {
            val resp = RetrofitClient.getClient(context)
                .getUserMessageChat(userId, receiverId, page = "1", size = "30")
            messages = resp.rows
        } catch (e: Exception) {
            println("Error fetching messages: ${e.message}")
        }
    }

    LaunchedEffect(Unit) {
        launch {
            token = DataStoreManager.getToken(context).first()
        }
    }

    // 2) Single WebSocket connection + main-thread UI updates
    DisposableEffect(receiverId, userId) {
        val client = WebSocketClient(
            url = "wss://backend.cuttoshape.com/?userId=$userId", // <-- changed
            token = token,  // add JWT if your backend needs it
            scope = scope,
            onOpen = { isSocketOpen = true; Log.d(CHAT_TAG, "WS OPEN") },
            onMessage = { raw ->
                Log.d(CHAT_TAG, "WS IN: $raw")
                val parsed = parseMessage(raw)
                if (parsed == null) return@WebSocketClient

                // Compare as strings; server sometimes sends numbers
                val u = userId
                val r = receiverId
                val belongsToThisChat =
                    (parsed.senderId == r && parsed.receiverId == u) ||
                            (parsed.senderId == u && parsed.receiverId == r)

                Log.d(CHAT_TAG, "parsed(id=${parsed.id}, sender=${parsed.senderId}, recv=${parsed.receiverId}) match=$belongsToThisChat")

                if (belongsToThisChat) {
                    messages = messages + parsed
                }
            },
            onClosed = { c, r -> isSocketOpen = false; Log.d(CHAT_TAG, "WS CLOSED $c $r") },
            onFailure = { t, code, body ->
                isSocketOpen = false
                Log.e(CHAT_TAG, "WS FAIL: ${t.message} http=$code body=$body", t)
            }
        )
        socket = client
        client.connect()
        onDispose { client.close(); socket = null }
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
            items(
                items = messages,
                key = { it.id.ifBlank { "${it.senderId}-${it.receiverId}-${it.content.content}-${it.status}-${it.type}-${it.createdAt}" } }
            ) { message ->
                Box(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    contentAlignment = if (message.senderId == userId) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        color = if (message.senderId == userId) Color(0xFF4A90E2) else Color.LightGray,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            message.content.content,
                            modifier = Modifier.padding(8.dp),
                            color = if (message.senderId == userId) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        Row(Modifier.padding(16.dp).fillMaxWidth()) {
            IconButton(onClick = { /* attachments */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Green)
            }
            TextField(
                value = input,
                onValueChange = { input = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color(0xFF4A90E2),
                    unfocusedIndicatorColor = Color.Gray,
                    cursorColor = Color.Black
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") }
            )
            IconButton(
                enabled = input.isNotBlank() && isSocketOpen,
                onClick = {
                    val text = input
                    input = ""

                    // optimistic UI (optional)
                    messages = messages + ChatDetails(
                        id = System.currentTimeMillis().toString(),
                        senderId = userId,
                        receiverId = receiverId,
                        content = ChatContent(content = text, productId = ""),
                        type = "STRING",        // keep consistent locally too
                        status = "NEW",
                        createdAt = "", createdBy = "", updatedAt = "", updatedBy = "",
                        deletedAt = "", deletedBy = ""
                    )

                    val payload = buildSendJsonStrict(userId, receiverId, text)
                    Log.d(CHAT_TAG, "WS OUT: $payload")
                    val sent = socket?.sendMessage(payload) ?: false
                    Log.d(CHAT_TAG, "WS OUT SENT? $sent isOpen=$isSocketOpen")
                }
            ) { Icon(Icons.Default.Send, null, tint = if (connected) Color(0xFF4A90E2) else Color.LightGray) }

// Optional tiny indicator in your UI:
            Text(if (connected) "Online" else "Connecting…", color = if (connected) Color(0xFF28A745) else Color.Gray)
        }
    }
}

// JSON helper (escapes quotes/newlines)
private fun buildSendJsonStrict(senderId: String, receiverId: String, text: String): String {
    val senderNum = senderId.toIntOrNull()
    val receiverNum = receiverId.toIntOrNull()

    val content = org.json.JSONObject().apply {
        put("content", text)            // ONLY "content" per server frames
    }

    return org.json.JSONObject().apply {
        // If ids parse to Int, send as numbers; else fallback to strings
        if (senderNum != null) put("senderId", senderNum) else put("senderId", senderId)
        if (receiverNum != null) put("receiverId", receiverNum) else put("receiverId", receiverId)

        put("type", "STRING")           // match server enum casing
        put("status", "NEW")            // match server value
        put("content", content)
        // DO NOT send messageId (server sets it); DO NOT send productId unless server requires it
    }.toString()
}
