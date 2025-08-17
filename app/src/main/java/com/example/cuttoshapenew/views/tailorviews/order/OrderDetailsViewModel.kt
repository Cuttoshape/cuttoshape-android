package com.example.cuttoshapenew.views.tailorviews.order

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuttoshapenew.apiclients.Order
import com.example.cuttoshapenew.apiclients.OrderFilterRequest
import com.example.cuttoshapenew.apiclients.Filter
import com.example.cuttoshapenew.apiclients.RetrofitClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _order = MutableLiveData<Order?>()
    val order: LiveData<Order?> = _order

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchOrderDetails(orderId: Int, buyerId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("Got here in Tailor", "I am")
            try {
                val request = OrderFilterRequest(
                    isDetail = true,
                    limit = 1,
                    orderFilter = listOf(Filter("id", orderId, "equal")),
                    page = 1,
                    quotationFilter = listOf(Filter("sellerId", buyerId, "equal")),
                    isListing = false
                )
                val response = RetrofitClient.getClient(context).getOrderDetails(request)
                _order.value = response.newRows.firstOrNull()
            } catch (e: Exception) {
                _error.value = "Failed to load order details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}