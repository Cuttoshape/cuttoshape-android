package com.example.cuttoshapenew.views.tailorviews.Chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cuttoshapenew.apiclients.ChatDetails
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.utils.WebSocketClient
import com.example.cuttoshapenew.utils.parseMessage
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    receiverId: String,
    userId: String
) {
    var messages by remember { mutableStateOf(listOf<ChatDetails>()) }
    var input by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val page by remember { mutableStateOf("1") }
    val size by remember { mutableStateOf("10") }
    var webSocketClient by remember { mutableStateOf<WebSocketClient?>(null) }

    // Fetch initial messages from API
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.getClient(context).getUserMessageChat(userId, receiverId, page, size)
                messages = response.rows
            } catch (e: Exception) {
                println("Error fetching messages: ${e.message}")
            }
        }
    }

    // Establish WebSocket connection for real-time updates
    DisposableEffect(Unit) {
        val client = WebSocketClient("wss://backend.cuttoshape.com/connection?userId=$userId") { message ->
            val parsed = parseMessage(message)
            if (parsed.senderId == receiverId || parsed.receiverId == receiverId) {
                messages = messages + parsed
            }
        }
        webSocketClient = client
        client.connect()

        onDispose {
            client.close()
            webSocketClient = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
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
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            IconButton(onClick = {
            }) {
                Icon(Icons.Default.Add, contentDescription = "Send", tint = Color.Green)
            }
            TextField(
                value = input,
                onValueChange = { input = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = Color(0xFF4A90E2),
                    unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                    cursorColor = Color.Black,
                    focusedIndicatorColor = Color(0xFF4A90E2),
                    unfocusedIndicatorColor = Color.Gray
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") }
            )
            IconButton(onClick = {
                if (input.isNotBlank() && webSocketClient != null) {
                    sendMessageWebSocket(webSocketClient!!, receiverId, input, userId)
                    input = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF4A90E2))
            }
        }
    }
}

fun sendMessageWebSocket(webSocketClient: WebSocketClient, receiverId: String, message: String, senderId: String) {
    val json = """
        {
            "senderId": "$senderId",
            "receiverId": "$receiverId",
            "content": {"content": "$message", "productId": ""},
            "type": "text",
            "status": "sent"
        }
    """.trimIndent()
    webSocketClient.sendMessage(json)
}