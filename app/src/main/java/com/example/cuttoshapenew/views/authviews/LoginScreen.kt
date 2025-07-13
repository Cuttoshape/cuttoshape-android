package com.example.cuttoshapenew.views.authviews

import android.annotation.SuppressLint
import retrofit2.HttpException
import com.google.gson.Gson
import com.google.gson.JsonObject
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog // ✅ This is the missing import
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.apiclients.LoginRequest
import com.example.cuttoshapenew.utils.DataStoreManager
import kotlinx.coroutines.launch
import androidx.navigation.NavController

@SuppressLint("UnusedBoxWithConstraintsScope")
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onSignUpClick: () -> Unit,
    navController: NavController,
    //onLoginSuccess: () -> Unit, // Callback to indicate successful login
) {
    val buttonColor = Color(0xFF0089FA)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        BoxWithConstraints {
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .width(screenWidth * 1f) // almost full width
                    .height(screenHeight * 0.7f) // height to your taste
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize(),

                ) {
                    // Title
                    Spacer(modifier = Modifier.height(64.dp)) // Adjust the value to your taste
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log In",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Inputs
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit button
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        val request = LoginRequest(email = email, password = password)
                                        val response = RetrofitClient.getClient(context).login(request)
                                        // Store token and user data
                                        DataStoreManager.saveAuthData(context, response.token, response.user.userType, response.user)
                                        val userType = response.user.userType

                                        if (userType.uppercase() == "TAILOR") {
                                            // Check if the user has a business profile
                                            if (response.user.business != null) {
                                                // Navigate to Dashboard if business exists
                                                navController.navigate("dashboard") {
                                                    popUpTo(navController.graph.startDestinationId)
                                                    launchSingleTop = true
                                                }
                                            } else {
                                                // Navigate to Business Registration if no business exists
                                                navController.navigate("business_registration") {
                                                    popUpTo(navController.graph.startDestinationId)
                                                    launchSingleTop = true
                                                }
                                            }
                                        } else {
                                            // Navigate to Profile screen for non-tailors
                                            navController.navigate("profile") {
                                                popUpTo(navController.graph.startDestinationId)
                                                launchSingleTop = true
                                            }
                                        }
                                        onDismiss()
                                    } catch (e: Exception) {
                                        errorMessage = when (e) {
                                            is HttpException -> {
                                                val errorBody = e.response()?.errorBody()?.string()
                                                val errorMsg = try {
                                                    val json = Gson().fromJson(errorBody, JsonObject::class.java)
                                                    json["message"]?.asString ?: "Unknown error"
                                                } catch (parseException: Exception) {
                                                    "Unexpected error format"
                                                }
                                                "Login failed: $errorMsg"
                                            }
                                            else -> "Login failed: ${e.localizedMessage ?: "Unknown error"}"
                                        }
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading
                        ) {
                            Text("Submit")
                        }
                    }


                    Spacer(modifier = Modifier.height(12.dp))

                    // Links
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.Black)) {
                                append("Forgot Password? ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = buttonColor,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Reset")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* TODO */ },
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.Black)) {
                                append("Don't have an account? ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = buttonColor,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Sign up")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSignUpClick() },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}