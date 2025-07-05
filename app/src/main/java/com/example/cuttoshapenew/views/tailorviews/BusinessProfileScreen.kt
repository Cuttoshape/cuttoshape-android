package com.example.cuttoshapenew.views.tailorviews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cuttoshapenew.utils.DataStoreManager
import com.example.cuttoshapenew.apiclients.CreateBusinessRequest
import com.example.cuttoshapenew.apiclients.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun BusinessProfileScreen(navController: NavController) {
    val buttonColor = Color(0xFF0089FA)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var businessName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var ownerId by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        ownerId = DataStoreManager.getUserId(context).first()?.toIntOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "← Back",
                modifier = Modifier
                    .clickable {
                        if (successMessage != null) {
                            // Business creation was successful, navigate to dashboard
                            navController.navigate("dashboard") {
                                popUpTo("business_registration") { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            // Business creation not successful, navigate to business_registration
                            navController.popBackStack()
                        }
                    }
                    .padding(start = 16.dp),
                fontSize = 16.sp,
                color = Color.Blue
            )
        }

        Text("Business Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                shape = CircleShape,
                color = Color.LightGray,
                modifier = Modifier.size(120.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                    contentDescription = null,
                    modifier = Modifier.padding(30.dp),
                    tint = Color.White
                )
            }
            Surface(
                shape = CircleShape,
                color = buttonColor,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        successMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Color.Green,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Business Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text("State") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text("Country") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = zipCode,
                onValueChange = { zipCode = it },
                label = { Text("ZipCode") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Code") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Upload Documents Logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Upload Documents")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        try {
                            if (ownerId != null) {
                                val request = CreateBusinessRequest(
                                    name = businessName,
                                    email = email,
                                    address = address,
                                    city = city,
                                    state = state,
                                    country = country,
                                    zipCode = zipCode,
                                    mobile = mobile,
                                    avatar = null, // Update if avatar upload is implemented
                                    ownerId = ownerId!!,
                                    status = "REGISTERED",
                                    code = code
                                )
                                val response = RetrofitClient.getClient(context).createBusiness(request)
                                successMessage = "Business profile created successfully!"

                            } else {
                                errorMessage = "Cannot create business profile: User ID is missing."
                            }
                        } catch (e: HttpException) {
                            errorMessage = when (e.code()) {
                                400 -> "Invalid data provided. Please check your inputs."
                                401 -> "Authentication failed. Please log in again."
                                403 -> "You don’t have permission to create a business profile."
                                else -> "Failed to create business profile: ${e.message()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Failed to create business profile: ${e.message ?: "An unexpected error occurred."}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp),
                enabled = businessName.isNotBlank() && email.isNotBlank() && ownerId != null && !isLoading
            ) {
                Text("Save")
            }
        }
    }
}
