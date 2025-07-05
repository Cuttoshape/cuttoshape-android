package com.example.cuttoshapenew.views.authviews


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cuttoshapenew.utils.DataStoreManager
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    var userEmail by remember { mutableStateOf<String?>(null) }
    var userFirstName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        DataStoreManager.getUserEmail(context).collectLatest { email ->
            userEmail = email
        }
        DataStoreManager.getUserFirstName(context).collectLatest { firstName ->
            userFirstName = firstName
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Your Profile",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        userFirstName?.let {
            Text(
                text = "Name: $it",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        userEmail?.let {
            Text(
                text = "Email: $it",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        if (userEmail == null || userFirstName == null) {
            Text("Loading user data...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}