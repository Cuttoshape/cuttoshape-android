package com.example.cuttoshapenew.views.tailorviews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Face
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.apiclients.UpdateProfileRequest
import com.example.cuttoshapenew.apiclients.User
import com.example.cuttoshapenew.utils.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException


@Composable
fun UserProfileScreen(navController: NavController) {
    val buttonColor = Color(0xFF0089FA)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var userId by remember { mutableStateOf<Int?>(null) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {

        userId = DataStoreManager.getUserId(context).first()?.toIntOrNull()
        email = DataStoreManager.getUserEmail(context).first() ?: ""
        firstName = DataStoreManager.getUserFirstName(context).first() ?: ""
        lastName = DataStoreManager.getUserLastName(context).first() ?: ""
        address = DataStoreManager.getUserAddress(context).first() ?: ""
        city = DataStoreManager.getUserCity(context).first() ?: ""
        state = DataStoreManager.getUserState(context).first() ?: ""
        zipCode = DataStoreManager.getUserZipCode(context).first() ?: ""
        country = DataStoreManager.getUserCountry(context).first() ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start) {
            Text(
                text = "← Back",
                modifier = Modifier
                    .clickable { navController.popBackStack() }
                    .padding(start = 16.dp),

                fontSize = 16.sp,
                color = buttonColor,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Text(text = "User Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Avatar with camera icon
        Box(contentAlignment = Alignment.BottomEnd) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .padding(20.dp),
                tint = Color.White
            )
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Camera",
                modifier = Modifier
                    .offset(x = (-8).dp, y = (-8).dp)
                    .size(28.dp)
                    .background(buttonColor, CircleShape)
                    .padding(4.dp),
                tint = Color.White
            )
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        successMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Color.Green,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Input fields
        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
        Row {
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
        Row {
            OutlinedTextField(
                value = zipCode,
                onValueChange = { zipCode = it },
                label = { Text("ZipCode") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                label = { Text("Country") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null
                    successMessage = null
                    try {
                        if (userId != null) {
                            val request = UpdateProfileRequest(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                address = address.ifEmpty { null },
                                city = city.ifEmpty { null },
                                state = state.ifEmpty { null },
                                zipCode = zipCode.ifEmpty { null },
                                country = country.ifEmpty { null }
                            )
                            val response = RetrofitClient.getClient(context).updateProfile(userId!!, request)
                            // Since the API returns the updated user object with a 200 status code, we can assume success
                            DataStoreManager.saveAuthData(
                                context,
                                DataStoreManager.getToken(context).first() ?: "",
                                "TAILOR",
                                User(
                                    id = userId!!,
                                    email = email,
                                    firstName = firstName,
                                    lastName = lastName,
                                    userType = "TAILOR", // Update if stored
                                    photoUrl = null,
                                    address = address.ifEmpty { null },
                                    phone = null,
                                    city = city.ifEmpty { null },
                                    state = state.ifEmpty { null },
                                    zipCode = zipCode.ifEmpty { null },
                                    country = country.ifEmpty { null },
                                    createdAt = "",
                                    createdBy = null,
                                    updatedAt = "",
                                    updatedBy = null,
                                    deletedAt = null,
                                    deletedBy = null,
                                    business = null,
                                    bodyData = emptyList(),
                                    shippingAddresses = emptyList(),
                                    cartItems = emptyList()
                                )
                            )
                            successMessage = "Profile updated successfully!"
                            //navController.popBackStack() // Return to previous screen
                        } else {
                            errorMessage = "Cannot update profile: User ID is missing."
                        }
                    } catch (e: HttpException) {
                        errorMessage = when (e.code()) {
                            400 -> "Invalid data provided. Please check your inputs."
                            401 -> "Authentication failed. Please log in again."
                            403 -> "You don’t have permission to update this profile."
                            404 -> "User not found. Please log in again."
                            else -> "Failed to update profile: ${e.message()}"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Failed to update profile: ${e.message ?: "An unexpected error occurred."}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
            shape = RoundedCornerShape(8.dp),// green
        ) {
            Text(text = "Save", color = Color.White, fontSize = 18.sp)
        }
    }
}
