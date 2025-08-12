package com.example.cuttoshapenew.views.customerviews.cart

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cuttoshapenew.apiclients.CartItemRequest
import com.example.cuttoshapenew.apiclients.CartItemResponse
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.utils.DataStoreManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartItemCard(item: CartItemResponse, buttonColor: Color, onDelete: (CartItemResponse) -> Unit) {
    var showAddToCartSheet by remember { mutableStateOf(false) }
    val addToCartSheetState = rememberModalBottomSheetState()
    var selectedColor by remember { mutableStateOf<String?>(null) }
    var selectedFabric by remember { mutableStateOf<String?>(null) }
    var selectedDesign by remember { mutableStateOf<String?>(null) }

    var cartErrorMessage by remember { mutableStateOf<String?>(null) }
    var isLoadingCart by remember { mutableStateOf(false) }

    var submitResult by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = item.product?.documents?.firstOrNull()?.url
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
                painter = rememberAsyncImagePainter("https://via.placeholder.com/80"),
                contentDescription = "Placeholder Image",
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product?.name ?: "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.userData?.name ?: "Unknown",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text("Cost: $${item.cost}", fontWeight = FontWeight.Bold, color = Color(0xFF2EA24C))

                Row(modifier = Modifier.padding(6.dp)){
                    Text("${item.configurations.color}, ${item.configurations.design}, ${item.configurations.fabric}")
                }

                Row() {
                    Button(
                        onClick = { onDelete(item) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEECED)),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Item",
                            tint = Color(0xFFDF4055),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = Color(0xFFDF4055), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(22.dp))
                    Button(
                        onClick = { showAddToCartSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9F3FF)),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = "Edit Item",
                            tint = Color(0xFF1671B4),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", color = Color(0xFF1671B4), fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showAddToCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddToCartSheet = false },
            sheetState = addToCartSheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp)
            ) {
                // Title
                Text(
                    text = "Edit ${item.product?.name ?: "Product"} in Cart",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )

                // Color Selection
                Text(
                    text = "Color",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                )
                val colors = item.product?.options?.filter { it.name == "COLOR" }?.map { it.value }
                    ?: emptyList()
                var colorExpanded by remember { mutableStateOf(false) }
                if (colors.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = colorExpanded,
                        onExpandedChange = { colorExpanded = !colorExpanded }
                    ) {
                        TextField(
                            value = selectedColor ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = colorExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .padding(bottom = 8.dp, start = 16.dp, end = 16.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F4F9),
                                unfocusedContainerColor = Color(0xFFF5F4F9),
                                focusedTextColor = buttonColor,
                                unfocusedTextColor = buttonColor,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                                cursorColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = colorExpanded,
                            onDismissRequest = { colorExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .clip(RoundedCornerShape(45.dp))
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            colors.forEachIndexed { index, color ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = color,
                                                color = Color.Black,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                            )
                                        },
                                        onClick = {
                                            selectedColor = color
                                            colorExpanded = false
                                        }
                                    )
                                    if (index < colors.size - 1) {
                                        Divider(
                                            color = Color.LightGray,
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Fabric Selection
                Text(
                    text = "Fabric",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                )
                val fabrics =
                    item.product?.options?.filter { it.name == "FABRIC" }?.map { it.value }
                        ?: emptyList()
                var fabricExpanded by remember { mutableStateOf(false) }
                if (fabrics.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = fabricExpanded,
                        onExpandedChange = { fabricExpanded = !fabricExpanded }
                    ) {
                        TextField(
                            value = selectedFabric ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fabricExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F4F9),
                                unfocusedContainerColor = Color(0xFFF5F4F9),
                                focusedTextColor = buttonColor,
                                unfocusedTextColor = buttonColor,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                                cursorColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = fabricExpanded,
                            onDismissRequest = { fabricExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .clip(RoundedCornerShape(45.dp))
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            fabrics.forEachIndexed { index, fabric ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = fabric,
                                                color = Color.Black,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                            )
                                        },
                                        onClick = {
                                            selectedFabric = fabric
                                            fabricExpanded = false
                                        }
                                    )
                                    if (index < fabrics.size - 1) {
                                        Divider(
                                            color = Color.LightGray,
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Design Selection
                Text(
                    text = "Design",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                )
                val designs =
                    item.product?.options?.filter { it.name == "DESIGN" }?.map { it.value }
                        ?: emptyList()
                var designExpanded by remember { mutableStateOf(false) }
                if (designs.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = designExpanded,
                        onExpandedChange = { designExpanded = !designExpanded }
                    ) {
                        TextField(
                            value = selectedDesign ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = designExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F4F9),
                                unfocusedContainerColor = Color(0xFFF5F4F9),
                                focusedTextColor = buttonColor,
                                unfocusedTextColor = buttonColor,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                                cursorColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = designExpanded,
                            onDismissRequest = { designExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .clip(RoundedCornerShape(45.dp))
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            designs.forEachIndexed { index, design ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = design,
                                                color = Color.Black,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                            )
                                        },
                                        onClick = {
                                            selectedDesign = design
                                            designExpanded = false
                                        }
                                    )
                                    if (index < designs.size - 1) {
                                        Divider(
                                            color = Color.LightGray,
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { showAddToCartSheet = false },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Cancel", color = Color.Red, fontSize = 16.sp)
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                isLoadingCart = true
                                cartErrorMessage = null

                                val userId = DataStoreManager.getUserId(context).first()

                                val payload = CartItemRequest(
                                    configurations = mapOf(
                                        "color" to (selectedColor ?: ""),
                                        "design" to (selectedDesign ?: ""),
                                        "fabric" to (selectedFabric ?: "")
                                    ),
                                    color = selectedColor ?: "",
                                    design = selectedDesign ?: "",
                                    fabric = selectedFabric ?: "",
                                    cost = 120, // Hardcoded as per example, replace with dynamic value if needed
                                    productId = item.productId, // Use productId or default to 8
                                    userDataId = "3", // Hardcoded as per example, replace with dynamic value if needed
                                    userId = userId?.toIntOrNull()
                                )
                                try {
                                    val response = RetrofitClient.getClient(context).addToCart(
                                        userId?.toIntOrNull(),
                                        payload
                                    )
                                    Log.d("response Cart", response.toString())
                                    if (response.success == "true") {
                                        submitResult = "Item added to cart successfully!"
                                        showAddToCartSheet = false
                                    } else {
                                        submitResult = "Failed to add item to cart."
                                    }
                                } catch (e: Exception) {
                                    cartErrorMessage = when (e) {
                                        is HttpException -> {
                                            val errorBody = e.response()?.errorBody()?.string()
                                            val errorMsg = try {
                                                val json = Gson().fromJson(
                                                    errorBody,
                                                    JsonObject::class.java
                                                )
                                                json["message"]?.asString ?: "Unknown error"
                                            } catch (parseException: Exception) {
                                                "Unexpected error format"
                                            }
                                            "Add Cart failed: $errorMsg"
                                        }

                                        else -> "Add Cart failed: ${e.localizedMessage ?: "Unknown error"}"
                                    }
                                    Log.e("Add Cart", "Error: ${e.message}", e)
                                } finally {
                                    isLoadingCart = false
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        Text("Submit", color = Color.White, fontSize = 16.sp)
                    }
                }

                // Display submit result
                submitResult?.let {
                    Text(
                        text = it,
                        color = if (it.contains("successfully")) Color.Green else Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    )
                }
            }
        }

    }
}