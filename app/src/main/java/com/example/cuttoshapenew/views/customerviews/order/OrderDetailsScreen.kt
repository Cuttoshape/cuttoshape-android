package com.example.cuttoshapenew.views.customerviews.order

import android.R
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
import com.example.cuttoshapenew.apiclients.Order
import com.example.cuttoshapenew.apiclients.Quotation
import com.example.cuttoshapenew.views.customerviews.order.OrderDetailsViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderDetailsScreen(
    navController: NavController,
    orderId: Int,
    buyerId: Int,
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    val order = viewModel.order.observeAsState()
    val isLoading = viewModel.isLoading.observeAsState(false)
    val error = viewModel.error.observeAsState(null)

    LaunchedEffect(orderId, buyerId) {
        viewModel.fetchOrderDetails(orderId, buyerId)
    }

    if (isLoading.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error.value != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = error.value!!, color = Color.Red, fontSize = 16.sp)
        }
    } else if (order.value != null) {
        Log.d("Order", order.toString())
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

            // Order Summary
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
                    ) // Updated to formatOrderDate
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
                    Button(
                        onClick = { /* Navigate to shipping address */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAE51E6)),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Shipping Address", color = Color.White, fontSize = 12.sp)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    order.value!!.quotations.forEach { quotation ->
                        Text("Item: $${quotation.cost}.00", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Shipping: $${order.value!!.quotations.sumOf { it.shippingCost }}.00", fontSize = 14.sp, color = Color(0xFF628AF1))
                    Text(
                        "Grand Total: $${order.value!!.quotations.sumOf { it.cost } + order.value!!.quotations.sumOf { it.shippingCost }}.00",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF28A745)
                    )
                }
            }

            // Items Section
            Text(
                text = "Items (${order.value!!.itemCount})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            LazyColumn {
                items(order.value!!.quotations) { quotation ->
                    OrderItemCard(quotation)
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No order details found", fontSize = 16.sp)
        }
    }
}

@Composable
fun OrderItemCard(quotation: Quotation) {
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
                    onClick = { /* Navigate to measurement */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAE51E6)),
//                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Measurement", color = Color.White, fontSize = 12.sp)
                }
            }
            Text(text = quotation.status, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically).padding(
                start = 8.dp
            ))

        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatOrderDate(dateString: String): String { // Renamed to avoid ambiguity
    val date = ZonedDateTime.parse(dateString)
    return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
}