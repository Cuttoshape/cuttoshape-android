package com.example.cuttoshapenew.views.tailorviews

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import okhttp3.MultipartBody
import com.example.cuttoshapenew.apiclients.Product
import com.example.cuttoshapenew.apiclients.ProductOption
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import retrofit2.HttpException
import android.util.Log
import okhttp3.RequestBody.Companion.toRequestBody // Add this import
import android.net.Uri
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductDialog(
    onDismiss: () -> Unit,
    onSave: (String, Pair<Double, Double>, String, String, List<String>, List<Pair<String, String>>, List<Pair<String, String>>, List<Pair<String, String>>) -> Unit
) {
    val context = LocalContext.current
    var productName by remember { mutableStateOf("") }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("male") }
    var images by remember { mutableStateOf(listOf<String>()) }
    var styles by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var newStyle by remember { mutableStateOf("") }
    var styleCost by remember { mutableStateOf("") }
    var colors by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var newColor by remember { mutableStateOf("") }
    var colorCost by remember { mutableStateOf("") }
    var fabrics by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var newFabric by remember { mutableStateOf("") }
    var fabricCost by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                if (images.size < 6) {
                    images = images + uri.toString()
                }
            }
        }
    }

    fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        launcher.launch(intent)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5F4F9)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("PRODUCT DETAILS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Text("Product Name", fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                TextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                    )

                )
                Text("Price Range", fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = minPrice,
                        onValueChange = { minPrice = it },
                        label = { Text("Low Price") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        )

                    )
                    Text("-", fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterVertically))
                    TextField(
                        value = maxPrice,
                        onValueChange = { maxPrice = it },
                        label = { Text("High Price") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                }
                Text("Description", fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                TextField(
                    value = description,
                    onValueChange = { description = it.take(50) },
                    label = { Text("Description (max 50 chars)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                    ),
                    trailingIcon = { Text("${description.length}/50", fontSize = 12.sp, color = Color.Gray) }
                )

                Text("GENDER", fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { gender = "male" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (gender == "male") Color.White else Color(0xFFF1EFF1)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Male", color = Color.Black)
                    }
                    Button(
                        onClick = { gender = "female" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (gender == "female") Color.White else Color(0xFFF1EFF1)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Female", color = Color.Black)
                    }
                    Button(
                        onClick = { gender = "unisex" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (gender == "unisex") Color.White else Color(0xFFF1EFF1)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Unisex", color = Color.Black)
                    }
                }

                Text("PRODUCT IMAGES (MAX 6)", fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    images.forEach { imageUri ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(80.dp)
                                .padding(end = 8.dp)
                        )
                    }
                    if (images.size < 6) {
                        Button(
                            onClick = { pickImage() },
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEBF2FF)),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Image",
                                tint = Color(0xFF4A90E2),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Text("STYLES", fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                styles.forEachIndexed { index, (style, cost) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$style ($cost)", modifier = Modifier.weight(1f))
                        IconButton(onClick = { styles = styles.toMutableList().apply { removeAt(index) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Style", tint = Color.Red)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = newStyle,
                        onValueChange = { newStyle = it },
                        label = { Text("New Style") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                    TextField(
                        value = styleCost,
                        onValueChange = { styleCost = it },
                        label = { Text("Cost") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                    IconButton(onClick = { if (newStyle.isNotEmpty() && styleCost.isNotEmpty()) { styles = styles + Pair(newStyle, styleCost); newStyle = ""; styleCost = "" } }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Style", tint = Color(0xFF4A90E2))
                    }
                }

                Text("COLORS", fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                colors.forEachIndexed { index, (color, cost) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$color ($cost)", modifier = Modifier.weight(1f))
                        IconButton(onClick = { colors = colors.toMutableList().apply { removeAt(index) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Color", tint = Color.Red)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = newColor,
                        onValueChange = { newColor = it },
                        label = { Text("New Color") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                    TextField(
                        value = colorCost,
                        onValueChange = { colorCost = it },
                        label = { Text("Cost") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                    IconButton(onClick = { if (newColor.isNotEmpty() && colorCost.isNotEmpty()) { colors = colors + Pair(newColor, colorCost); newColor = ""; colorCost = "" } }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Color", tint = Color(0xFF4A90E2))
                    }
                }

                Text("FABRIC OPTIONS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                fabrics.forEachIndexed { index, (fabric, cost) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$fabric ($cost)", modifier = Modifier.weight(1f))
                        IconButton(onClick = { fabrics = fabrics.toMutableList().apply { removeAt(index) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Fabric", tint = Color.Red)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = newFabric,
                        onValueChange = { newFabric = it },
                        label = { Text("New Fabric") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        )

                    )
                    TextField(
                        value = fabricCost,
                        onValueChange = { fabricCost = it },
                        label = { Text("Cost") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                    IconButton(onClick = { if (newFabric.isNotEmpty() && fabricCost.isNotEmpty()) { fabrics = fabrics + Pair(newFabric, fabricCost); newFabric = ""; fabricCost = "" } }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Fabric", tint = Color(0xFF4A90E2))
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val min = minPrice.toDoubleOrNull() ?: 0.0
                                val max = maxPrice.toDoubleOrNull() ?: 0.0
                                val options = mutableListOf<ProductOption>().apply {
                                    styles.forEach { (value, cost) -> add(ProductOption("DESIGN", value, cost)) }
                                    colors.forEach { (value, cost) -> add(ProductOption("COLOR", value, cost)) }
                                    fabrics.forEach { (value, cost) -> add(ProductOption("FABRIC", value, cost)) }
                                }

                                // Create product object
                                val product = Product(
                                    name = productName,
                                    businessId = 2,
                                    productGenderType = gender,
                                    options = options,
                                    highPrice = max.toString(),
                                    lowPrice = min.toString(),
                                    createdBy = 3
                                )

                                // Convert product to JSON string
                                val gson = Gson()
                                val productJson = gson.toJson(product).toRequestBody("application/json".toMediaTypeOrNull())

                                // Convert image URIs to MultipartBody.Part
                                val imageParts = images.mapNotNull { imageUri ->
                                    try {
                                        val uri = Uri.parse(imageUri)
                                        val inputStream = context.contentResolver.openInputStream(uri)
                                        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
                                        inputStream?.use { input ->
                                            FileOutputStream(file).use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                        MultipartBody.Part.createFormData(
                                            "images[]",
                                            file.name,
                                            file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                                        )
                                    } catch (e: Exception) {
                                        Log.e("CreateProduct", "Error converting URI to file: ${e.message}")
                                        null
                                    }
                                }

                                // Log the payload for debugging
                                Log.d("CreateProduct", "Product JSON: ${gson.toJson(product)}")
                                Log.d("CreateProduct", "Image Parts Count: ${imageParts.size}")

                                // API call
                                val response = RetrofitClient.getClient(context).createProduct(
                                    productJson = productJson,
                                    images = imageParts
                                )
                                snackbarHostState.showSnackbar("Product created! Code: ${response.code}")
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
                                        "Product Creation failed: $errorMsg"
                                    }
                                    else -> "Product Creation failed: ${e.localizedMessage ?: "Unknown error"}"
                                }
                                Log.e("CreateProduct", "Error: ${e.message}", e)
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading
                ) {
                    Text("Save", fontSize = 18.sp, color = Color.White)
                }
                SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}