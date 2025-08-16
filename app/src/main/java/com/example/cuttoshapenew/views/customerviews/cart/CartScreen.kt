package com.example.cuttoshapenew.views.customerviews.cart

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.cuttoshapenew.apiclients.PaymentIntentRequest
import com.example.cuttoshapenew.apiclients.ShipAddressRequest
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.example.cuttoshapenew.utils.DataStoreManager
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController) {
    val viewModel: CartViewModel = hiltViewModel()
    val cartItems by viewModel.cartItems.collectAsState()
    val paymentIntent by viewModel.paymentIntent.collectAsState()
    val context = LocalContext.current
    val shipAddresses by viewModel.shipAddresses.collectAsState()
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val buttonColor = Color(0xFF4A90E2)
    val scope = rememberCoroutineScope()
    var shipOption by remember { mutableStateOf("pickup") }
    var showCheckout by remember { mutableStateOf(false) }
    var checkoutSheetState = rememberModalBottomSheetState()
    var showPaymentModal by remember { mutableStateOf(false) }
    val isPaymentSuccessful = remember { mutableStateOf(false) }
    var paymentSheetState = rememberModalBottomSheetState()
    var showNewShipModal by remember { mutableStateOf(false) }
    var newShipSheetState = rememberModalBottomSheetState()
    var userId by remember { mutableStateOf<String?>(null) }
    //val paymentSheet = remember { PaymentSheet.Builder(::onPaymentSheetResult) }.build()
    val paymentIntents = PaymentIntentRequest(
        cartItems = cartItems,
        userId = userId.toString(),
        shipping_options = "SELF"
    )
    val paymentSheet = rememberPaymentSheet { result ->
        viewModel.onPaymentSheetResult(result, paymentIntents){
            showCheckout = false
        }
    }
    var customerConfig by remember { mutableStateOf<PaymentSheet.CustomerConfiguration?>(null) }
    var paymentIntentClientSecret by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            viewModel.fetchCartItems()
            viewModel.fetchShipAddress()
            userId = DataStoreManager.getUserId(context).first()
            val paymentIntents = PaymentIntentRequest(
                cartItems = cartItems,
                userId = userId.toString(),
                shipping_options = "SELF"
            )
            viewModel.createPaymentIntent(paymentIntents)
        }
    }

    // Configure Stripe only when paymentIntent is available
    LaunchedEffect(paymentIntent) {
        paymentIntent?.let { intent ->
            Log.d("Pay", intent.toString()) // Debug log
            paymentIntentClientSecret = intent.paymentIntent
            customerConfig = PaymentSheet.CustomerConfiguration(
                id = intent.customer.toString(),
                ephemeralKeySecret = intent.ephemeralKey.toString()
            )
            PaymentConfiguration.init(context, intent.publishableKey.toString())
        } ?: run {
            Log.e("StripeConfig", "Payment intent is null")
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCheckout = true },
                containerColor = buttonColor,
                modifier = Modifier
                    .width(120.dp)
                    .height(70.dp),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Check out",
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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

    // Checkout Modal
    if (showCheckout) {
        ModalBottomSheet(
            onDismissRequest = { showCheckout = false },
            sheetState = checkoutSheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Header
                Text(
                    text = "Checkout",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Order Summary
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 80.dp, vertical = 16.dp)
                        .background(Color(0xFFE7E7EA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Items",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp, start = 20.dp).wrapContentWidth(Alignment.CenterHorizontally)
                        )
                        cartItems.forEach { item ->
                            Text(
                                text = "- item: $${item.cost}",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(bottom = 4.dp, start = 20.dp).wrapContentWidth(Alignment.CenterHorizontally),
                                color = Color.Green
                            )
                        }
                        Text(
                            text = "Total: $${cartItems.sumOf { it.cost }}",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp, start = 20.dp).wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }

                // Shipping Option
                Text(
                    text = "Shipping Option",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().background(color = Color(0xFFF1EFF1)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { shipOption = "pickup" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (shipOption == "pickup") Color.White else Color.LightGray),
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text("Pickup", color = Color.Black)
                    }
                    Button(
                        onClick = { shipOption = "tailorship" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (shipOption == "tailorship") Color.White else Color.LightGray),
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text("Tailor Shipment", color = Color.Black)
                    }
                }

                when (shipOption) {
                    "pickup" -> {
                        Row {
                            Button(
                                onClick = {

                                    val currentConfig = customerConfig
                                    val currentClientSecret = paymentIntentClientSecret
                                    Log.d("Rest", customerConfig.toString())
                                    if (currentConfig != null && currentClientSecret != null) {
                                        presentPaymentSheet(paymentSheet, currentConfig, currentClientSecret)
                                    }


                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF945CDF)),
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 32.dp).fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                            ) {
                                Text("Pay", color = Color.White)
                            }
                        }
                    }
                    "tailorship" -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { showPaymentModal = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    shape = RoundedCornerShape(4.dp),
                                ) {
                                    Text("Cancel", color = buttonColor, fontSize = 15.sp)
                                }

                                Button(
                                    onClick = { showNewShipModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    shape = RoundedCornerShape(4.dp),
                                ) {
                                    Text("Add New", color = buttonColor, fontSize = 15.sp)
                                }
                            }
                            var selectedShip by remember { mutableStateOf("") }
                            shipAddresses.forEach { item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { selectedShip = item.id }
                                ) {
                                    RadioButton(
                                        selected = (selectedShip == item.id),
                                        onClick = { selectedShip = item.id },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = buttonColor,
                                            unselectedColor = Color.Gray
                                        )
                                    )
                                    Text("${item.address}, ${item.city}, ${item.state}, ${item.zipCode}, ${item.country}", fontSize = 16.sp)
                                }
                            }

                            Row (modifier = Modifier.padding(top = 50.dp)){
                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF007BFF),
                                        disabledContainerColor = Color(0xFF007BFF)
                                    ),
                                    modifier = Modifier.weight(1f).padding(horizontal = 50.dp).fillMaxWidth(),
                                    shape = RoundedCornerShape(4.dp),
                                    enabled = false
                                ) {
                                    Text(
                                        "Submit",
                                        color = Color.White,
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Payment Modal
    if (showPaymentModal) {
        ModalBottomSheet(
            onDismissRequest = { showPaymentModal = false },
            sheetState = paymentSheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Test", color = Color(0xFF1671B4), fontSize = 16.sp, modifier = Modifier.padding(16.dp).background(Color(0xFFFFDE91)))
                    Button(
                        onClick = { showPaymentModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text("X", color = Color.LightGray, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row {
                    Button(
                        onClick = { showPaymentModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01D66F)),
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text("Pay with Link", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(25.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "Or pay using",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Divider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }

                Text(
                    text = "Card Information",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(0.dp)
                )

                // Card Number
                var cardNumber by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card Number") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color(0xFF4A90E2),
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color(0xFF4A90E2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                var expiryDate by remember { mutableStateOf("") }
                var cvv by remember { mutableStateOf("") }
                Row {
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("MM/YY") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color(0xFF4A90E2),
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.Black,
                            focusedIndicatorColor = Color(0xFF4A90E2),
                            unfocusedIndicatorColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )

                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { cvv = it },
                        label = { Text("CVV") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color(0xFF4A90E2),
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.Black,
                            focusedIndicatorColor = Color(0xFF4A90E2),
                            unfocusedIndicatorColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
                }

                // Country Dropdown
                var isExpanded by remember { mutableStateOf(false) }
                var selectedCountry by remember { mutableStateOf("United States") }
                val countries = listOf(
                    "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua and Barbuda", "Argentina", "Armenia", "Australia", "Austria",
                    "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bhutan",
                    "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei", "Bulgaria", "Burkina Faso", "Burundi", "Cabo Verde",
                    "Cambodia", "Cameroon", "Canada", "Central African Republic", "Chad", "Chile", "China", "Colombia", "Comoros",
                    "Congo (Brazzaville)", "Congo (Kinshasa)", "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czechia", "Denmark", "Djibouti",
                    "Dominica", "Dominican Republic", "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea",
                    "Estonia", "Eswatini", "Ethiopia", "Fiji", "Finland", "France", "Gabon", "Gambia", "Georgia", "Germany",
                    "Ghana", "Greece", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Honduras", "Hungary",
                    "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan",
                    "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea (North)", "Korea (South)", "Kosovo", "Kuwait", "Kyrgyzstan",
                    "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Madagascar",
                    "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico",
                    "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique", "Myanmar", "Namibia",
                    "Nauru", "Nepal", "Netherlands", "New Zealand", "Nicaragua", "Niger", "Nigeria", "North Macedonia", "Norway",
                    "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland",
                    "Portugal", "Qatar", "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines",
                    "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone",
                    "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Sudan", "Spain",
                    "Sri Lanka", "Sudan", "Suriname", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania",
                    "Thailand", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Tuvalu", "Uganda",
                    "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "Uzbekistan", "Vanuatu", "Vatican City",
                    "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe"
                )
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    OutlinedTextField(
                        value = selectedCountry,
                        onValueChange = { selectedCountry = it },
                        label = { Text("Country") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color(0xFF4A90E2),
                            unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                            cursorColor = Color.Black,
                            focusedIndicatorColor = Color(0xFF4A90E2),
                            unfocusedIndicatorColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        countries.forEach { country ->
                            DropdownMenuItem(
                                text = { Text(country) },
                                onClick = {
                                    selectedCountry = country
                                    isExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                var zip by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = zip,
                    onValueChange = { zip = it },
                    label = { Text("ZIP") },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color(0xFF4A90E2),
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color(0xFF4A90E2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                // Confirm Payment Button
                Row {
                    Button(
                        onClick = {
                            scope.launch {
                                // Add payment processing logic here (e.g., API call)
                                if (cardNumber.isNotBlank() && expiryDate.isNotBlank() && cvv.isNotBlank() && zip.isNotBlank()) {
                                    // Simulate payment success
                                    // viewModel.clearCart // Clear cart after successful payment
                                    showPaymentModal = false
                                    showCheckout = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007BFF),
                            disabledContainerColor = Color(0xFF007BFF)
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        enabled = cardNumber.isNotBlank() && expiryDate.isNotBlank() && cvv.isNotBlank() && zip.isNotBlank()
                    ) {
                        Text(
                            "Pay $${cartItems.sumOf { it.cost }}",
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }

    // Payment Modal
    if (showNewShipModal) {
        ModalBottomSheet(
            onDismissRequest = { showNewShipModal = false },
            sheetState = newShipSheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showNewShipModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text("Cancel", color = buttonColor, fontSize = 15.sp)
                    }
                }


                // Card Number
                var addres by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = addres,
                    onValueChange = { addres = it },
                    label = { Text("Address") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color(0xFF4A90E2),
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color(0xFF4A90E2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                var city by remember { mutableStateOf("") }
                var state by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = { Text("City") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color(0xFF4A90E2),
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color(0xFF4A90E2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = { Text("State") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color(0xFF4A90E2),
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color(0xFF4A90E2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(4.dp)
                )


                var zip by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = zip,
                    onValueChange = { zip = it },
                    label = { Text("ZIP") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color(0xFF4A90E2),
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color(0xFF4A90E2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                var country by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color(0xFF4A90E2),
                        unfocusedLabelColor = Color.Black.copy(alpha = 0.5f),
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color(0xFF4A90E2),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                // Confirm Payment Button
                Row {
                    Button(
                        onClick = {
                            scope.launch {
                                // Add payment processing logic here (e.g., API call)
                                val userId = DataStoreManager.getUserId(context).first()
                                if (addres.isNotBlank() && city.isNotBlank() && state.isNotBlank() && zip.isNotBlank() && country.isNotBlank()) {
                                    // Simulate payment success
                                    var shipAddress = ShipAddressRequest(
                                        address = addres,
                                        city = city,
                                        state = state,
                                        zipCode = zip,
                                        country = country,
                                        userId = userId.toString()
                                    )

                                    viewModel.createShipAddress(shipAddress)

                                    // viewModel.clearCart // Clear cart after successful payment
                                    showNewShipModal = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007BFF),
                            disabledContainerColor = Color(0xFF007BFF)
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 70.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        enabled = addres.isNotBlank() && city.isNotBlank() && state.isNotBlank() && zip.isNotBlank() && country.isNotBlank()
                    ) {
                        Text(
                            "Create",
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

//private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
//    when(paymentSheetResult) {
//        is PaymentSheetResult.Canceled -> {
//            print("Canceled")
//        }
//        is PaymentSheetResult.Failed -> {
//            print("Error: ${paymentSheetResult.error}")
//        }
//        is PaymentSheetResult.Completed -> {
//            // Display for example, an order confirmation screen
//            print("Completed")
//        }
//    }
//}

private fun presentPaymentSheet(
    paymentSheet: PaymentSheet,
    customerConfig: PaymentSheet.CustomerConfiguration,
    paymentIntentClientSecret: String
) {
    paymentSheet.presentWithPaymentIntent(
        paymentIntentClientSecret,
        PaymentSheet.Configuration.Builder(merchantDisplayName = "My merchant name")
            .customer(customerConfig)
            // Set `allowsDelayedPaymentMethods` to true if your business handles
            // delayed notification payment methods like US bank accounts.
            .allowsDelayedPaymentMethods(true)
            .build()
    )
}