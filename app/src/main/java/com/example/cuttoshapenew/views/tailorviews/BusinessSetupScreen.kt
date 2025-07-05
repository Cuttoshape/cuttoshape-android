package com.example.cuttoshapenew.views.tailorviews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.cuttoshapenew.apiclients.ApiService
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.apiclients.SignUpRequest
import com.example.cuttoshapenew.utils.DataStoreManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import retrofit2.HttpException
@Composable
fun BusinessRegistrationScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Business Registration",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth() // 👈 This is the key part
                .padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        RegistrationStep(
            stepNumber = 1,
            title = "User Profile",
            description = "Set up your personal details and login information.",
            buttonText = "User Profile",
            onClick = { navController.navigate("userProfile") }
        )

        RegistrationStep(
            stepNumber = 2,
            title = "Business Profile",
            description = "Fill in your business details including name, address, and industry.",
            buttonText = "Business Profile",
            onClick = { navController.navigate("business_profile") }
        )

        RegistrationStep(
            stepNumber = 3,
            title = "Strip Account",
            description = "Connect your strip account for payment processing.",
            buttonText = "Strip Account",
            onClick = { /* TODO: Navigate to Stripe account setup */ }
        )

        RegistrationStep(
            stepNumber = 4,
            title = "Account Review",
            description = "Our team would review your account.",
            showButton = false
        )
    }
}

@Composable
fun RegistrationStep(
    stepNumber: Int,
    title: String,
    description: String,
    buttonText: String = "",
    showButton: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color(0xFF0089FA), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            if (showButton) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0089FA), // background color
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = buttonText)
                }
            }
        }
    }
}
