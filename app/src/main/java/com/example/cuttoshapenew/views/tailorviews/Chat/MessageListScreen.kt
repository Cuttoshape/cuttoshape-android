package com.example.cuttoshapenew.views.tailorviews.Chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cuttoshapenew.apiclients.MessageResponse
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.utils.WebSocketClient
import kotlinx.coroutines.launch
import parseConnectionsFromJson

@Composable
fun MessageListScreen(
    navController: NavHostController,
    userId: String
) {
    var connections by remember { mutableStateOf(listOf<MessageResponse>()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Fetch initial connections from API
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = RetrofitClient.getClient(context).getMessages(userId)
                connections = response
            } catch (e: Exception) {
                // Log or display error
                println("Error fetching connections: ${e.message}")
            }
        }
    }

    // Establish WebSocket connection for real-time updates
    DisposableEffect(Unit) {
        val webSocketClient = WebSocketClient("wss://backend.cuttoshape.com/connection?userId=$userId") { message ->
            val newList = parseConnectionsFromJson(message)
            connections = newList
        }
        webSocketClient.connect()

        onDispose {
            webSocketClient.close()
        }
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search...") },
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        LazyColumn {
            items(connections) { name ->
                ListItem(
                    headlineContent = { Text("${name.firstName} ${name.lastName}") },
                    leadingContent = {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color(0xFF2196F3)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${name.firstName.first()} ${name.lastName.first()}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .clickable {
                            navController.navigate("chat_screen/${name.id}")
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}