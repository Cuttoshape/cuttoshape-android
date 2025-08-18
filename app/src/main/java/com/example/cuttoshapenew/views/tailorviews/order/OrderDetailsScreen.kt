package com.example.cuttoshapenew.views.tailorviews.order

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cuttoshapenew.apiclients.Quotation
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.ui.window.Dialog
import com.example.cuttoshapenew.apiclients.UserData
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import com.example.cuttoshapenew.apiclients.UpdateQuotationRequest
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TailorOrderDetailsScreen(
    navController: NavController,
    orderId: Int,
    buyerId: Int,
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    val order = viewModel.order.observeAsState()
    val isLoading = viewModel.isLoading.observeAsState(false)
    val error = viewModel.error.observeAsState(null)
    val buttonColor = Color(0xFF4A90E2)
    val scope = rememberCoroutineScope()

    LaunchedEffect(orderId, buyerId) {
        viewModel.fetchOrderDetails(orderId, buyerId)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
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
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Update Cost",
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Update Cost", fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) {


            if (isLoading.value) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (error.value != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error.value!!, color = Color.Red, fontSize = 16.sp)
                }
            } else if (order.value != null) {

                // NEW: dialog visibility state
                var showAddressDialog by rememberSaveable { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5))
                        .padding(16.dp)
                ) {
                    Text(
                        text = order.value!!.orderNumber,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Date: ${formatOrderDate(order.value!!.createdAt)}",
                                fontSize = 16.sp
                            )
                            Text(
                                "Buyer: ${order.value!!.buyerName}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Status: ${order.value!!.status}",
                                fontSize = 16.sp,
                                color = Color(0xFF28A745)
                            )

                            // UPDATED: open the modal
                            Button(
                                onClick = { showAddressDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFAE51E6
                                    )
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text("Shipping Address", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            order.value!!.quotations.forEach { quotation ->
                                Text(
                                    "Item: $${quotation.cost}.00",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                "Shipping: $${order.value!!.quotations.sumOf { it.shippingCost }}.00",
                                fontSize = 14.sp,
                                color = Color(0xFF628AF1)
                            )
                            Text(
                                "Grand Total: $${order.value!!.quotations.sumOf { it.cost } + order.value!!.quotations.sumOf { it.shippingCost }}.00",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF28A745)
                            )
                        }
                    }

                    Text(
                        text = "Items (${order.value!!.itemCount})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    LazyColumn {
                        items(order.value!!.quotations) { q ->
                            OrderItemCard(
                                quotation = q,
                                onSubmitStatus = { newStatus ->
                                    scope.launch {
                                        val req = UpdateQuotationRequest(
                                            id = q.id.toString(),        // ensure String
                                            shippingDetails = "",        // per your requirement
                                            status = newStatus,
                                            trackingNumber = ""          // per your requirement
                                        )
                                        viewModel.updateQuotation(listOf(req))
                                    }
                                }
                            )
                        }
                    }
                }

                // NEW: the modal dialog
                if (showAddressDialog) {
                    // ⚠️ Replace the values below with your real fields from `order.value!!`
                    Log.d("The Ship", order.value?.shippingAddress.toString())
                    ShippingAddressDialog(
                        address = order.value!!.shippingAddress.addressLine1.toString(),
                        city = order.value!!.shippingAddress.city.toString(),
                        state = order.value!!.shippingAddress.state.toString(),
                        zip = order.value!!.shippingAddress.zipCode.toString(),
                        country = order.value!!.shippingAddress.country.toString(),
                        onDismiss = { showAddressDialog = false }
                    )
                }

            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No order details found", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(quotation: Quotation, onSubmitStatus: (String) -> Unit) {

    var showMeasurementDialog by rememberSaveable { mutableStateOf(false) }
    var showStatusDialog by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = quotation.productImage,
                contentDescription = "Product Image",
                modifier = Modifier.size(90.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quotation.productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Price: $${quotation.cost}.00",
                    fontSize = 14.sp,
                    color = Color(0xFF28A745)
                )
                Text(
                    text = "Config: ${quotation.configurations.values.joinToString(", ")}",
                    fontSize = 14.sp,
                    maxLines = 1, // Limit to one line
                    overflow = TextOverflow.Ellipsis // Use ellipsis for overflow
                )

                Button(
                    onClick = { showMeasurementDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAE51E6)),
//                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Measurement", color = Color.White, fontSize = 12.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quotation.status,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Button(
                    onClick = { showStatusDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAE51E6)),
                ) { Text("Status", color = Color.White, fontSize = 12.sp) }

                if (showStatusDialog) {
                    StatusDialog(
                        currentStatus = quotation.status,
                        onSubmit = { chosen ->
                            onSubmitStatus(chosen)        // send chosen status upward
                            showStatusDialog = false      // close after submit
                        },
                        onDismiss = { showStatusDialog = false }
                    )
                }

                if (showMeasurementDialog) {
                    MeasurementDialog(quotation.userData) { showMeasurementDialog = false }
                }
            }

        }
    }

//    if(showMeasurementDialog){
//        MeasurementDialog(
//            quotation.userData,
//            onDismiss = { showMeasurementDialog = false }
//        )
//
//    }
//
//    if(showStatusDialog){
//        StatusDialog(
//            quotation.status,
//            onDismiss = { showStatusDialog = false }
//        )
//
//    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatOrderDate(dateString: String): String { // Renamed to avoid ambiguity
    val date = ZonedDateTime.parse(dateString)
    return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
}

@Composable
fun ShippingAddressDialog(
    address: String,
    city: String,
    state: String,
    zip: String,
    country: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        // Outer surface to get rounded corners and white sheet look
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F5)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {

                // Header: centered title with close button on the right
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFFFF))
                        .padding(vertical = 14.dp, horizontal = 12.dp)
                ) {
                    Text(
                        "Shipping Address",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                // Inner white card with the fields
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            "SHIPPING ADDRESS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LabeledRow("Address", address)
                        Divider()
                        LabeledRow("City", city)
                        Divider()
                        LabeledRow("State", state)
                        Divider()
                        LabeledRow("Zip Code", zip)
                        Divider()
                        LabeledRow("Country", country)
                    }
                }
            }
        }
    }
}

@Composable
fun MeasurementDialog(
    userData: UserData,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        // Outer surface to get rounded corners and white sheet look
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F5)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {

                // Header: centered title with close button on the right
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFFFF))
                        .padding(vertical = 14.dp, horizontal = 12.dp)
                ) {
                    Text(
                        "Measurements",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                // Inner white card with the fields
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            "PERSONAL INFORMATION",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LabeledRow("Name", userData.name)
                        Divider()
                        LabeledRow("Age", userData.age.toString())
                        Divider()
                        LabeledRow("Height", userData.height.toString())
                        Divider()
                        LabeledRow("Weight", userData.weight.toString())
                        Divider()
                        LabeledRow("Gender", userData.genderType)
                    }
                }

                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            "MEASUREMENTS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LabeledRow("Head", userData.data.head)
                        Divider()
                        LabeledRow("Chest", userData.data.chest)
                        Divider()
                        LabeledRow("Waist", userData.data.waist)
                        Divider()
                        LabeledRow("Height", userData.data.height)
                        Divider()
                        LabeledRow("Shoulder", userData.data.sholder)
                        Divider()
                        LabeledRow("Arm Length", userData.data.armLength)
                        Divider()
                        LabeledRow("Leg Length", userData.data.legLength.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun StatusDialog(
    currentStatus: String,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val buttonColor = Color(0xFF4A90E2)
    var selected by rememberSaveable { mutableStateOf(currentStatus) } // local state

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F5)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                // header ...
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("SELECT STATUS", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                        val options = listOf("Created", "Reviewed", "Completed", "Shipped", "Delivered")
                        RadioGroup(options = options, initial = currentStatus) { chosen ->
                            selected = chosen
                        }

                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { onSubmit(selected) },
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                        ) {
                            Text("Submit", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(
            text = "$label: $value",
            fontSize = 16.sp
        )
    }
}

@Composable
fun <T> RadioGroup(
    options: List<T>,
    label: (T) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    initial: T = options.first(),
    onSelected: (T) -> Unit = {}
) {
    var selected by rememberSaveable { mutableStateOf(initial) }

    Column(modifier.selectableGroup()) {
        options.forEach { option ->
            val isSelected = option == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = {
                            selected = option
                            onSelected(option)
                        },
                        role = Role.RadioButton
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = isSelected, onClick = null) // Row handles clicks
                Spacer(Modifier.width(8.dp))
                Text(label(option))
            }
        }
    }
}