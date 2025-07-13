package com.example.cuttoshapenew.views.authviews

import android.annotation.SuppressLint
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
import androidx.compose.ui.window.Dialog
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.apiclients.SignUpRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import retrofit2.HttpException

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupDialog(
    onDismiss: () -> Unit,
    onLoginClick: () -> Unit,
    onSignupSuccess: () -> Unit,
) {
    val buttonColor = Color(0xFF0089FA)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("Tailor") }
    var invitationCode by remember { mutableStateOf("") }

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
                    .width(screenWidth * 1f)
                    .height(screenHeight * 0.8f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Up",
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = userType == "Customer",
                                onClick = { userType = "Customer" }
                            )
                            Text("Customer")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = userType == "Tailor",
                                onClick = { userType = "Tailor" }
                            )
                            Text("Tailor")
                        }
                    }

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

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
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = invitationCode,
                        onValueChange = { invitationCode = it },
                        label = { Text("Invitation Code") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = userType == "Tailor"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    if (password != confirmPassword) {
                                        errorMessage = "Passwords do not match."
                                        isLoading = false
                                        return@launch
                                    }
                                    if (userType == "Tailor" && invitationCode.isBlank()) {
                                        errorMessage = "Invitation code is required for Tailors."
                                        isLoading = false
                                        return@launch
                                    }

                                    try {
                                        val request = SignUpRequest(
                                            fullname = fullName,
                                            email = email,
                                            password = password,
                                            userType = userType.uppercase(),
                                            invitationCode = invitationCode
                                        )
                                        val response = RetrofitClient.getClient(context).signUp(request)
                                        onSignupSuccess()
                                        onDismiss()
                                    } catch (e: Exception) {
                                        errorMessage = when (e) {
                                            is HttpException -> {
                                                val errorBody = e.response()?.errorBody()?.string()
                                                val errorMsg = try {
                                                    val json = Gson().fromJson(errorBody, JsonObject::class.java)
                                                    json["error"]?.asString ?: "Unknown error"
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
                            enabled = fullName.isNotBlank() && email.isNotBlank()
                                    && password.isNotBlank() && confirmPassword.isNotBlank()
                                    && !isLoading
                        ) {
                            Text("Create Account")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.Black)) {
                                append("Already have an account? ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = buttonColor,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Log in")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLoginClick() },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
