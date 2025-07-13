package com.example.cuttoshapenew.views.tailorviews

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.text.toIntOrNull
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import com.example.cuttoshapenew.apiclients.ProductRequest
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.model.Product
import com.example.cuttoshapenew.utils.DataStoreManager
import android.util.Log
import androidx.compose.material.icons.filled.PlayArrow
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductsScreen(navController: NavController, onAddNewProductClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    val buttonColor = Color(0xFF0089FA)
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch products when the screen is composed
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            try {
                val userId = DataStoreManager.getUserId(context).first() // Get current user ID
                val request = ProductRequest(
                    filter = listOf(mapOf(
                        "field" to "createdBy",
                        "value" to userId,
                        "condition_type" to "eq" // Equals condition
                    )),
                    page = 1,
                    limit = 360
                )
                val response = RetrofitClient.getClient(context).getProducts(request)

                // Filter products by createdBy client-side if server filter doesn't work
                products = response.products.filter {
                    it.createdBy == userId?.toIntOrNull()
                }

            } catch (e: Exception) {
                errorMessage = "Failed to load products: ${e.message}"
                Log.e("MyProductsScreen", "Error: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(

        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = buttonColor,
                modifier = Modifier
                    .width(170.dp)
                    .height(50.dp),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("+ New Product", fontSize = 16.sp)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage!!, color = Color.Red)
                }
            } else if (products.isEmpty()) {
                Text(
                    "No products available yet.",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        ProductCard(product = product, navController = navController)
                    }
                }
            }
        }
    }

    if (showDialog) {
        CreateProductDialog(
            onDismiss = { showDialog = false },
            onSave = { productName, priceRange, description, gender, images, styles, colors, fabrics ->
                scope.launch {
                    Toast.makeText(context, "Saved: $productName, $priceRange, $description, $gender, Images: $images, Styles: $styles, Colors: $colors, Fabrics: $fabrics", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun ProductCard(product: Product, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("productDetail/${product.id}") } // Navigate to detail screen
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            val imageUrl = product.documents.firstOrNull()?.url
            imageUrl?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
            } ?: Image(
                painter = rememberAsyncImagePainter("https://via.placeholder.com/80"), // Placeholder if no image
                contentDescription = "Placeholder Image",
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Product Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${product.lowPrice}.00",
                    fontSize = 16.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "by ${product.business.owner?.firstName ?: "Unknown"}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "View Details",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}