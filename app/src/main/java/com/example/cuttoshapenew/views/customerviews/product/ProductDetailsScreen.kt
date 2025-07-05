package com.example.cuttoshapenew.views.customerviews.product

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cuttoshapenew.model.Product
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip

@Composable
fun ProductDetailScreen(productId: Int?, navController: NavController) {
    val viewModel: ProductDetailViewModel = viewModel()
    val product by viewModel.product.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState(null) // Directly observe the delegated property
    val context = LocalContext.current // Access context within composable
    val buttonColor = Color(0xFF4A90E2)


    // Fetch product when the screen is composed
    LaunchedEffect(productId) {
        viewModel.fetchProduct(productId, context)
    }

    Scaffold(

    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = errorMessage ?: "", color = Color.Red, fontSize = 16.sp)
                    }
                }
                product != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Main Product Image
                        val mainImageUrl = product?.documents?.firstOrNull()?.url
                        mainImageUrl?.let {
                            Image(
                                painter = rememberAsyncImagePainter(it),
                                contentDescription = "Main Product Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(450.dp),
                                contentScale = ContentScale.FillWidth
                            )
                        } ?: Image(
                            painter = rememberAsyncImagePainter("https://via.placeholder.com/300"),
                            contentDescription = "Placeholder Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(450.dp)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Fit
                        )

                        // Thumbnail Images
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(product?.documents ?: emptyList()) { doc ->
                                Image(
                                    painter = rememberAsyncImagePainter(doc.url),
                                    contentDescription = "Thumbnail Image",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(Color.LightGray)
                                        .clickable {
                                            // Optionally switch main image on click
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Action Buttons
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp) // Optional: Add padding around the container
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(50.dp)) // Rounds the corners
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            Color.LightGray
                                        ), // Outline thickness and color
                                        shape = RoundedCornerShape(50.dp) // Matches the clip shape
                                    ),
                                color = Color.White // Background color (matches your containerColor)
                            ) {
                                NavigationBar(
                                    containerColor = Color.White
                                ) {
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Description",
                                                tint = buttonColor
                                            )
                                        },
                                        label = { Text("Description", color = buttonColor) },
                                        selected = true,
                                        onClick = { navController.navigate("marketplace") }
                                    )
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingCart,
                                                contentDescription = "Cart",
                                                tint = buttonColor
                                            )
                                        },
                                        label = { Text("Add to Cart", color = buttonColor) },
                                        selected = false,
                                        onClick = { navController.navigate("quotation") }
                                    )
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Email,
                                                contentDescription = "Conversation",
                                                tint = buttonColor
                                            )
                                        },
                                        label = { Text("Conversation", color = buttonColor) },
                                        selected = false,
                                        onClick = { navController.navigate("scan") }
                                    )


                                }
                            }
                        }

//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp),
//                            horizontalArrangement = Arrangement.SpaceEvenly
//                        ) {
//                            Button(
//                                onClick = { /* Handle Description */ },
//                                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
//                            ) {
//                                Icon(Icons.Default.Info, contentDescription = "Description", tint = Color.White)
//                                Spacer(modifier = Modifier.width(4.dp))
//                                Text("Description", color = Color.White)
//                            }
//                            Button(
//                                onClick = { /* Handle Add to Cart */ },
//                                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
//                            ) {
//                                Icon(Icons.Default.ShoppingCart, contentDescription = "Add to Cart", tint = Color.White)
//                                Spacer(modifier = Modifier.width(4.dp))
//                                Text("Add to Cart", color = Color.White)
//                            }
//                            Button(
//                                onClick = { /* Handle Conversation */ },
//                                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
//                            ) {
//                                Icon(Icons.Default.AddCircle, contentDescription = "Conversation", tint = Color.White)
//                                Spacer(modifier = Modifier.width(4.dp))
//                                Text("Conversation", color = Color.White)
//                            }
//                        }
                    }
                }
            }
        }
    }
}