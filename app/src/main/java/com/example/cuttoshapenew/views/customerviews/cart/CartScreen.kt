package com.example.cuttoshapenew.views.customerviews.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cuttoshapenew.apiclients.CartItemResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController) {
    val viewModel: CartViewModel = hiltViewModel()
    val cartItems by viewModel.cartItems.collectAsState()
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val buttonColor = Color(0xFF4A90E2)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            viewModel.fetchCartItems()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Handle checkout logic */ },
                containerColor = buttonColor,
                modifier = Modifier
                    .width(120.dp)
                    .height(70.dp),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, // Center the content horizontally
                    verticalArrangement = Arrangement.Center, // Center vertically within the column
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Check out",
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp)) // Vertical spacing between icon and text
                    Text("Checkout", fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(errorMessage ?: "Unknown error", color = Color.Red, fontSize = 16.sp)
                }
            } else if (cartItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your cart is empty", fontSize = 18.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemCard(item = item, buttonColor = buttonColor, onDelete = { selectedItem ->
                            scope.launch {
                                viewModel.removeCartItem(selectedItem)
                            }
                        })
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total: $${cartItems.sumOf { it.cost }}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}