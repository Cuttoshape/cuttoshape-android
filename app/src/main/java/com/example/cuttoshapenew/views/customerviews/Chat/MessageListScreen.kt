package com.example.cuttoshapenew.views.customerviews.Chat

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cuttoshapenew.apiclients.MessageResponse
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.utils.WebSocketClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import parseConnectionsFromJson
private const val CHAT_TAG = "WS_CHAT"

@Composable
fun CustomerMessageListScreen(
    navController: NavHostController,
    userId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var connections by remember { mutableStateOf(listOf<MessageResponse>()) }
    var webSocketClient by remember { mutableStateOf<WebSocketClient?>(null) }
    var isSocketOpen by remember { mutableStateOf(false) }

    // Initial load via HTTP
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.getClient(context).getMessages(userId)
            connections = response
        } catch (e: Exception) {
            println("Error fetching connections: ${e.message}")
        }
    }

    // Realtime updates via WS (uses the new WebSocketClient with main-thread dispatch)
    DisposableEffect(userId) {
        val client = WebSocketClient(
            url = "wss://backend.cuttoshape.com/connection?userId=$userId",
            token = null,                  // add JWT if your backend needs it
            scope = scope,                 // <-- important: pass scope
            onOpen = {
                isSocketOpen = true
                Log.d(CHAT_TAG, "WS OPEN")
            },
            onMessage = { raw ->
                Log.d(CHAT_TAG, "WS IN: $raw")
                // If your server multiplexes different event types, filter here
                // e.g. if (isConnectionsPayload(raw)) { ... }
                val newList = parseConnectionsFromJson(raw)
                connections = newList      // already on Main thread
            },
            onClosed = { code, reason ->
                isSocketOpen = false
                Log.d(CHAT_TAG, "WS CLOSED: code=$code reason=$reason")
            },
            onFailure = { t, code, body ->
                isSocketOpen = false
                Log.e(CHAT_TAG, "WS FAIL: ${t.message}  http=$code  body=$body", t)
            }
        )
        webSocketClient = client
        client.connect()

        onDispose {
            client.close()
            webSocketClient = null
        }
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = "",
            onValueChange = {}, // hook up search later
            placeholder = { Text("Search...") },
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
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        LazyColumn {
            items(connections, key = { it.id }) { user ->
                ListItem(
                    headlineContent = { Text("${user.firstName} ${user.lastName}") },
                    leadingContent = {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color(0xFF2196F3)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${user.firstName.first()} ${user.lastName.first()}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .clickable { navController.navigate("chat_screen/${user.id}") }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}