package com.example.cuttoshapenew.views.tailorviews.order

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TailorOrderScreen(navController: NavController, viewModel: OrderViewModel = hiltViewModel()) {
    val ordersState = viewModel.orders
    val isLoadingState = viewModel.isLoading
    val errorState = viewModel.error
    val userId = viewModel.userid

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            isLoadingState.value -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorState.value != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorState.value!!, color = Color.Red)
                }
            }
            ordersState.value.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders found")
                }
            }
            else -> {
                Log.d("UserId", userId.toString())
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(ordersState.value) { order ->
                        OrderCard(order = order, onClick = {
                            navController.navigate("orderDetails/tailor/${order.id}/${userId}")
                        })
                    }
                }
            }
        }
    }
}