package com.example.cuttoshapenew.views.customerviews.product

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.cuttoshapenew.apiclients.CartItemRequest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cuttoshapenew.apiclients.MeasurementResponse
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.utils.DataStoreManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: Int?, navController: NavController) {
    val viewModel: ProductDetailViewModel = viewModel()
    val product by viewModel.product.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState(null)
    val context = LocalContext.current
    val buttonColor = Color(0xFF4A90E2)
    var cartErrorMessage by remember { mutableStateOf<String?>(null) }
    var isLoadingCart by remember { mutableStateOf(false) }

    // State to track the selected image URL
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    // State to control description bottom sheet visibility
    var showDescriptionSheet by remember { mutableStateOf(false) }
    val descriptionSheetState = rememberModalBottomSheetState()
    var isLoadingMeasurement by remember { mutableStateOf(true) }
    var errorMessageMeasurement by remember { mutableStateOf<String?>(null) }
    // State to control add to cart bottom sheet visibility
    var showAddToCartSheet by remember { mutableStateOf(false) }
    val addToCartSheetState = rememberModalBottomSheetState()

    // States for selected options in the Add to Cart modal
    var selectedColor by remember { mutableStateOf<String?>(null) }
    var selectedFabric by remember { mutableStateOf<String?>(null) }
    var selectedDesign by remember { mutableStateOf<String?>(null) }
    var selectedUser by remember { mutableStateOf<String?>(null) }
    var measurements by remember { mutableStateOf<List<MeasurementResponse>>(emptyList()) }
    // State for submit result
    var submitResult by remember { mutableStateOf<String?>(null) }
    var userId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // HTTP Client setup
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    // Set the initial selected image when the product is loaded
    LaunchedEffect(product) {
        if (product != null && selectedImageUrl == null) {
            selectedImageUrl = product!!.documents.firstOrNull()?.url
        }
    }

    // Fetch product when the screen is composed
    LaunchedEffect(productId) {
        viewModel.fetchProduct(productId, context)
    }

    LaunchedEffect(Unit) {
        userId = DataStoreManager.getUserId(context).first()
        try {
            measurements = RetrofitClient.getClient(context).getMeasurements(userId.toString())
        } catch (e: Exception) {
            errorMessageMeasurement = "Failed to load measurements: ${e.message}"
        } finally {
            isLoadingMeasurement = false
        }
    }


    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = errorMessage ?: "", color = Color.Red, fontSize = 16.sp)
                    }
                }

                product != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Main Product Image using selectedImageUrl
                        selectedImageUrl?.let {
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
                                        .clickable { selectedImageUrl = doc.url }
                                        .then(
                                            if (doc.url == selectedImageUrl) {
                                                Modifier.border(
                                                    2.dp,
                                                    Color.Blue,
                                                    RoundedCornerShape(4.dp)
                                                )
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Action Buttons
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(50.dp))
                                    .border(
                                        BorderStroke(1.dp, Color.LightGray),
                                        shape = RoundedCornerShape(50.dp)
                                    ),
                                color = Color.White
                            ) {
                                NavigationBar(containerColor = Color.White) {
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Description",
                                                tint = buttonColor
                                            )
                                        },
                                        label = { Text("Description", color = buttonColor) },
                                        selected = false,
                                        onClick = { showDescriptionSheet = true }
                                    )
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingCart,
                                                contentDescription = "Add to Cart",
                                                tint = buttonColor
                                            )
                                        },
                                        label = { Text("Add to Cart", color = buttonColor) },
                                        selected = false,
                                        onClick = { showAddToCartSheet = true }
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
                    }
                }
            }
        }
    }

    // Description Bottom Sheet
    if (showDescriptionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDescriptionSheet = false },
            sheetState = descriptionSheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 16.dp)
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp, top = 0.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    TextButton(onClick = { scope.launch { descriptionSheetState.hide(); showDescriptionSheet = false } }) {
                        Text("Close", color = buttonColor, fontSize = 20.sp)
                    }
                }

                // Description title
                Text(
                    text = product?.name ?: "",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                // Description content
                Text(
                    text = "$${product?.lowPrice ?: "No price available"} - $${product?.highPrice ?: "No description available"}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = Color.Green,
                    fontSize = 25.sp
                )

                // Specifications
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Specifications",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Manufacturer: ${product?.business?.name ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Colors: ${
                        product?.options?.filter { it.name == "COLOR" }
                            ?.joinToString(", ") { it.value } ?: "Not specified"
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = "Designs: ${
                        product?.options?.filter { it.name == "DESIGN" }
                            ?.joinToString(", ") { it.value } ?: "Not specified"
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = "Fabrics: ${
                        product?.options?.filter { it.name == "FABRIC" }
                            ?.joinToString(", ") { it.value } ?: "Not specified"
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }

    // Add to Cart Bottom Sheet
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
            ) {
                // Title
                Text(
                    text = "Add ${product?.name ?: "Product"} to Cart",
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
                val colors = product?.options?.filter { it.name == "COLOR" }?.map { it.value } ?: emptyList()
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
                val fabrics = product?.options?.filter { it.name == "FABRIC" }?.map { it.value } ?: emptyList()
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
                val designs = product?.options?.filter { it.name == "DESIGN" }?.map { it.value } ?: emptyList()
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

                // User Measurement
                Text(
                    text = "User",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                )

                var userExpanded by remember { mutableStateOf(false) }
                if (measurements.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = userExpanded,
                        onExpandedChange = { userExpanded = !userExpanded }
                    ) {
                        TextField(
                            value = selectedUser ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userExpanded) },
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
                            expanded = userExpanded,
                            onDismissRequest = { userExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .clip(RoundedCornerShape(45.dp))
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            measurements.forEachIndexed { index, user ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = user.name,
                                                color = Color.Black,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                            )
                                        },
                                        onClick = {
                                            selectedUser = user.id.toString()
                                            userExpanded = false
                                        }
                                    )
                                    if (index < measurements.size - 1) {
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
                                    productId = productId ?: 8, // Use productId or default to 8
                                    userDataId = selectedUser ?: "", // Hardcoded as per example, replace with dynamic value if needed
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
                                                val json = Gson().fromJson(errorBody, JsonObject::class.java)
                                                json["message"]?.asString ?: "Unknown error"
                                            } catch (parseException: Exception) {
                                                "Unexpected error format"
                                            }
                                            "Add Cart failed: $errorMsg"
                                        }
                                        else -> "Add Cart failed: ${e.localizedMessage ?: "Unknown error"}"
                                    }
                                    Log.e("Add Cart", "Error: $e", e)
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