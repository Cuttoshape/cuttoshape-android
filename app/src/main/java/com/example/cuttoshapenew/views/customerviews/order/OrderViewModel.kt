package com.example.cuttoshapenew.views.customerviews.order

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuttoshapenew.apiclients.Filter
import com.example.cuttoshapenew.apiclients.Order
import com.example.cuttoshapenew.apiclients.OrderFilterRequest
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.utils.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// ViewModel
@HiltViewModel
class OrderViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _orders = mutableStateOf<List<Order>>(emptyList())
    val orders: State<List<Order>> = _orders

    var userid: String? = ""

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        fetchOrders()
    }

    fun fetchOrders() {
        viewModelScope.launch {
            val userId = DataStoreManager.getUserId(context).first()
            userid = userId
            _isLoading.value = true
            _error.value = null
            try {
                val request = OrderFilterRequest(
                    isListing = true,
                    limit = 10,
                    orderFilter = listOf(Filter("buyerId", userId?.toIntOrNull(), "equal")),
                    page = 1,
                    quotationFilter = emptyList(),
                    isDetail = false
                )
                val response = RetrofitClient.getClient(context).getOrders(request)
                _orders.value = response.rows
            } catch (e: Exception) {
                _error.value = "Failed to load orders: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// Date Formatting Helper
@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(dateString: String): String {
    val zonedDateTime = ZonedDateTime.parse(dateString)
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    return zonedDateTime.format(formatter)
}